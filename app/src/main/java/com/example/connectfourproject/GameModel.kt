package com.example.connectfourproject



import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow

// Represents a player in the game
data class Player(
    var name: String = "" // Player's name
)

// Represents the game state and board
data class Game(
    var gameBoard: List<Int> = List(rows * cols) { 0 }, // 0 for empty, 1 for player 1, 2 for player 2
    var gameState: String = "invite", // "invite", "player1_turn", "player2_turn", "player1_won", "player2_won", "draw"
    var player1Id: String = "", // Player 1's ID
    var player2Id: String = "" // Player 2's ID
)

// Constants for the game board size
const val rows = 6
const val cols = 7

// ViewModel to manage the game state and database interactions
class GameModel: ViewModel() {
    val db = Firebase.firestore // Firestore instance
    var localPlayerId = mutableStateOf<String?>(null) // Local player's ID
    val playerMap = MutableStateFlow<Map<String, Player>>(emptyMap()) // Real-time map of player IDs to Player objects
    val gameMap = MutableStateFlow<Map<String, Game>>(emptyMap()) // Real-time map of game IDs to Game objects

    // Initialize the game by listening to changes in the database
    fun initGame() {
        // Listen to changes in the "players" collection and update the player map
        db.collection("players")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    val updatedMap = value.documents.associate { doc ->
                        doc.id to doc.toObject(Player::class.java)!! // Convert document to Player object
                    }
                    playerMap.value = updatedMap // Update player map
                }
            }

        // Listen to changes in the "games" collection and update the game map
        db.collection("games")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    val updatedMap = value.documents.associate { doc ->
                        doc.id to doc.toObject(Game::class.java)!! // Convert document to Game object
                    }
                    gameMap.value = updatedMap // Update game map
                }
            }
    }

    // Checks the winner of the game based on the current board state
    fun checkWinner(board: List<Int>): Int {
        // Check rows for 4 consecutive pieces from the same player
        for (row in 0 until rows) {
            for (col in 0..(cols - 4)) {
                val start = row * cols + col
                if (board[start] != 0 &&
                    board[start] == board[start + 1] &&
                    board[start] == board[start + 2] &&
                    board[start] == board[start + 3]
                ) {
                    return board[start] // Return the winner (1 for player 1, or 2 for player 2)
                }
            }
        }

        // Check columns 4 consecutive pieces from the same player
        for (col in 0 until cols) {
            for (row in 0..(rows - 4)) {
                val start = row * cols + col
                if (board[start] != 0 &&
                    board[start] == board[start + cols] &&
                    board[start] == board[start + 2 * cols] &&
                    board[start] == board[start + 3 * cols]
                ) {
                    return board[start] // Return the winner
                }
            }
        }

        // Check diagonals (top-left to bottom-right) for 4 consecutive pieces from the same player
        for (row in 0..(rows - 4)) {
            for (col in 0..(cols - 4)) {
                val start = row * cols + col
                if (board[start] != 0 &&
                    board[start] == board[start + cols + 1] &&
                    board[start] == board[start + 2 * (cols + 1)] &&
                    board[start] == board[start + 3 * (cols + 1)]
                ) {
                    return board[start] // Return the winner
                }
            }
        }

        // Check anti-diagonals (bottom-left to top-right) for 4 consecutive pieces from the same player
        for (row in 0..(rows - 4)) {
            for (col in 3 until cols) {
                val start = row * cols + col
                if (board[start] != 0 &&
                    board[start] == board[start + cols - 1] &&
                    board[start] == board[start + 2 * (cols - 1)] &&
                    board[start] == board[start + 3 * (cols - 1)]
                ) {
                    return board[start] // Return the winner
                }
            }
        }

        // If no empty cells remain, it's a draw
        if (!board.contains(0)) {
            return 3 // Indicate a draw
        }

        // No winner or draw found, game continues
        return 0
    }

    // Updates the game state based after a player makes a move
    fun checkGameState(gameId: String?, cell: Int) {
        if (gameId != null) {
            val game: Game? = gameMap.value[gameId]
            if (game != null) {
                // Check if it's the local player's turn
                val myTurn = (game.gameState == "player1_turn" && game.player1Id == localPlayerId.value) ||
                            (game.gameState == "player2_turn" && game.player2Id == localPlayerId.value)
                if (!myTurn) return // If not the player's turn, do nothing

                // Create a mutable copy of the game board
                val list: MutableList<Int> = game.gameBoard.toMutableList() //
                val col = cell % cols

                // Find the lowest empty cell in the selected column
                var targetCell: Int? = null
                for (row in (rows - 1) downTo 0) {
                    val index = row * cols + col
                    if (list[index] == 0) {
                        targetCell = index
                        break
                    }
                }

                if (targetCell == null) return // No valid move

                // Update the board with the current player's move
                if (game.gameState == "player1_turn") {
                    list[targetCell] = 1 // Player 1's move
                } else if (game.gameState == "player2_turn") {
                    list[targetCell] = 2 // Player 2's move
                }

                // Determine the new game state (win, draw, or next player's turn)
                val winner = checkWinner(list)
                val newState = when (winner) {
                    1 -> "player1_won"
                    2 -> "player2_won"
                    3 -> "draw"
                    else -> if (game.gameState == "player1_turn") "player2_turn" else "player1_turn"
                    }

                // Update the game state and board in the database
                db.collection("games").document(gameId)
                    .update(
                        "gameBoard", list,
                        "gameState", newState
                    )
            }
        }
    }
}