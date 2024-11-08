package com.example.connectfourproject

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

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