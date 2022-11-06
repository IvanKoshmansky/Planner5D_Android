package com.example.android.planner5d.paint

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.example.android.planner5d.models.FloorPlan
import timber.log.Timber

class FloorView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val painter = Painter(
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isAntiAlias = true
            isDither = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        },
        resources
    )

    // задать для инициирования перерисовки
    fun setFloorPlan(floorPlan: FloorPlan) {
        painter.floorPlan = floorPlan
        invalidate()
    }

    fun move(dx: Float, dy: Float) {
    }

    fun zoomIn() {
    }

    fun zoomOut() {
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        Timber.d("debug_regex: onSizeChanged")
        painter.setViewBounds(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Timber.d("debug_regex: onDraw")
        painter.setViewBounds(width, height)
        painter.draw(canvas)
    }
}
