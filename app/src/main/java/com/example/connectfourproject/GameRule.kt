package com.example.connectfourproject

import androidx.compose.runtime.MutableState

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