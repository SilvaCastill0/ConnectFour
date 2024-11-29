package com.example.connectfourproject

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.dp


class LobbyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val playerName = intent.getStringExtra("playerName")

        setContent {
            val model: GameModel = viewModel()

            playerName?.let {
                model.registerPlayer(it)
            }

            LobbyScreen(model = model)
        }
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LobbyScreen(model: GameModel) {
    val playersState = model.playerMap.collectAsState()
    val players = playersState.value
    val challengePopup = remember { mutableStateOf<Pair<String, String>?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        model.init()
        model.listenForChallenges { challengeId, challengerId ->
            challengePopup.value = Pair(challengeId, challengerId)

        }
    }

    Scaffold {
        Column(modifier = Modifier.padding(16.dp)) {
            if (players.isEmpty()) {
                Text("No players available.")
            } else {
                players.entries.forEach { (id, player) ->
                    PlayerListItem(playerName = player.name) {
                        model.createChallenge(id)
                    }
                }
            }

            challengePopup.value?.let { (challengeId, challengerId) ->
                AlertDialog(
                    onDismissRequest = { challengePopup.value = null },
                    title = { Text("Challenge Received") },
                    text = { Text("Player $challengerId has challenged you!") },
                    confirmButton = {
                        Button(onClick = {
                            model.acceptChallenge(challengeId, context)
                            challengePopup.value = null
                        }) {
                            Text("Accept")
                        }
                    },
                    dismissButton = {
                        Button(onClick = {
                            model.declineChallenge(challengeId)
                            challengePopup.value = null
                        }) {
                            Text("Decline")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PlayerListItem(playerName: String, onChallengeClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(playerName)
        Button(onClick = onChallengeClick) {
            Text("Challenge")
        }
    }
}



/*
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
    )
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

fun getCurrentPlayer(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("playerId", null)
}



fun listenForChallenges(playerId: String, context: Context) {
    val db = Firebase.firestore

    db.collection("Challenges")
        .whereEqualTo("challengedId", playerId)
        .whereEqualTo("status", "pending")
        .addSnapshotListener { snapshot, e ->
            if (e != null) {
                println("Error listening for challenges: $e")
                return@addSnapshotListener
            }

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
        .addOnSuccessListener {
            println("Challenge sent successfully!")

            listenForChallengeUpdates(currentPlayerId, context)
        }
        .addOnFailureListener { e ->
            println("Error sending challenge: $e")
        }
}

fun listenForChallengeUpdates(challengeId: String, context: Context) {
    val db = Firebase.firestore

    db.collection("Challenges").document(challengeId)
        .addSnapshotListener { snapshot, e ->
            if (e != null) {
                println("Error listening for challenge updates: $e")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val status = snapshot.getString("status")
                val gameSessionId = snapshot.getString("gameSessionId")

                if (status == "accepted" && !gameSessionId.isNullOrEmpty()) {
                    println("Challenge accepted! Game session ID: $gameSessionId")

                    // Navigate to GameActivity
                    val intent = Intent(context, GameActivity::class.java)
                    intent.putExtra("gameSessionId", gameSessionId)
                    context.startActivity(intent)
                }
            }
        }
}

fun showChallengeDialog(challengerId: String, challengeId: String, context: Context) {
    AlertDialog.Builder(context)
        .setTitle("Challenge Received")
        .setMessage("Player $challengerId has challenged you!")
        .setPositiveButton("Accept") { _, _ ->
            acceptChallenge(challengeId, context)
        }
        .setNegativeButton("Decline") { _, _ ->
            declineChallenge(challengeId)
        }
        .show()
}

fun declineChallenge(challengeId: String) {
    val db = Firebase.firestore
    db.collection("Challenges").document(challengeId)
        .update("status", "declined")
        .addOnSuccessListener {
            println("Challenge declined.")
        }
        .addOnFailureListener { e ->
            println("Error declining challenge: $e")
        }
}

fun acceptChallenge(challengeId: String, context: Context) {
    val db = Firebase.firestore
    val gameSessionId = "game-${(0..999999).random()}"

    val gameSessionData = hashMapOf(
        "board" to List(6) { List(7) { 0 } },
        "currentPlayer" to 1,
        "status" to "active"
    )

    db.collection("Challenges").document(challengeId)
        .update("status", "accepted", "gameSessionId", gameSessionId)
        .addOnSuccessListener {
            // Create the game session
            db.collection("gameSessions").document(gameSessionId)
                .set(gameSessionData)
                .addOnSuccessListener {
                    // Fetch the challenge to update player documents
                    db.collection("Challenges").document(challengeId).get()
                        .addOnSuccessListener { document ->
                            val challengerId =
                                document.getString("challengerId") ?: return@addOnSuccessListener
                            val challengedId =
                                document.getString("challengedId") ?: return@addOnSuccessListener

                            // Update gameSessionId for both players
                            db.collection("Players").document(challengerId)
                                .update("gameSessionId", gameSessionId)
                            db.collection("Players").document(challengedId)
                                .update("gameSessionId", gameSessionId)

                            // Navigate to GameActivity
                            val intent = Intent(context, GameActivity::class.java)
                            intent.putExtra("gameSessionId", gameSessionId)
                            context.startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            println("Error accepting challenge: $e")
                        }
                }
                .addOnFailureListener { e ->
                    println("Error accepting challenge: $e")
                }
        }
        .addOnFailureListener { e ->
            println("Error accepting challenge: $e")
        }
}

fun fetchPlayers(
    context: Context,
    onPlayersFetched: (List<Player>) -> Unit
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
            }
        }
}

 */