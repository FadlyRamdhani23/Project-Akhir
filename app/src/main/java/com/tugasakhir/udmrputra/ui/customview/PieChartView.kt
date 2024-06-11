package com.tugasakhir.udmrputra.ui.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private var fruitWeight: Float = 0f
    private var vegetableWeight: Float = 0f

    fun setData(fruitWeight: Float, vegetableWeight: Float) {
        this.fruitWeight = fruitWeight
        this.vegetableWeight = vegetableWeight
        invalidate() // Request to redraw the view
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val totalWeight = fruitWeight + vegetableWeight
        if (totalWeight == 0f) return

        val fruitAngle = 360f * (fruitWeight / totalWeight)
        val vegetableAngle = 360f * (vegetableWeight / totalWeight)

        val width = width.toFloat()
        val height = height.toFloat()
        val radius = Math.min(width, height) / 2f

        // Draw Fruit Segment
        paint.color = Color.rgb(92, 132, 161)
        canvas.drawArc(0f, 0f, width, height, 0f, fruitAngle, true, paint)

        // Draw Vegetable Segment
        paint.color = Color.rgb(132, 179, 167)
        canvas.drawArc(0f, 0f, width, height, fruitAngle, vegetableAngle, true, paint)

        // Draw text on Fruit Segment
        paint.color = Color.WHITE
        paint.textSize = 20f
        val fruitText = "$fruitWeight kg\nBuah"
        drawCenteredText(canvas, fruitText, width / 3, height / 3)

        // Draw text on Vegetable Segment
        val vegetableText = "$vegetableWeight kg\nSayur"
        drawCenteredText(canvas, vegetableText, 2 * width / 3, 2 * height / 3)
    }

    private fun drawCenteredText(canvas: Canvas, text: String, cx: Float, cy: Float) {
        val lines = text.split("\n")
        val textHeight = paint.descent() - paint.ascent()
        val textOffset = textHeight / 2 - paint.descent()
        lines.forEachIndexed { index, line ->
            val y = cy + index * textHeight + textOffset
            canvas.drawText(line, cx - paint.measureText(line) / 2, y, paint)
        }
    }
}