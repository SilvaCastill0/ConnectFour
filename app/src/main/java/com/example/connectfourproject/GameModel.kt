package com.example.connectfourproject


import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow


data class Player(
    var name: String = ""
)

data class Game(
    var gameBoard: List<Int> = List(rows * cols) { 0 },
    var gameState: String = "invite",
    var player1Id: String = "",
    var player2Id: String = ""
)

const val rows = 6
const val cols = 7

class GameModel: ViewModel() {
    val db = Firebase.firestore
    var localPlayerId = mutableStateOf<String?>(null)
    val playerMap = MutableStateFlow<Map<String, Player>>(emptyMap())
    val gameMap = MutableStateFlow<Map<String, Game>>(emptyMap())

    fun initGame() {
        db.collection("players")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    val updatedMap = value.documents.associate { doc ->
                        doc.id to doc.toObject(Player::class.java)!!
                    }
                    playerMap.value = updatedMap
                }
            }

        db.collection("games")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    val updatedMap = value.documents.associate { doc ->
                        doc.id to doc.toObject(Game::class.java)!!
                    }
                    gameMap.value = updatedMap
                }
            }
    }

    fun checkWinner(board: List<Int>): Int {
        for (row in 0 until rows) {
            for (col in 0..(cols - 4)) {
                val start = row * cols + col
                if (board[start] != 0 &&
                    board[start] == board[start + 1] &&
                    board[start] == board[start + 2] &&
                    board[start] == board[start + 3]
                ) {
                    return board[start]
                }
            }
        }

        for (col in 0 until cols) {
            for (row in 0..(rows - 4)) {
                val start = row * cols + col
                if (board[start] != 0 &&
                    board[start] == board[start + cols] &&
                    board[start] == board[start + 2 * cols] &&
                    board[start] == board[start + 3 * cols]
                ) {
                    return board[start]
                }
            }
        }


        for (row in 0..(rows - 4)) {
            for (col in 0..(cols - 4)) {
                val start = row * cols + col
                if (board[start] != 0 &&
                    board[start] == board[start + cols + 1] &&
                    board[start] == board[start + 2 * (cols + 1)] &&
                    board[start] == board[start + 3 * (cols + 1)]
                ) {
                    return board[start]
                }
            }
        }


        for (row in 0..(rows - 4)) {
            for (col in 3 until cols) {
                val start = row * cols + col
                if (board[start] != 0 &&
                    board[start] == board[start + cols - 1] &&
                    board[start] == board[start + 2 * (cols - 1)] &&
                    board[start] == board[start + 3 * (cols - 1)]
                ) {
                    return board[start]
                }
            }
        }

        if (!board.contains(0)) {
            return 3
        }


        return 0
    }


    fun checkGameState(gameId: String?, cell: Int) {
        if (gameId != null) {
            val game: Game? = gameMap.value[gameId]
            if (game != null) {
                val myTurn = (game.gameState == "player1_turn" && game.player1Id == localPlayerId.value) ||
                            (game.gameState == "player2_turn" && game.player2Id == localPlayerId.value)
                if (!myTurn) return

                val list: MutableList<Int> = game.gameBoard.toMutableList()

                val col = cell % cols

                var targetCell: Int? = null
                for (row in (rows - 1) downTo 0) {
                    val index = row * cols + col
                    if (list[index] == 0) {
                        targetCell = index
                        break
                    }
                }

                if (targetCell == null) return

                if (game.gameState == "player1_turn") {
                    list[targetCell] = 1
                } else if (game.gameState == "player2_turn") {
                    list[targetCell] = 2
                }

                    val winner = checkWinner(list)
                    val newState = when (winner) {
                        1 -> "player1_won"
                        2 -> "player2_won"
                        3 -> "draw"
                        else -> if (game.gameState == "player1_turn") "player2_turn" else "player1_turn"
                    }

                db.collection("games").document(gameId)
                    .update(
                        "gameBoard", list,
                        "gameState", newState
                    )
            }
        }
    }
}