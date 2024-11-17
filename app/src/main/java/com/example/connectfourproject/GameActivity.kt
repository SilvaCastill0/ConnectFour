package com.example.connectfourproject

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp


class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent{
            GameScreen()
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun GameScreen() {
    Scaffold(
        topBar = { TopBarBackButton() },
    ) {
        GameGrid()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarBackButton() {
    val context = LocalContext.current

    androidx.compose.material3.TopAppBar(
        title = {
            Text(text = "Connect Four")
        },
        navigationIcon = {
            IconButton(onClick = {
                val intent = Intent(context, MainActivity::class.java)
                (context as Activity).startActivity(intent)
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"

                )
            }
        }
    )
}

@Composable
fun GameGrid() {
    val gameBoard = remember { mutableStateOf(Array(6) { Array(7) { 0 } }) }
    val currentPlayer = remember { mutableStateOf(1) }
    val winDetected = remember { mutableStateOf(0) }


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
                                    } else {
                                        currentPlayer.value = if (currentPlayer.value == 1) 2 else 1
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
            ShowWinMessage(currentPlayer)
        }
    }
}

@Composable
fun ShowWinMessage(currentPlayer: MutableState<Int>) {

    Text(

    val context = LocalContext.current
    LaunchedEffect(currentPlayer.value) {
        Toast.makeText(context, "Player ${currentPlayer.value} wins!", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun DrawWinMessage() {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        Toast.makeText(context, "Draw!", Toast.LENGTH_SHORT).show()
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewGameGrid() {
    GameScreen()
}
