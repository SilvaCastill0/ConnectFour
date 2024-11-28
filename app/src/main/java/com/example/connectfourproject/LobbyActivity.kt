package com.example.connectfourproject

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.appcompat.app.AlertDialog

data class Player(
    val id: String = "",
    val name: String = "",
    var challenge: String? = null,
    var gameSessionId: String? = null
)


class LobbyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val playerName = intent.getStringExtra("playerName")

        playerName?.let { savePlayer(it, this) }

        setContent {
            LobbyScreenFireBase()
        }
    }
}

@Composable
fun LobbyScreenFireBase() {
    val context = LocalContext.current
    val playerNames = remember { mutableStateOf<List<Player>>(emptyList()) }

    LaunchedEffect(Unit) {
        fetchPlayers(context) { playerNames.value = it }
    }

    Box {
        LobbyScreen(playerNames = playerNames.value, context = context)
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LobbyScreen(playerNames: List<Player>, context: Context ) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (playerNames.isEmpty()) {
                Text("No players available")
            } else {
                playerNames.forEach { player ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = player.name)
                        Button(onClick = {
                            sendChallenge(
                                currentPlayerId = getCurrentPlayerId(context) ?: "",
                                challengePlayerId = player.id,
                                context
                            )
                        }) {
                            Text("Challenge")
                        }
                    }
                }
            }
        }
    }
}


fun savePlayer(playerName: String, context: Context) {
    val db = Firebase.firestore
    val playerId = "$playerName-${(0..9999999).random()}"

    val playerData = hashMapOf(
        "id" to playerId,
        "name" to playerName,
        "challenge" to null,
        "gameSessionId" to null
    )

    db.collection("Players").document(playerId)
        .set(playerData)
        .addOnSuccessListener {
            println("Player added: $playerId")
            savePlayerLocally(context, playerId)
            listenForChallenges(playerId, context)
        }
        .addOnFailureListener { e ->
            println("Error adding player: $e")
        }
}

fun savePlayerLocally(context: Context, playerId: String) {
    val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString("playerId", playerId).apply()
}

fun getCurrentPlayerId(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("playerId", null)
}


fun listenForChallenges(playerId: String, context: Context) {
    val db = Firebase.firestore

    db.collection("Challenges")
        .whereEqualTo("challengedId", playerId)
        .whereEqualTo("status", "pending")
        .addSnapshotListener { snapshot, e ->
            if (snapshot != null && !snapshot.isEmpty) {
                val challenge = snapshot.documents.first()
                val challengerId = challenge.getString("challengerId") ?: return@addSnapshotListener
                showChallengeDialog(challengerId, challenge.id, context)
            }
        }
}

fun sendChallenge(currentPlayerId: String, challengePlayerId: String, context: Context) {
    val db = Firebase.firestore
    val challengeData = hashMapOf(
        "challengerId" to currentPlayerId,
        "challengedId" to challengePlayerId,
        "status" to "pending"
    )

    db.collection("Challenges")
        .add(challengeData)
        .addOnSuccessListener { documentReference ->
            println("Challenge sent successfully!")

            listenForChallengeUpdates(documentReference.id, context)
        }
        .addOnFailureListener { e ->
            println("Error sending challenge: $e")
        }
}

fun listenForChallengeUpdates(challengeId: String, context: Context) {
    val db = Firebase.firestore

    db.collection("Challenges").document(challengeId)
        .addSnapshotListener { snapshot, e ->
            if (snapshot != null && snapshot.exists()) {
                val status = snapshot.getString("status")
                val gameSessionId = snapshot.getString("gameSessionId")

                if (status == "accepted" && !gameSessionId.isNullOrEmpty()) {
                    navigateToGameActivity(context, gameSessionId)
                }
            }
        }
}

fun navigateToGameActivity(context: Context, gameSessionId: String) {
    val intent = Intent(context, GameActivity::class.java)
    intent.putExtra("gameSessionId", gameSessionId)
    context.startActivity(intent)
}

fun showChallengeDialog(challengerId: String, challengeId: String, context: Context) {
    AlertDialog.Builder(context)
        .setTitle("Challenge Received")
        .setMessage("Player $challengerId has challenged you!")
        .setPositiveButton("Accept") { _, _ ->
            acceptChallenge(challengeId, context)
        }
        .setNegativeButton("Decline", null)
        .show()
}


fun acceptChallenge(challengeId: String, context: Context) {
    val db = Firebase.firestore
    val gameSessionId = "game-${(0..999999).random()}"

    db.collection("Challenges").document(challengeId)
        .get()
        .addOnSuccessListener { challengeDoc ->
            val challengerId = challengeDoc.getString("challengerId") ?: return@addOnSuccessListener
            val challengedId = challengeDoc.getString("challengedId") ?: return@addOnSuccessListener

            val gameData = hashMapOf(
                "board" to List(6) { List(7) { 0 } },
                "currentPlayer" to challengerId,
                "player1Id" to challengerId,
                "player2Id" to challengedId,
                "status" to "active"
            )

            db.collection("gameSessions").document(gameSessionId)
                .set(gameData)
                .addOnSuccessListener {
                    db.collection("Players").document(challengerId).update("gameSessionId", gameSessionId)
                    db.collection("Players").document(challengedId).update("gameSessionId", gameSessionId)
                    navigateToGameActivity(context, gameSessionId)
                }
        }
}

fun fetchPlayers(
    context: Context,
    onPlayersFetched: (List<Player>) -> Unit
) {
    val db = Firebase.firestore
    val currentPlayerId = getCurrentPlayerId(context) ?: return

    db.collection("Players")
        .addSnapshotListener { snapshot, e ->
            if (snapshot != null) {
                val players = snapshot.documents.mapNotNull {
                    it.toObject(Player::class.java)
                }
                println("Fetched players: $players")
                onPlayersFetched(players.filter { it.id != currentPlayerId })
            }
        }
}