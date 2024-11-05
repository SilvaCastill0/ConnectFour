package com.example.connectfourproject

import android.content.Context
import android.util.AttributeSet
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class GamePieceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): View(context, attrs, defStyleAttr)  {

    val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    init {
        setEmpty()
    }

    fun setEmpty() {
        paint.color = Color.GRAY
        invalidate()
    }

    fun setPlayer1() {
        paint.color = Color.RED
        invalidate()
    }

    fun setPlayer2() {
        paint.color = Color.YELLOW
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius = width.coerceAtMost(height) / 2f
        canvas.drawCircle(width / 2f, height / 2f, radius, paint)
    }

}