package com.example.connectfourproject

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun ConnectFour() {
    val navController = rememberNavController()
    val model = GameModel()
    model.initGame()

    NavHost(navController = navController, startDestination = "player") {
        composable("player") { NewPlayerScreen(navController, model) }
        composable("lobby") { LobbyScreen(navController, model) }
        composable("game/{gameId}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")
            GameScreen(navController, model, gameId)
        }
    }
}


@Composable
fun NewPlayerScreen(navController: NavController, model: GameModel) {
    val sharedPreferences = LocalContext.current
        .getSharedPreferences("test1", Context.MODE_PRIVATE)

    LaunchedEffect(Unit) {
        model.localPlayerId.value = sharedPreferences.getString("playerId", null)
        if (model.localPlayerId.value != null) {
            navController.navigate("lobby")
        }
    }

    if (model.localPlayerId.value == null) {
        var playerName by remember { mutableStateOf("") }

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

                Button(
                    onClick = {
                        if (playerName.isNotBlank()) {
                            val newPlayer = Player(name = playerName)

                            model.db.collection("players")
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
    } else {
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
    val players by model.playerMap.asStateFlow().collectAsStateWithLifecycle()
    val games by model.gameMap.asStateFlow().collectAsStateWithLifecycle()

    LaunchedEffect(games) {
        games.forEach { (gameId, game) ->
            // TODO: Popup with accept invite?
            if ((game.player1Id == model.localPlayerId.value || game.player2Id == model.localPlayerId.value)
                && (game.gameState == "player1_turn" || game.gameState == "player2_turn")
            ) {
                navController.navigate("game/${gameId}")
            }
        }
    }

    var playerName = "Unknown?"
    players[model.localPlayerId.value]?.let {
        playerName = it.name
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connect Four - $playerName",
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(align = Alignment.Center)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent)) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .paint(
                    painter = painterResource(id = R.drawable.backmain),
                    contentScale = ContentScale.FillBounds
                )
        ) {
            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                items(players.entries.toList()) { (documentId, player) ->
                    if (documentId != model.localPlayerId.value) {
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = "Player Name: ${player.name}",
                                    color = Color.White)
                            },
                            supportingContent = {
                                Text(text = "User ID: $documentId",
                                    color = Color.White)
                            },
                            trailingContent = {
                                var hasGame = false
                                games.forEach { (gameId, game) ->
                                    if (game.player1Id == model.localPlayerId.value
                                        && game.gameState == "invite"
                                    ) {
                                        Text(text = "Waiting for accept...",
                                            color = Color.White)
                                        hasGame = true
                                    } else if (game.player2Id == model.localPlayerId.value
                                        && game.gameState == "invite"
                                    ) {
                                        Button(onClick = {
                                            model.db.collection("games").document(gameId)
                                                .update("gameState", "player1_turn")
                                                .addOnSuccessListener {
                                                    navController.navigate("game/${gameId}")
                                                }
                                                .addOnFailureListener {
                                                    Log.e(
                                                        "Error",
                                                        "Error updating game: $gameId"
                                                    )
                                                }
                                        }) {
                                            Text(
                                                text = "Accept invite",
                                                color = Color.White)
                                        }
                                        hasGame = true
                                    }
                                }
                                if (!hasGame) {
                                    Button(onClick = {
                                        model.db.collection("games")
                                            .add(
                                                Game(
                                                    gameState = "invite",
                                                    player1Id = model.localPlayerId.value!!,
                                                    player2Id = documentId
                                                )
                                            )
                                            .addOnSuccessListener { documentRef ->
                                                // TODO: Navigate?
                                            }
                                    }) {
                                        Text(
                                            text = "Challenge",
                                            color = Color.White)
                                    }
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(navController: NavController, model: GameModel, gameId: String?) {
    val players by model.playerMap.asStateFlow().collectAsStateWithLifecycle()
    val games by model.gameMap.asStateFlow().collectAsStateWithLifecycle()

    var playerName = "Unknown?"
    players[model.localPlayerId.value]?.let {
        playerName = it.name
    }


    if (gameId != null && games.containsKey(gameId)) {
        val game = games[gameId]!!
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("${players[game.player1Id]!!.name} vs ${players[game.player2Id]!!.name}",
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(align = Alignment.Center)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .paint(
                        painter = painterResource(id = R.drawable.backmain),
                        contentScale = ContentScale.FillBounds
                    )
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(innerPadding).fillMaxWidth()
                ) {
                    when (game.gameState) {
                        "player1_won", "player2_won", "draw" -> {

                            Text("Game over!",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.padding(20.dp))

                            if (game.gameState == "draw") {
                                Text(
                                    text = "It's a Draw!",
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            } else {
                                Text(
                                    text = "Player ${if (game.gameState == "player1_won") "1" else "2"} won!",
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                            Button(onClick = {
                                navController.navigate("lobby")
                            }) {
                                Text(
                                    text = "Back to lobby",
                                    color = Color.White)
                            }
                        }

                        else -> {

                            val myTurn =
                                game.gameState == "player1_turn" && game.player1Id == model.localPlayerId.value || game.gameState == "player2_turn" && game.player2Id == model.localPlayerId.value
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

                    for (i in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (j in 0 until cols) {
                                Button(
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .weight(1F)
                                        .aspectRatio(1F)
                                        .padding(1.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                                    onClick = {
                                        model.checkGameState(gameId, i * cols + j)
                                    },
                                    enabled = game.gameState != "player1_won" &&
                                            game.gameState != "player2_won" &&
                                            game.gameState != "draw"
                                ) {
                                    when (game.gameBoard[i * cols + j]) {
                                        1 -> Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(Color.Red, shape = CircleShape)
                                        )
                                        2 -> Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(Color.Yellow, shape = CircleShape)
                                        )

                                        else -> Text("")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }  else {
        Log.e(
            "Error",
            "Error Game not found: $gameId"
        )
        navController.navigate("lobby")
    }
}





