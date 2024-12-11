package com.example.connectfourproject

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.unit.sp

@Composable
fun ConnectFour() {
    // Initialize the navigation controller for screen transitions
    val navController = rememberNavController()
    val model = GameModel()
    model.initGame()

    // Define navigation routes for different screens in the app
    NavHost(navController = navController, startDestination = "player") {
        composable("player") { NewPlayerScreen(navController, model) } // Player registration screen
        composable("lobby") { LobbyScreen(navController, model) } // Lobby screen
        composable("game/{gameId}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") // Extract game ID from the route
            GameScreen(navController, model, gameId) // Game play screen
        }
    }
}


@Composable
fun NewPlayerScreen(navController: NavController, model: GameModel) {
    // Retrieve shared preferences for storing player ID locally
    val sharedPreferences = LocalContext.current
        .getSharedPreferences("ConnectFour", Context.MODE_PRIVATE)

    // Check if a player ID is already stored, and navigate to the lobby screen if so
    LaunchedEffect(Unit) {
        model.localPlayerId.value = sharedPreferences.getString("playerId", null)
        if (model.localPlayerId.value != null) {
            navController.navigate("lobby")
        }
    }

    if (model.localPlayerId.value == null) {
        var playerName by remember { mutableStateOf("") } // State to hold the player's name

        // Background image and input form for player name
        Box(
            modifier = Modifier
                .fillMaxSize()
                .paint(
                    painter = painterResource(id = R.drawable.backmain),
                    contentScale = ContentScale.FillBounds
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Connect Four",
                    color = Color.White)

                Spacer(modifier = Modifier.height(16.dp))

                // Input field for entering a unique player name
                OutlinedTextField(
                    value = playerName,
                    onValueChange = { playerName = it },
                    label = { Text(
                        text = "Enter your UserID",
                        color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White)
                )

                Spacer(modifier = Modifier.height(16.dp))


                // Button to create a new player in the database
                Button(
                    onClick = {
                        if (playerName.isNotBlank()) {
                            val newPlayer = Player(name = playerName)

                            model.db.collection("players") // Input the new player into the "players" collection in database
                                .add(newPlayer)
                                .addOnSuccessListener { documentRef ->
                                    val newPlayerId = documentRef.id
                                    sharedPreferences.edit()
                                        .putString("playerId", newPlayerId).apply()
                                    model.localPlayerId.value = newPlayerId
                                    navController.navigate("lobby")
                                }.addOnFailureListener { error ->
                                    Log.e(
                                        "Error",
                                        "Error creating player: ${error.message}"
                                    )
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Create Player",
                        color = Color.White)
                }
            }
        }
    } else { // If a player ID is found, show a loading screen

        Text(
            text = "Loading....",
            color = Color.White,
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(align = Alignment.Center)
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(navController: NavController, model: GameModel) {
    // Observe player and game data from the GameModel
    val players by model.playerMap.asStateFlow().collectAsStateWithLifecycle()
    val games by model.gameMap.asStateFlow().collectAsStateWithLifecycle()

    // Manage dialog visibility for challenges and game invitations
    var showChallengeDialog by remember { mutableStateOf(false) }
    var showAcceptDialog by remember { mutableStateOf(false) }
    var challengePlayerName by remember { mutableStateOf("") }
    var challengePlayerId by remember { mutableStateOf<String?>(null) }
    var acceptGameId by remember { mutableStateOf<String?>(null) }

    // Automatically navigate to the game screen if a game involving the local player starts
    LaunchedEffect(games) {
        games.forEach { (gameId, game) ->
            if ((game.player1Id == model.localPlayerId.value || game.player2Id == model.localPlayerId.value)
                && (game.gameState == "player1_turn" || game.gameState == "player2_turn")
            ) {
                navController.navigate("game/$gameId")
            }
        }
    }

    // Retrieve the local player's name from the players map
    var playerName = "Unknown?"
    players[model.localPlayerId.value]?.let {
        playerName = it.name
    }

    // Display the lobby interface
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Connect Four - $playerName",
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(align = Alignment.Center)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent) // Set the background color to transparent to see background image
            )
        }
    ) { innerPadding ->
        Box(
            // Create a Box with a background image
            modifier = Modifier
                .fillMaxSize()
                .paint(
                    painter = painterResource(id = R.drawable.backmain),
                    contentScale = ContentScale.FillBounds
                )
        ) {
            // Display a list of players and options to challenge them
            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                items(players.entries.toList()) { (documentId, player) ->
                    if (documentId != model.localPlayerId.value) {
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = "Player Name: ${player.name}",
                                    color = Color.White
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = "User ID: $documentId",
                                    color = Color.White
                                )
                            },
                            trailingContent = {
                                var hasGame = false
                                games.forEach { (gameId, game) ->
                                    // If player1 is waiting for player2 to accept
                                    if (game.player1Id == model.localPlayerId.value
                                        && game.gameState == "invite"
                                    ) {
                                        Text(
                                            text = "Waiting for accept...",
                                            color = Color.White
                                        )
                                        hasGame = true
                                    }
                                    // If player2 has been invited by player1
                                    else if (game.player2Id == model.localPlayerId.value
                                        && game.gameState == "invite"
                                    ) {
                                        Button(onClick = {
                                            acceptGameId = gameId
                                            showAcceptDialog = true
                                        }) {
                                            Text(
                                                text = "Message...!",
                                                color = Color.White
                                            )
                                        }
                                        hasGame = true
                                    }
                                }
                                if (!hasGame) {
                                    Button(onClick = {
                                        challengePlayerName = player.name
                                        challengePlayerId = documentId
                                        showChallengeDialog = true
                                    }) {
                                        Text(
                                            text = "Challenge",
                                            color = Color.White
                                        )
                                    }
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent) // Set the background color to transparent to see background image
                        )
                    }
                }
            }

            // Challenge Dialog
            if (showChallengeDialog && challengePlayerId != null) {
                AlertDialog(
                    onDismissRequest = { showChallengeDialog = false },
                    title = {
                        Text(text = "Challenge Player")
                    },
                    text = {
                        Text(text = "Do you want to challenge $challengePlayerName?")
                    },
                    confirmButton = {
                        Button(onClick = {
                            model.db.collection("games") // Add a new game to the database
                                .add(
                                    Game(
                                        gameState = "invite",
                                        player1Id = model.localPlayerId.value!!,
                                        player2Id = challengePlayerId!!
                                    )
                                )
                                .addOnSuccessListener {
                                    Log.d("Lobby", "Challenge sent to $challengePlayerId")
                                }
                                .addOnFailureListener {
                                    Log.e("Error", "Error sending challenge")
                                }
                            showChallengeDialog = false
                        }) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        Button(onClick = {
                            showChallengeDialog = false
                        }) {
                            Text("No")
                        }
                    }
                )
            }

            // Accept Invite Dialog
            if (showAcceptDialog && acceptGameId != null) {
                AlertDialog(
                    onDismissRequest = { showAcceptDialog = false },
                    title = {
                        Text(text = "Game Invite")
                    },
                    text = {
                        Text(text = "You have been challenged to a game...")
                    },
                    confirmButton = {
                        Button(onClick = {
                            model.db.collection("games").document(acceptGameId!!) // Update the game in the database
                                .update("gameState", "player1_turn")
                                .addOnSuccessListener {
                                    navController.navigate("game/$acceptGameId")
                                }
                                .addOnFailureListener {
                                    Log.e("Error", "Error accepting invite: ${it.message}")
                                }
                            showAcceptDialog = false
                        }) {
                            Text("Accept")
                        }
                    },
                    dismissButton = {
                        Button(onClick = {
                            model.db.collection("games").document(acceptGameId!!) // Update the game in the database
                                .update("gameState", "declined")
                                .addOnSuccessListener {
                                    Log.d("Lobby", "Game invite declined")
                                }
                                .addOnFailureListener {
                                    Log.e("Error", "Error declining invite: ${it.message}")
                                }
                            showAcceptDialog = false
                        }) {
                            Text("Decline")
                        }
                    }
                )
            }
        }
    }
}







