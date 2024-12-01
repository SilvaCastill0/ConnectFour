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

    fun checkHor(board: List<Int>, rows: Int, cols: Int): Boolean {
        for (row in 0 until rows) {
            for (col in 0..(cols - 4)) {
                val index = row * cols + col
                val value = board[index]
                if (value != 0 &&
                    value == board[index + 1] &&
                    value == board[index + 2] &&
                    value == board[index + 3]
                    ) {
                    return true
                }

            }
        }
        return false
    }

    fun checkVer(board: List<Int>, rows: Int, cols: Int): Boolean {
        for (col in 0 until cols) {
            for (row in 0..(rows - 4)) {
                val index = row * cols + col
                val value = board[index]
                if (value != 0 &&
                    value == board[index + cols] &&
                    value == board[index + 2 * cols] &&
                    value == board[index + 3 * cols]
                ) {
                    return true
                }
            }
        }
        return false
    }

    fun checkDiagonalTLBR(board: List<Int>, rows: Int, cols: Int): Boolean {
        for (row in 0..(rows - 4)) {
            for (col in 0..(cols - 4)) {
                val index = row * cols + col
                val value = board[index]
                if (value != 0 &&
                    value == board[index + cols + 1] &&
                    value == board[index + 2 * (cols + 1)] &&
                    value == board[index + 3 * (cols + 1)]
                ) {
                    return true
                }
            }
        }
        return false
    }

    fun checkDiagonalTRBL(board: List<Int>, rows: Int, cols: Int): Boolean {
        for (row in 0..(rows - 4)) {
            for (col in 3 until cols) {
                val index = row * cols + col
                val value = board[index]
                if (value != 0 &&
                    value == board[index + cols - 1] &&
                    value == board[index + 2 * (cols - 1)] &&
                    value == board[index + 3 * (cols - 1)]
                ) {
                    return true
                }
            }
        }
        return false
    }

    fun checkWinner(board: List<Int>, rows: Int, cols: Int): Int {
        return when {
            checkHor(board, rows, cols) -> 1
            checkVer(board, rows, cols) -> 1
            checkDiagonalTLBR(board, rows, cols) -> 1
            checkDiagonalTRBL(board, rows, cols) -> 1
            else -> 0
        }
    }

    fun isDraw(board: List<Int>): Boolean {
        return board.none { it == 0 }
    }


    fun checkGameState(gameId: String?, cell: Int) {
        if (gameId != null) {
            val game: Game? = gameMap.value[gameId]
            if (game != null) {
                val myTurn = (game.gameState == "player1_turn" && game.player1Id == localPlayerId.value) ||
                            (game.gameState == "player2_turn" && game.player2Id == localPlayerId.value)
                if (!myTurn) return

                val list = game.gameBoard.toMutableList()

                if (list[cell] != 0) return

                list[cell] = if (game.gameState == "player1_turn") 1 else 2

                var turn = if (game.gameState == "player1_turn") "player2_turn" else "player1_turn"

                val winner = checkWinner(list, rows, cols)
                if (winner == 1) {
                    turn = "player1_won"
                } else if (winner == 2) {
                    turn = "player2_won"
                } else if (isDraw(list)) {
                    turn = "draw"
                }

                db.collection("games").document(gameId)
                    .update(
                        "gameBoard", list,
                        "gameState", turn
                    )
                    .addOnFailureListener{ error ->
                        Log.e("Error", "Error updating game: ${error.message}")
                    }
            }
        }
    }


}