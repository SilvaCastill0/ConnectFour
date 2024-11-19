package com.example.connectfourproject

import androidx.compose.runtime.MutableState
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

fun CheckHor(gameBoard: MutableState<Array<Array<Int>>>, row: Int, currentPlayer: MutableState<Int>): Boolean{
    var count = 0
    for (col in 0 until gameBoard.value[row].size) {
        if (gameBoard.value[row][col] == currentPlayer.value) {
            count++
            if (count == 4) return true
        } else {
            count = 0
        }
    }
    return false
}

fun CheckVer(gameBoard: MutableState<Array<Array<Int>>>, col: Int, currentPlayer: MutableState<Int>): Boolean {
    var count = 0
    for (row in 0 until gameBoard.value.size) {
        if (gameBoard.value[row][col] == currentPlayer.value) {
            count++
            if (count == 4) return true
        } else {
            count = 0
        }
    }
    return false
}

fun CheckDiag(gameBoard: MutableState<Array<Array<Int>>>,row: Int, col: Int, currentPlayer: MutableState<Int>): Boolean {
    var count = 0

    // Check diagonal from top-left to bottom-right
    count = 0
    for(i in -3..3) {
        val r = row + i
        val c = col + i
        if (r in gameBoard.value.indices && c in gameBoard.value[row].indices && gameBoard.value[r][c] == currentPlayer.value) {
            count++
            if (count == 4) return true
        }else {
            count = 0
        }
    }

    // Check diagonal from top-right to bottom-left
    count = 0
    for(i in -3..3) {
        val r = row + i
        val c = col - i
        if (r in gameBoard.value.indices && c in gameBoard.value[row].indices && gameBoard.value[r][c] == currentPlayer.value) {
            count++
            if (count == 4) return true
        }else {
            count = 0
        }
    }
    return false
}

fun CheckDraw(gameBoard: MutableState<Array<Array<Int>>>): Boolean {
    for (row in gameBoard.value) {
        for (cell in row) {
            if (cell == 0) return false
        }
    }
    return true
}


fun WinCheck(gameBoard: MutableState<Array<Array<Int>>>, row: Int, col: Int, currentPlayer: MutableState<Int>): Boolean {
    return CheckHor(gameBoard, row, currentPlayer) ||
            CheckVer(gameBoard, col, currentPlayer) ||
            CheckDiag(gameBoard, row, col, currentPlayer)
}



fun dropPiece(gameBoard: MutableState<Array<Array<Int>>>, col: Int, currentPlayer: MutableState<Int>): Int? {
    for (row in gameBoard.value.size - 1 downTo 0) {
        if (gameBoard.value[row][col] == 0) {
            val updatedBoard = gameBoard.value.map { it.copyOf() }.toTypedArray()
            updatedBoard[row][col] = currentPlayer.value
            gameBoard.value = updatedBoard
            return row
        }
    }
    return null
}

fun syncGameBoard(
    gameSessionId: String,
    gameBoard: MutableState<Array<Array<Int>>>,
    currentPlayer: MutableState<Int>,
) {
    val db = Firebase.firestore
    db.collection("gameSessions").document(gameSessionId)
        .addSnapshotListener { snapshot, e ->
            if (e != null) {
                println("Listen failed: $e")
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val board = snapshot.get("board") as? List<List<Long>>
                val currentPlayerTurn = snapshot.getLong("currentPlayer")?.toInt()

                if (board != null && currentPlayerTurn != null) {
                    gameBoard.value =
                        board.map { it.map { it.toInt() }.toTypedArray() }.toTypedArray()
                    currentPlayer.value = currentPlayerTurn
                }
            }
        }
}

fun updateGameBoard(
    gameSessionId: String,
    gameBoard: Array<Array<Int>>,
    currentPlayer: Int
) {
    val db = Firebase.firestore
    db.collection("gameSessions").document(gameSessionId)
        .update("board", gameBoard.map { it.toList() }, "currentPlayer", currentPlayer)
        .addOnSuccessListener{
            println("Game board updated successfully")
        }
        .addOnFailureListener { e ->
            println("Error updating game board: $e")
        }
}