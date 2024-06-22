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
        val centerX = width / 2f
        val centerY = height / 2f

        // Draw Fruit Segment
        paint.color = Color.rgb(92, 132, 161)
        canvas.drawArc(centerX - radius, centerY - radius, centerX + radius, centerY + radius, 0f, fruitAngle, true, paint)

        // Draw Vegetable Segment
        paint.color = Color.rgb(132, 179, 167)
        canvas.drawArc(centerX - radius, centerY - radius, centerX + radius, centerY + radius, fruitAngle, vegetableAngle, true, paint)

        // Draw text on Fruit Segment
        paint.color = Color.WHITE
        paint.textSize = 30f
        val fruitText = "${fruitWeight.toInt()} kg\nBuah"
        drawSegmentText(canvas, fruitText, centerX, centerY, radius, fruitAngle / 2)

        // Draw text on Vegetable Segment
        val vegetableText = "${vegetableWeight.toInt()} kg\nSayur"
        drawSegmentText(canvas, vegetableText, centerX, centerY, radius, fruitAngle + vegetableAngle / 2)
    }

    private fun drawSegmentText(canvas: Canvas, text: String, cx: Float, cy: Float, radius: Float, angle: Float) {
        val lines = text.split("\n")
        val textHeight = paint.descent() - paint.ascent()
        val textOffset = textHeight / 2 - paint.descent()

        val angleRadians = Math.toRadians(angle.toDouble())
        val x = cx + (radius / 2 * Math.cos(angleRadians)).toFloat()
        val y = cy + (radius / 2 * Math.sin(angleRadians)).toFloat()

        lines.forEachIndexed { index, line ->
            val lineY = y + index * textHeight + textOffset
            canvas.drawText(line, x - paint.measureText(line) / 2, lineY, paint)
        }
    }
}