@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(navController: NavController, model: GameModel, gameId: String?) {
    // Observe player and game data from the GameModel
    val players by model.playerMap.asStateFlow().collectAsStateWithLifecycle()
    val games by model.gameMap.asStateFlow().collectAsStateWithLifecycle()

    // Retrieve the local player's name from the players map
    var playerName = "Unknown?"
    players[model.localPlayerId.value]?.let {
        playerName = it.name
    }

    // Ensure that the game ID is valid and exists in the games map
    if (gameId != null && games.containsKey(gameId)) {
        val game = games[gameId]!! // Retrieve the game object

        // Scaffold for the Game Screen UI
        Scaffold(
            topBar = {
                // Display the game title with the player names
                TopAppBar(
                    title = { Text("${players[game.player1Id]!!.name} vs ${players[game.player2Id]!!.name}",
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(align = Alignment.Center)) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent) // Set the background color to transparent to see background image
                )
            }
        ) { innerPadding ->
            // Background image for the game screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .paint(
                        painter = painterResource(id = R.drawable.backmain),
                        contentScale = ContentScale.FillBounds
                    )
            ) {
                // Centered content for the game states and board
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxWidth()
                ) {
                    // Handle different game states: game over, draw, etc
                    when (game.gameState) {
                        "player1_won", "player2_won", "draw" -> {
                            // Display game result
                            Text("Game over!",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium,
                                fontSize = 60.sp)
                            Spacer(modifier = Modifier.padding(20.dp))

                            if (game.gameState == "draw") {
                                Text(
                                    text = "It's a Draw!",
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            } else {
                                Text(
                                    text = "${if (game.gameState == "player1_won") players[game.player1Id]!!.name else players[game.player2Id]!!.name} won!", // Display the winner
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                            Spacer(modifier = Modifier.padding(10.dp))

                            // Button to return to the lobby
                            Button(onClick = {
                                navController.navigate("lobby")
                            }) {
                                Text(
                                    text = "Back to lobby",
                                    color = Color.White)
                            }
                        }

                        else -> {
                            // Display the current player's turn
                            val myTurn =
                                game.gameState == "player1_turn" && game.player1Id == model.localPlayerId.value ||
                                        game.gameState == "player2_turn" && game.player2Id == model.localPlayerId.value

                            val turn = if (myTurn) "Your turn!" else "Wait for other player"
                            Text(
                                turn,
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium,
                            )
                            Spacer(modifier = Modifier.padding(20.dp))
                        }
                    }


                    Spacer(modifier = Modifier.padding(20.dp))

                    // Game board grid
                    for (i in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (j in 0 until cols) {

                                // Individual button for each cell on the board
                                Button(
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .weight(1F)
                                        .aspectRatio(1F)
                                        .padding(1.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                                    onClick = {
                                        model.checkGameState(gameId, i * cols + j) // Handle the move
                                    },
                                    enabled = game.gameState != "player1_won" &&
                                            game.gameState != "player2_won" &&
                                            game.gameState != "draw"
                                ) {
                                    //Cell content with color based on the game state
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(8.dp)
                                            .aspectRatio(1F)
                                            .background(
                                                color = when (game.gameBoard[i * cols + j]) {
                                                    1 -> Color.Red //Player's 1 piece
                                                    2 -> Color.Yellow //Player's 2 piece
                                                    else -> Color.LightGray //Empty cell
                                                },
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }  else {
        // Log and navigate to the lobby if the game is not found
        Log.e(
            "Error",
            "Error Game not found: $gameId"
        )
        navController.navigate("lobby")
    }
}