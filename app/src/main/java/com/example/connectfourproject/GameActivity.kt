package com.example.connectfourproject

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.firebase.firestore.FirebaseFirestore



class GameActivity : ComponentActivity() {
    private lateinit var gameSessionId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        gameSessionId = intent.getStringExtra("gameSessionId") ?: ""
        setContent {
            GameScreen(gameSessionId, this)
        }
    }
}

@Composable
fun GameScreen(gameSessionId: String, context: Context) {
    val db = FirebaseFirestore.getInstance()
    val gameBoard = remember { mutableStateOf(Array(6) { Array(7) { 0 } }) }
    val currentPlayer = remember { mutableStateOf("") }
    val gameStatus = remember { mutableStateOf("active") }

    LaunchedEffect(gameSessionId) {
        db.collection("gameSessions").document(gameSessionId)
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null && snapshot.exists()) {
                    val board = snapshot.get("board") as? List<List<Long>>
                    val currentTurn = snapshot.getString("currentPlayer")
                    val status = snapshot.getString("status")

                    if (board != null && currentTurn != null) {
                        gameBoard.value = board.map { it.map { it.toInt() }.toTypedArray() }.toTypedArray()
                        currentPlayer.value = currentTurn
                    }
                    if (status != null) {
                        gameStatus.value = status
                    }
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = if (currentPlayer.value == getCurrentPlayerId(context)) "Your Turn!" else "Opponent's Turn!",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxSize()) {
            items(gameBoard.value.flatten().size) { index ->
                val row = index / 7
                val col = index % 7

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .padding(4.dp)
                        .background(
                            when (gameBoard.value[row][col]) {
                                1 -> Color.Red
                                2 -> Color.Yellow
                                else -> Color.Gray
                            }
                        )
                ) {
                    Button(
                        onClick = {
                            if (currentPlayer.value == getCurrentPlayerId(context)) {
                                makeMove(row, col, gameSessionId, gameBoard, context)
                            }
                        },
                        enabled = gameBoard.value[row][col] == 0,
                        modifier = Modifier.fillMaxSize()
                    ) {}
                }
            }
        }

        if (gameStatus.value == "draw") {
            PopUpDraw { /* Navigate back or restart */ }
        } else if (gameStatus.value == "win") {
            PopUpWinner(winner = "Player ${if (currentPlayer.value == "player1Id") 1 else 2}") {
                /* Navigate back or restart */
            }
        }
    }
}

fun makeMove(row: Int, col: Int, gameSessionId: String, gameBoard: MutableState<Array<Array<Int>>>, context: Context) {
    val db = FirebaseFirestore.getInstance()

    val updatedBoard = gameBoard.value.map { it.copyOf() }.toTypedArray()
    for (r in updatedBoard.size - 1 downTo 0) {
        if (updatedBoard[r][col] == 0) {
            updatedBoard[r][col] = if (getCurrentPlayerId(context) == "player1Id") 1 else 2
            break
        }
    }

    db.collection("gameSessions").document(gameSessionId)
        .update(
            "board", updatedBoard.map { it.toList() },
            "currentPlayer", if (getCurrentPlayerId(context) == "player1Id") "player2Id" else "player1Id"
        )
}

@Composable
fun PopUpDraw(onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("It's a Draw!", style = MaterialTheme.typography.headlineMedium)
                Button(onClick = { onDismissRequest() }) {
                    Text("OK")
                }
            }
        }
    }
}

@Composable
fun PopUpWinner(winner: String, onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("$winner Wins!", style = MaterialTheme.typography.headlineMedium)
                Button(onClick = { onDismissRequest() }) {
                    Text("OK")
                }
            }
        }
    }
}

