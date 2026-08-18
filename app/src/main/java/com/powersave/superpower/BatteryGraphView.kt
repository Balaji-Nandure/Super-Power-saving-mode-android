package com.powersave.superpower

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BatteryGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var dataPoints: List<BatteryDataPoint> = emptyList()

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#222222")
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#777777")
        textSize = 26f
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00E676")
        strokeWidth = 6f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00E676")
        style = Paint.Style.FILL
    }

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun setData(points: List<BatteryDataPoint>) {
        this.dataPoints = points
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val paddingLeft = 60f
        val paddingRight = 30f
        val paddingTop = 30f
        val paddingBottom = 50f

        val graphWidth = w - paddingLeft - paddingRight
        val graphHeight = h - paddingTop - paddingBottom

        // 1. Draw Grid Lines (100%, 75%, 50%, 25%, 0%)
        val levels = listOf(100, 75, 50, 25, 0)
        for (lvl in levels) {
            val y = paddingTop + graphHeight * (1f - (lvl / 100f))
            canvas.drawLine(paddingLeft, y, w - paddingRight, y, gridPaint)
            canvas.drawText("${lvl}%", 8f, y + 8f, textPaint)
        }

        if (dataPoints.size < 2) return

        // 2. Compute Coordinate Points
        val minTime = dataPoints.first().timestamp
        val maxTime = dataPoints.last().timestamp
        val timeSpan = (maxTime - minTime).coerceAtLeast(1L).toFloat()

        val coords = mutableListOf<Pair<Float, Float>>()
        for (p in dataPoints) {
            val normX = (p.timestamp - minTime).toFloat() / timeSpan
            val normY = p.level / 100f

            val x = paddingLeft + (normX * graphWidth)
            val y = paddingTop + graphHeight * (1f - normY)
            coords.add(Pair(x, y))
        }

        // 3. Build Smooth Path
        val path = Path()
        val fillPath = Path()

        path.moveTo(coords[0].first, coords[0].second)
        fillPath.moveTo(coords[0].first, paddingTop + graphHeight)
        fillPath.lineTo(coords[0].first, coords[0].second)

        for (i in 0 until coords.size - 1) {
            val p0 = coords[i]
            val p1 = coords[i + 1]
            val midX = (p0.first + p1.first) / 2f
            val midY = (p0.second + p1.second) / 2f

            path.quadTo(p0.first, p0.second, midX, midY)
            fillPath.quadTo(p0.first, p0.second, midX, midY)
        }

        val last = coords.last()
        path.lineTo(last.first, last.second)
        fillPath.lineTo(last.first, last.second)
        fillPath.lineTo(last.first, paddingTop + graphHeight)
        fillPath.close()

        // 4. Draw Gradient Area under curve
        val gradient = LinearGradient(
            0f, paddingTop, 0f, paddingTop + graphHeight,
            Color.parseColor("#3300E676"), Color.parseColor("#00000000"),
            Shader.TileMode.CLAMP
        )
        fillPaint.shader = gradient
        canvas.drawPath(fillPath, fillPaint)

        // 5. Draw Curve
        canvas.drawPath(path, linePaint)

        // 6. Draw Data Points & Timestamps
        for (i in coords.indices) {
            val c = coords[i]
            val point = dataPoints[i]

            pointPaint.color = if (point.isCharging) Color.parseColor("#00E5FF") else Color.parseColor("#00E676")
            canvas.drawCircle(c.first, c.second, 5f, pointPaint)

            if (i == 0 || i == coords.size - 1 || i == coords.size / 2) {
                val timeStr = timeFormat.format(Date(point.timestamp))
                val textW = textPaint.measureText(timeStr)
                val clampedX = (c.first - textW / 2f).coerceIn(paddingLeft, w - paddingRight - textW)
                canvas.drawText(timeStr, clampedX, h - 10f, textPaint)
            }
        }
    }
}
