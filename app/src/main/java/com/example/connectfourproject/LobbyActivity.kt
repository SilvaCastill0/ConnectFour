package com.example.connectfourproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class LobbyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val playerName = intent.getStringExtra("playerName")

        playerName?.let {
            savePlayerNameToFirebase(it)
        }

        setContent{
            LobbyScreen()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        deleteAllPlayer()
    }
}


@Composable
fun LobbyScreen() {
    val playerNames = remember { mutableStateOf<List<String>>(emptyList()) }
    var playerNamesListener: ListenerRegistration? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        playerNamesListener = listenForPlayerUpdates { names ->
            playerNames.value = names
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            playerNamesListener?.remove()
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(playerNames.value.size) { playerName ->

                ListItem(
                    headlineContent = {
                        Text("Name: ${playerNames.value[playerName]}")
                    },
                    supportingContent =  {
                        Text("Player ID: ${playerName + 1}")
                    },
                    trailingContent = {
                        Button(onClick = {
                            val player1 = playerNames.value[playerName]
                            val player2 = playerNames.value[(playerName + 1) % playerNames.value.size]
                        }) { }
                    })
            }
        }
    }
}


fun savePlayerNameToFirebase(playerName: String) {
    val db = Firebase.firestore
    val playerData = hashMapOf("name" to playerName)

    db.collection("Players")
        .add(playerData)
        .addOnSuccessListener { documentReference ->
            println("Player added with UserNameID: &{documentReference.id}")
        }
        .addOnFailureListener { e ->
            println("Error adding player: $e")
        }
}

fun listenForPlayerUpdates(onNamesUpdated: (List<String>) -> Unit): ListenerRegistration {
    val dp = Firebase.firestore
    return dp.collection("Players")
        .addSnapshotListener { snapshot, e ->
            if (e != null) {
                println("Error listening for player updates: $e")
                return@addSnapshotListener
            }

            val playerNames = snapshot?.documents?.mapNotNull { it.getString("name") } ?: emptyList()
            onNamesUpdated(playerNames)
        }
}

fun deleteAllPlayer() {
    val db = Firebase.firestore

    db.collection("Players")
        .get()
        .addOnSuccessListener { snapshot ->
            for (document in snapshot.documents) {
                document.reference.delete()
                    .addOnSuccessListener {
                        println("Player deleted successfully")
                    }
                    .addOnFailureListener { e ->
                        println("Error deleting player: $e")
                    }
            }
        }
        .addOnFailureListener { e ->
            println("Error getting players: $e")
        }
}

fun createGameSession(player1: String, player2: String, onSessionCreated: (String) -> Unit) {
    val db = Firebase.firestore
    val gameData = hashMapOf(
        "player1" to player1,
        "player2" to player2,
        "boardState" to Array(6) { Array(7) { 0 } },
        "currentPlayer" to player1
    )

    db.collection("GameSessions")
        .add(gameData)
        .addOnSuccessListener { documentReference ->
            onSessionCreated(documentReference.id)
        }
        .addOnFailureListener { e ->
            println("Error creating game session: $e")
        }
}
