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
import androidx.compose.ui.unit.dp

data class Player(
    val id: String = "",
    val name: String = "",
    var challenge: String = "",
    var gameSessionId: String = ""
)


class LobbyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val playerName = intent.getStringExtra("playerName")

        playerName?.let { savePlayer(it, this) }

        setContent{
            LobbyScreenFireBase(context = this)
        }
    }
}

@Composable
fun LobbyScreenFireBase(context: Context) {
    val playerNames = remember { mutableStateOf<List<Player>>(emptyList()) }
    val challengerId = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        fetchPlayers(context, {playerNames.value = it}, { id -> challengerId.value = id})
    }

    LaunchedEffect(Unit) {
        listenForChallenge(context) { id ->
            challengerId.value = id
        }
    }

    Box {
        LobbyScreen(playerNames = playerNames.value, context = context)

        challengerId.value?.let { id ->
            ShowChallengeDialog(
                challengerId = id,
                onGameStart = {
                    acceptChallenge(id, context)
                    challengerId.value = null
                },
                onDismiss = { challengerId.value = null }
            )
        }
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LobbyScreen(playerNames: List<Player>, context: Context ) {
    Scaffold(
        containerColor = Color.Transparent,
        content = {
            Column(modifier = Modifier.padding(16.dp)) {
                if (playerNames.isEmpty()) {
                    Text("No players available", color = Color.White)
                } else {
                    playerNames.forEach { player ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = player.name, color = Color.White)
                            Button(onClick = {
                                sendChallenge(
                                    currentPlayerId = getCurrentPlayer(context) ?: "",
                                    challengePlayerId = player.id
                                )
                            }) {
                                Text("Challenge")
                            }
                        }
                    }
                }
            }
        }
    )
}


@Composable
fun ShowChallengeDialog(
    challengerId: String,
    onGameStart: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Challenge Received") },
        text = { Text(text = "Player $challengerId has challenged you!") },
        confirmButton = {
            Button(onClick = { onGameStart() }) {
                Text("Accept")
            }
        },
        dismissButton = {
            Button(onClick = { }) {
                Text("Decline")
            }
        }
    )
}

fun savePlayer(playerName: String, context: Context) {
    val db = Firebase.firestore
    val playerId = "${playerName}-${(0..999999).random()}"
    val playerData = hashMapOf(
        "id" to playerId,
        "name" to playerName,
        "challenge" to "",
        "gameSessionId" to ""
    )

    db.collection("Players").document(playerId)
        .set(playerData)
        .addOnSuccessListener {
            println("Player added: $playerId")
            savePlayerLocally(context, playerId)
        }
        .addOnFailureListener { e ->
            println("Error adding player: $e")
        }
}
fun savePlayerLocally(context: Context, playerId: String) {
    val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString("playerId", playerId).apply()
}

fun getCurrentPlayer(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("playerId", null)
}

fun sendChallenge(currentPlayerId: String, challengePlayerId: String) {
    val db = Firebase.firestore
    db.collection("Players").document(challengePlayerId)
        .update("challenge", currentPlayerId)
        .addOnSuccessListener{
            println("Challenge sent to $challengePlayerId from $currentPlayerId successfully")
        }
        .addOnFailureListener { e ->
            println("Error sending challenge: $e")
        }
}

fun acceptChallenge(challengerId: String, context: Context) {
    val db = Firebase.firestore
    val currentPlayerId = getCurrentPlayer(context) ?: return
    val gameSessionId = "$currentPlayerId-$challengerId"

    val sessionData = hashMapOf(
        "board" to List(6) { List(7) { 0 } },
        "currentPlayer" to 1
    )

    db.collection("gameSessions").document(gameSessionId)
        .set(sessionData)
        .addOnSuccessListener {
            db.collection("Players").document(currentPlayerId)
                .update("challenge", "", "gameSessionId", gameSessionId)
            db.collection("Players").document(challengerId)
                .update("challenge", "", "gameSessionId", gameSessionId)

            val intent = Intent(context, GameActivity::class.java)
            intent.putExtra("gameSessionId", gameSessionId)
            context.startActivity(intent)
        }
        .addOnFailureListener { e ->
            println("Error accepting challenge: $e")
        }
}

fun listenForChallenge(
    context: Context,
    onChallengeReceived: (String) -> Unit
) {
    val db = Firebase.firestore
    val currentPlayerId = getCurrentPlayer(context) ?: return

    db.collection("Players").document(currentPlayerId)
        .addSnapshotListener { snapshot, e ->
            if (e != null) {
                println("Error listening for challenges: $e")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val challenge = snapshot.getString("challenge") ?: ""
                if (challenge.isNotEmpty()) {
                    println("Challenge received from: $challenge")
                    onChallengeReceived(challenge)
                }
            }
        }
}


fun fetchPlayers(
    context: Context,
    onPlayersFetched: (List<Player>) -> Unit,
    onChallengeReceived: (String) -> Unit
) {
    val db = Firebase.firestore
    val currentPlayerId = getCurrentPlayer(context) ?: return

    db.collection("Players")
        .addSnapshotListener { snapshot, e ->
            if (e != null) {
                println("Error fetching players: $e")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val players = snapshot.documents.mapNotNull {
                    it.toObject(Player::class.java)
                }
                println("Fetched players: $players")
                onPlayersFetched(players.filter { it.id != currentPlayerId })

                val currentPlayer = players.find { it.id == currentPlayerId }
                if (currentPlayer?.challenge?.isNotEmpty() == true) {
                    println("Challenge received from: ${currentPlayer.challenge}")
                    onChallengeReceived(currentPlayer.challenge)
                }
            }
        }
}