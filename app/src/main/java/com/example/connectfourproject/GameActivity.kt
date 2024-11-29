package com.example.connectfourproject

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign


class GameActivity : ComponentActivity() {
    private lateinit var gameSessionId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameSessionId = intent.getStringExtra("gameSessionId") ?: ""
        if(gameSessionId.isEmpty()) {
            println("Invalid game session ID")
            finish()
        }

        setContent{
            GameScreen(gameSessionId)
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun GameScreen(gameSessionId: String) {
    val gameBoard = remember { mutableStateOf(List(6) { MutableList(7) { 0 } }) }
    val currentPlayer = remember { mutableStateOf(1) }
    val winDetected = remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        syncGameBoard(gameSessionId, gameBoard, currentPlayer)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Game UI components
        if (winDetected.value == 0) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                contentPadding = PaddingValues(4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(gameBoard.value.flatten().size) { index ->
                    val row = index / 7
                    val col = index % 7

                    Button(
                        onClick = {
                            val updatedRow = dropPiece(gameBoard, col, currentPlayer)
                            if (updatedRow != null) {
                                if (WinCheck(gameBoard, updatedRow, col, currentPlayer)) {
                                    winDetected.value = currentPlayer.value
                                } else {
                                    currentPlayer.value = if (currentPlayer.value == 1) 2 else 1
                                    updateGameBoard(gameSessionId, gameBoard.value, currentPlayer.value)
                                }
                            }
                        },
                        modifier = Modifier.padding(4.dp)
                    ) {
                        when (gameBoard.value[row][col]) {
                            1 -> Text("R") // Red
                            2 -> Text("Y") // Yellow
                            else -> Text("")
                        }
                    }
                }
            }
        }
    }
}

/*
@Composable
fun GameGrid(gameSessionId: String) {
    val gameBoard = remember { mutableStateOf(Array(6) { Array(7) { 0 } }) }
    val currentPlayer = remember { mutableStateOf(1) }
    val winDetected = remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        syncGameBoard(gameSessionId, gameBoard, currentPlayer)
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.backmain),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        if (winDetected.value == 0) {
        Text(
            text = "Player ${currentPlayer.value}'s turn",
            color = if (currentPlayer.value == 1) Color.Red else Color.Yellow,
            fontSize = 24.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .graphicsLayer { translationY = 600f }
                .padding(top = 16.dp)
        )
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationY = 700f }
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(gameBoard.value.flatten().size) { index ->
                    val row = index / 7
                    val col = index % 7

                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(
                                if (gameBoard.value[row][col] == 1) Color.Red
                                else if (gameBoard.value[row][col] == 2) Color.Yellow
                                else Color.Gray
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                val row = dropPiece(gameBoard, col, currentPlayer)
                                if (row != null) {
                                    if (WinCheck(gameBoard, row, col, currentPlayer)) {
                                        winDetected.value = 1
                                    } else if(CheckDraw(gameBoard)) {
                                        winDetected.value = 2
                                    } else {
                                        currentPlayer.value = if (currentPlayer.value == 1) 2 else 1
                                        updateGameBoard(gameSessionId, gameBoard.value, currentPlayer.value)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            when (gameBoard.value[row][col]) {
                                1 -> Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(Color.Red, shape = CircleShape)
                                )

                                2 -> Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(Color.Yellow, shape = CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }
        else if (winDetected.value == 1) {
            PopUpWinner(currentPlayer, onDismissRequest = { winDetected.value = 0 })
        }
        else if (winDetected.value == 2) {
            PopUpDraw(onDismissRequest = { winDetected.value = 0 })
        }
    }
}
 */

@Composable
fun PopUpWinner(currentPlayer: MutableState<Int>, onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Player ${currentPlayer.value} wins!",
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PopUpDraw(onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Draw!",
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                textAlign = TextAlign.Center
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewGameGrid() {
    GameScreen(gameSessionId = "")
}
