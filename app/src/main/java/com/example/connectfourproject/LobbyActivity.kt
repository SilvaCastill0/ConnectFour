package com.example.connectfourproject

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview

data class Player(
    val playerId: String = "",
    val name: String = "",
    var challenge: String = "",
)


class LobbyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val playerName = intent.getStringExtra("playerName")

        if (playerName != null) {
            savePlayer(playerName, this)
        }

        setContent{
            LobbyScreenFireBase()
        }
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LobbyScreen(playerNames: List<Player>) {
    Box {
        Image(
            painter = painterResource(id = R.drawable.backmain),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { TopBarBackButton() },
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {

                playerNames.forEach { name ->
                    ListItem(
                        headlineContent = {
                            Text(text = name.name)
                        },
                        supportingContent = {
                            Text(text = name.playerId)
                        },
                        trailingContent = {
                            Button(onClick = {
                            sendChallenge(currentPlayerId = "YOUR_CURRENT_PLAYER_ID", challengePlayerId = name.playerId)
                            }) {
                                Text(text = "Challenge")
                            }
                        }
                    )
                }
            }

        }
    }
}


@Composable
fun LobbyScreenFireBase() {
    val playerNames = remember { mutableStateOf<List<Player>>(emptyList()) }

    LaunchedEffect(Unit) {
        fetchPlayer(
            onPlayerFetched = { fetchedPlayerNames ->
                playerNames.value = fetchedPlayerNames
            },
            onChallengeReceived = { challengerId ->
                ShowChallengeDialog(challengerId)
            }
        )
    }

    LobbyScreen(playerNames = playerNames.value)
}


fun savePlayer(playerName: String, context: Context) {
    val db = Firebase.firestore
    val randomID = "${playerName}-${(0..999999).random()}"
    val playerData = hashMapOf(
        "id" to randomID,
        "name" to playerName
    )

    db.collection("Players").document(randomID)
        .set(playerData)
        .addOnSuccessListener {
            println("Player added with UserNameID: &randomID")

            savePlayerLocally(context, randomID)
        }
        .addOnFailureListener { e ->
            println("Error adding player: $e")
        }
}

fun savePlayerLocally(context: Context, playerId: String) {
    val sharedPreferences = context.getSharedPreferences("MyPrefs", MODE_PRIVATE)
    sharedPreferences.edit().putString("playerId", playerId).apply()
}

fun getCurrentPlayer(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("MyPrefs", MODE_PRIVATE)
    return sharedPreferences.getString("playerId", null)
}

@Composable
fun ShowChallengeDialog(challengerId: String) {
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Challenge Received") },
            text = { Text(text = "Player $challengerId has challenged you!") },
            confirmButton = {
                Button(onClick = {
                    onGameStart()
                    showDialog = false
                }) {
                    Text("Accept")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Decline")
                }
            }
        )
    }
}




fun fetchPlayer(onPlayerFetched: (List<Player>) -> Unit, onChallengeReceived: (String) -> Unit) {
    val db = Firebase.firestore
    db.collection("Players")
        .addSnapshotListener { snapshot, e ->
            if (e != null) {
                println("Error fetching player names: $e")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val players = snapshot.documents.map { document ->
                    val player = document.toObject(Player::class.java) ?: Player()
                    player.copy(playerId = document.id)
                }
                onPlayerFetched(players)

                val currentPlayer = players.find { it.playerId == "YOUR_CURRENT_PLAYER_ID" }
                if (currentPlayer?.challenge?.isNotEmpty() == true) {
                    onChallengeReceived(currentPlayer.challenge)
                }
            }
        }
}

fun sendChallenge(currentPlayerId: String, challengePlayerId: String) {
    val db = Firebase.firestore
    val gameSessionId = "$currentPlayerId-$challengePlayerId"
    db.collection("Players").document(currentPlayerId)
        .update("challenge", challengePlayerId, "gameSessionId", gameSessionId)
        .addOnSuccessListener {
            println("Challenge sent to $challengePlayerId from $currentPlayerId successfully")
        }
        .addOnFailureListener { e ->
            println("Error sending challenge: $e")
        }
}

fun acceptChallenge(challengerId: String, context: Context) {
    val db = Firebase.firestore
    val currentPlayerId = getCurrentPlayer(context)
    if (currentPlayerId != null) {
        db.collection("Players").document(currentPlayerId)
            .get()
            .addOnSuccessListener { document ->
                val gameSessionId = document.getString("gameSessionId")
                if (gameSessionId != null) {
                    db.collection("Players").document(currentPlayerId)
                        .update("challenge", "")
                        .addOnSuccessListener {
                            val intent = Intent(context, GameActivity::class.java)
                            intent.putExtra("gameSessionId", gameSessionId)
                            context.startActivity(intent)
                        }
                }
            }
            .addOnFailureListener { e ->
                println("Error accepting challenge: $e")
            }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLobbyScreen() {
    val mockPlayerNames = listOf(
        Player(name = "Player 1", playerId = "3"),
        Player(name = "Player 2", playerId = "2"),
        Player(name = "Player 3", playerId = "1")
    )

    LobbyScreen(playerNames = mockPlayerNames)
}
/*
@Composable
fun MainScreen() {
    val db = Firebase.firestore
    val playerList = MutableStateFlow<List<Player>>(emptyList())

    db.collection("players")
        .addSnapshotListener{ value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (value != null) {
                playerList.value = value.toObjects()
            }
        }

    val players by playerList.asStateFlow().collectAsStateWithLifecycle()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(players) { player ->

                ListItem(
                    headlineContent = {
                        Text("Name: ${player.name}")
                    },
                    supportingContent = {
                        Text("Score: ${player.score}")
                    },
                    trailingContent = {
                        Button(onClick = {
                            val query = db.collection("players").whereEqualTo("playerId", player.playerId)

                            query.get().addOnSuccessListener { querySnapshot ->
                                for (documentSnapshot in querySnapshot) {
                                    documentSnapshot.reference.update("invitation", "Hello!")
                                }}



                        }) {
                            Text("Invite")
                        }
                    }
                )


            }
        }
    }
}
  */

