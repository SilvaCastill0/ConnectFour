package com.example.connectfourproject

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Player(
    var name: String = "",
    var challenge: String? = null,
    var gameSessionId: String? = null
)

data class Game(
    var gameBoard: List<List<Int>> = List(6) { List(7) { 0 } },
    var gameState: String = "player1_turn",
    var player1Id: String = "",
    var player2Id: String = ""
)

class GameModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _playerMap = MutableStateFlow<Map<String, Player>>(emptyMap())
    val playerMap: StateFlow<Map<String, Player>> = _playerMap

    private val _gameMap = MutableStateFlow<Map<String, Game>>(emptyMap())
    val gameMap: StateFlow<Map<String, Game>> = _gameMap

    val localPlayerId = mutableStateOf<String?>(null)

    fun init() {
        db.collection("Players").addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                _playerMap.value = it.documents.associate { doc ->
                    doc.id to doc.toObject(Player::class.java)!!
                }
            }
        }

        db.collection("gameSessions").addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                _gameMap.value = it.documents.associate { doc ->
                    doc.id to doc.toObject(Game::class.java)!!
                }
            }
        }
    }

    fun registerPlayer(playerName: String) {
        val playerId = "$playerName-${System.currentTimeMillis()}"
        val newPlayer = Player(name = playerName)
        localPlayerId.value = playerId

        db.collection("Players").document(playerId).set(newPlayer)
    }

    fun createChallenge(opponentId: String) {
        val playerId = localPlayerId.value ?: return
        val challenge = mapOf(
            "challengerId" to playerId,
            "challengedId" to opponentId,
            "status" to "pending"
        )

        db.collection("Challenges").add(challenge)
            .addOnSuccessListener { documentReference ->
                println("Challenge sent successfully.")
            }
            .addOnFailureListener { e ->
                println("Error sending challenge.")
            }
    }

    fun listenForChallenges(onChallengeReceived: (String, String) -> Unit) {
        val playerId = localPlayerId.value ?: return

        db.collection("Challenges")
            .whereEqualTo("challengedId", playerId)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("Error listening for challenges: $e")
                    return@addSnapshotListener
                }

                snapshot?.documents?.firstOrNull()?.let { document ->
                    val challengerId = document.getString("challengerId") ?: return@let
                    val challengeId = document.id
                    println("Challenge received: challengerId: $challengerId, challengeId: $challengeId")
                    onChallengeReceived(challengerId, challengeId)
                }
            }
    }

    fun acceptChallenge(challengeId: String, context: Context) {
        val gameSessionId = "game-${System.currentTimeMillis()}"

        db.collection("Challenges").document(challengeId).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    println("Challenge document not found for ID: $challengeId")
                    return@addOnSuccessListener
                }
                println("Challenge document exists for ID: $challengeId")

                db.collection("Challenges").document(challengeId)
                    .update("status", "accepted", "gameSessionId", gameSessionId)
                    .addOnSuccessListener {
                        println("Challenge accepted and game session ID added.")
                    }
                    .addOnFailureListener { e ->
                        println("Error updating challenge: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                println("Error fetching challenge document: ${e.message}")
            }


        /*
        val gameSession = mapOf(
            "gameBoard" to List(6) { List(7) { 0 } }, // Empty 6x7 board
            "gameState" to "player1_turn" // Initial turn state
        )

        println("Checking if challenge exists with ID: $challengeId")

        // Step 1: Verify if the challenge document exists
        db.collection("Challenges").document(challengeId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    println("Challenge document exists with ID: $challengeId")

                    // Step 2: Update the challenge status to "accepted"
                    db.collection("Challenges").document(challengeId)
                        .update("status", "accepted", "gameSessionId", gameSessionId)
                        .addOnSuccessListener {
                            println("Challenge status updated to accepted.")

                            // Step 3: Create a new game session in Firebase
                            db.collection("gameSessions").document(gameSessionId)
                                .set(gameSession)
                                .addOnSuccessListener {
                                    println("Game session created successfully with ID $gameSessionId")

                                    // Step 4: Update players with the game session ID
                                    db.collection("Challenges").document(challengeId).get()
                                        .addOnSuccessListener { document ->
                                            val challengerId = document.getString("challengerId") ?: return@addOnSuccessListener
                                            val challengedId = document.getString("challengedId") ?: return@addOnSuccessListener

                                            db.collection("Players").document(challengerId)
                                                .update("gameSessionId", gameSessionId)
                                            db.collection("Players").document(challengedId)
                                                .update("gameSessionId", gameSessionId)

                                            // Step 5: Navigate to GameActivity
                                            val intent = Intent(context, GameActivity::class.java)
                                            intent.putExtra("gameId", gameSessionId)
                                            context.startActivity(intent)
                                        }
                                        .addOnFailureListener { e ->
                                            println("Error fetching challenge details: ${e.message}")
                                        }
                                }
                                .addOnFailureListener { e ->
                                    println("Error creating game session: ${e.message}")
                                }
                        }
                        .addOnFailureListener { e ->
                            println("Error updating challenge status: ${e.message}")
                        }
                } else {
                    println("Challenge document not found for ID: $challengeId")
                }
            }
            .addOnFailureListener { e ->
                println("Error fetching challenge document: ${e.message}")
            }

         */
    }

    fun declineChallenge(challengeId: String) {
        db.collection("Challenges").document(challengeId)
            .update("status", "declined")
            .addOnSuccessListener { println("Challenge declined.") }
            .addOnFailureListener { println("Error declining challenge.") }
    }

    fun listenForGameUpdates(gameId: String, onUpdate: (Game) -> Unit) {
        db.collection("gameSessions").document(gameId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("Error listening for game updates: $e")
                    return@addSnapshotListener
                }

                snapshot?.toObject(Game::class.java)?.let { game ->
                    println("Game updated: $game")
                    onUpdate(game)
                }
            }
    }



    fun makeMove(gameId: String, row: Int, col: Int) {
        val game = _gameMap.value[gameId] ?: return
        val currentPlayer = if (game.gameState == "player1_turn") 1 else 2

        if (game.gameBoard[row][col] == 0) {
            val updatedBoard = game.gameBoard.map { it.toMutableList() }.toMutableList()
            updatedBoard[row][col] = currentPlayer

            val newState = if (checkWin(updatedBoard, row, col, currentPlayer)) {
                if (currentPlayer == 1) "player1_won" else "player2_won"
            } else {
                if (game.gameState == "player1_turn") "player2_turn" else "player1_turn"
            }

            db.collection("gameSessions").document(gameId)
                .update("gameBoard", updatedBoard, "gameState", newState)
                .addOnSuccessListener {
                    println("Move made successfully.")
                    // Update the local game map
                    _gameMap.value = _gameMap.value.toMutableMap().apply {
                        this[gameId] = game.copy(
                            gameBoard = updatedBoard,
                            gameState = newState
                        )
                    }
                }
                .addOnFailureListener { e ->
                    println("Error making move: ${e.message}")
                }
        }
    }

    private fun checkWin(board: List<List<Int>>, row: Int, col: Int, player: Int): Boolean {
        // Horizontal check
        if ((0..board[0].size - 4).any { colStart ->
                (colStart until colStart + 4).all { board[row][it] == player }
            }) return true

        // Vertical check
        if ((0..board.size - 4).any { rowStart ->
                (rowStart until rowStart + 4).all { board[it][col] == player }
            }) return true

        // Diagonal check (top-left to bottom-right)
        if ((-3..0).any { offset ->
                val startRow = row + offset
                val startCol = col + offset
                (0..3).all { i ->
                    val r = startRow + i
                    val c = startCol + i
                    r in board.indices && c in board[0].indices && board[r][c] == player
                }
            }) return true

        // Diagonal check (top-right to bottom-left)
        if ((-3..0).any { offset ->
                val startRow = row + offset
                val startCol = col - offset
                (0..3).all { i ->
                    val r = startRow + i
                    val c = startCol - i
                    r in board.indices && c in board[0].indices && board[r][c] == player
                }
            }) return true

        return false
    }

}
