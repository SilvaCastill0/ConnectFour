package com.example.connectfourproject

import android.annotation.SuppressLint
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
            savePlayer(playerName)
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
                            Button(onClick = { /*TODO*/ }) {
                                Text(text = "Invite")
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
        fetchPlayer { players ->
            playerNames.value = players
        }
    }

    LobbyScreen(playerNames = playerNames.value)
}


fun savePlayer(playerName: String) {
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
        }
        .addOnFailureListener { e ->
            println("Error adding player: $e")
        }
}

fun fetchPlayer(onPlayerFetched: (List<Player>) -> Unit) {
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

