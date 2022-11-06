package com.example.android.planner5d.paint

import android.content.res.Resources
import android.graphics.*
import androidx.core.content.res.ResourcesCompat
import com.example.android.planner5d.R
import com.example.android.planner5d.models.FloorItem
import com.example.android.planner5d.models.FloorPlan

// TODO: проверить почему при малом масштабе исчезают линии
private const val WALLS_WIDTH_COEFF = 2.0f

class Painter (
    private val paint: Paint,
    resources: Resources,
) {
    var floorPlan: FloorPlan? = null
    set (value) {
        field = value
        userScale = 0.75f   // изображение вписано по охватывающеу прямоугольнику
        userOffsetX = 0.0f  // относительно центра
        userOffsetY = 0.0f
        calculateScales()
    }

    var viewWidth: Int = 0
    private set
    var viewHeight: Int = 0
    private set
    fun setViewBounds(width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
        calculateScales()
    }

    private val viewCenter = PointF(0.0f, 0.0f)
    private var viewScale = 1.0f
    private var offsetX = 0.0f  // в оригинальных единицах измерения
    private var offsetY = 0.0f

    private var userScale: Float = 0.75f   // zoomIn / zoomOut
    private var userOffsetX: Float = 0.0f  // просчитывается в системе координат проекта
    private var userOffsetY: Float = 0.0f

    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.color_floor_background, null)
    private val wallsColor = ResourcesCompat.getColor(resources, R.color.color_floor_walls, null)
    private val doorsColor = ResourcesCompat.getColor(resources, R.color.color_floor_doors, null)
    private val windowsColor = ResourcesCompat.getColor(resources, R.color.color_floor_windows, null)

    private var path = Path()

    private fun calculateScales() {
        viewCenter.x = viewWidth.toFloat() / 2
        viewCenter.y = viewHeight.toFloat() / 2
        floorPlan?.let { plan ->
            val enclosingRect = plan.enclosingRectangle()
            val viewScaleX = viewWidth / (enclosingRect.right - enclosingRect.left)
            val viewScaleY = viewHeight / (enclosingRect.bottom - enclosingRect.top)
            viewScale = if (viewScaleX < viewScaleY) viewScaleX else viewScaleY
            val planCenterOrigin = PointF(
                (enclosingRect.left + (enclosingRect.right - enclosingRect.left) / 2),
                (enclosingRect.top + (enclosingRect.bottom - enclosingRect.top) / 2)
            )
            offsetX = planCenterOrigin.x - (viewCenter.x / (viewScale * userScale)) + userOffsetX
            offsetY = planCenterOrigin.y - (viewCenter.y / (viewScale * userScale)) + userOffsetY
        }
    }

    private fun setCanvasArea(canvas: Canvas): Boolean {
        var result = false
        if ((viewWidth > 0) && (viewHeight > 0)) {
            floorPlan?.let { floor ->
                if ((floor.width > 0) && (floor.height > 0)) {
                    canvas.scale(viewScale * userScale, viewScale * userScale)
                    canvas.translate((-1) * offsetX, (-1) * offsetY)
                    result = true
                }
            }
        }
        return result
    }

    private fun drawBackground(canvas: Canvas) {
        paint.color = backgroundColor
        paint.style = Paint.Style.FILL
        canvas.drawRect(
            RectF().apply {
                left = 0.0f
                top = 0.0f
                right = floorPlan?.width ?: 0.0f
                bottom = floorPlan?.height ?: 0.0f
            },
            paint)
    }

    private fun drawWalls(canvas: Canvas) {
        paint.color = wallsColor
        paint.style = Paint.Style.STROKE
        floorPlan?.let { plan ->
            plan.floorItems.forEach { item ->
                if (item is FloorItem.RoomItem) {
                    if (item.walls.isNotEmpty()) {
                        item.walls.forEach { wall ->
                            if (wall.coords.isNotEmpty()) {
                                paint.strokeWidth = wall.width * WALLS_WIDTH_COEFF
                                path.reset()
                                path.moveTo(wall.coords[0].x, wall.coords[0].y)
                                for (idx in 1 until wall.coords.size) {
                                    path.lineTo(wall.coords[idx].x, wall.coords[idx].y)
                                }
                                canvas.drawPath(path, paint)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun drawDoorsAndWindows(canvas: Canvas) {

    }

    fun draw(canvas: Canvas) {
        canvas.save()
        if (setCanvasArea(canvas)) {
            drawBackground(canvas)
            drawWalls(canvas)
            drawDoorsAndWindows(canvas)
        }
        canvas.restore()
    }
}
