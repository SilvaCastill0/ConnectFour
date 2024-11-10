package com.example.connectfourproject

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Modifier
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.content.ContextCompat.startActivity


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
                context.startActivity(intent)
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

    Box(modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = R.drawable.backmain),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = 700f },
            contentPadding = PaddingValues(8.dp)
        ) {
            items(gameBoard.value.flatten().size) { index ->
                val row = index / 7
                val col = index % 7
                Button(
                    onClick = {
                        if(dropPiece(gameBoard, col, currentPlayer)) {
                            currentPlayer.value = if (currentPlayer.value == 1) 2 else 1
                        }
                    },
                    modifier = Modifier
                        .padding(4.dp)
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
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
}

fun dropPiece(gameBoard: MutableState<Array<Array<Int>>>, col: Int, currentPlayer: MutableState<Int>): Boolean {
    for (row in gameBoard.value.size - 1 downTo 0) {
        if (gameBoard.value[row][col] == 0) {
            val updatedBoard = gameBoard.value.map { it.copyOf() }.toTypedArray()
            updatedBoard[row][col] = currentPlayer.value
            gameBoard.value = updatedBoard
            return true
        }
    }
    return false
}



@Preview(showBackground = true)
@Composable
fun PreviewGameGrid() {
    GameScreen()
}


/*
var isPlayer1Turn = true

class GameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);

        setupGameGrid()

    }

    fun setupGameGrid() {
        val gamePieces = Array(6) { row ->
            Array(7) { col ->
                val pieceId = resources.getIdentifier(
                    "piece_${row}_${col}", "id",
                    packageName
                )
                val gamePiece = findViewById<GamePieceView>(pieceId)

                gamePiece?.let {
                    it.setOnClickListener { view ->
                        onGamePieceClicked(view as GamePieceView)
                    }
                }
                gamePiece
            }
        }
    }

    fun onGamePieceClicked(gamePiece: GamePieceView) {

        val col = getColumnFromId(gamePiece.id)
        val rowCount = 6

        for(row in rowCount - 1 downTo 0) {
            val pieceId = resources.getIdentifier(
                "piece_${row}_${col}", "id",
                packageName
            )
            val gamePiece = findViewById<GamePieceView>(pieceId)

            if (gamePiece != null && gamePiece.paint.color == Color.GRAY) {
                if (isPlayer1Turn) {
                    gamePiece.setPlayer1()
                } else {
                    gamePiece.setPlayer2()
                }
                isPlayer1Turn = !isPlayer1Turn
                break
            }
        }
    }

    fun getColumnFromId(pieceId: Int): Int {
        val resourceName = resources.getResourceName(pieceId)
        val parts = resourceName.split("_")
        return parts[2].toInt()

    }
}
 */