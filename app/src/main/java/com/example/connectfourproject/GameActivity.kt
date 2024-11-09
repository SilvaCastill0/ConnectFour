package com.example.connectfourproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.draw.clip


class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent{
            GameGrid()
        }
    }
}


@Composable
fun GameGrid() {
    val buttonsLabels = List(42) { false }

    Box(modifier = Modifier.fillMaxWidth()) {
        AndroidView(
            factory = { context ->
                LayoutInflater.from(context).inflate(R.layout.activity_game, null)
            },
            modifier = Modifier.fillMaxWidth()
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(buttonsLabels.size) { index ->
                val row = index / 7
                val col = index % 7
                Button(
                    onClick = {},
                    modifier = Modifier
                        .padding(4.dp)
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(Color.Red, shape = CircleShape)
                        )
                    }
                }
            }
        }
    }
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