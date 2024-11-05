package com.example.connectfourproject

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {

    private var isPlayer1Turn = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        setupGameGrid()

    }

    fun setupGameGrid(){
        val gamePieces = Array(6) { row ->
            Array(7) { col ->
                val pieceId = resources.getIdentifier("piece_${row}_${col}", "id",
                    packageName)
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
        if (gamePiece.paint.color == Color.GRAY) {
            if (isPlayer1Turn){
                gamePiece.setPlayer1()
            }
            else {
                gamePiece.setPlayer2()
            }
            isPlayer1Turn = !isPlayer1Turn
        }
    }
}