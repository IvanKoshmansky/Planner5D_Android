package com.example.android.planner5d.paint

import android.content.res.Resources
import android.graphics.*
import androidx.core.content.res.ResourcesCompat
import com.example.android.planner5d.R
import com.example.android.planner5d.models.FloorItem
import com.example.android.planner5d.models.FloorPlan

class Painter (
    private val paint: Paint,
    resources: Resources,
) {
    private var floorPlan: FloorPlan? = null
    private var _viewPort: ViewPort? = null
    private val viewPort: ViewPort
        get() = _viewPort ?: ViewPort.createDefault()

    fun submitData(floorPlan: FloorPlan, viewPort: ViewPort) {
        this.floorPlan = floorPlan
        this._viewPort = viewPort
        calculateScales()
    }

    fun submitData(viewPort: ViewPort) {
        this._viewPort = viewPort
        calculateScales()
    }

    private var viewWidth: Int = 0
    private var viewHeight: Int = 0

    fun setViewBounds(width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
        calculateScales()
    }

    private val viewCenter = PointF(0.0f, 0.0f)
    private var viewScale = 1.0f
    private var offsetX = 0.0f  // в оригинальных единицах измерения
    private var offsetY = 0.0f

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
            offsetX = planCenterOrigin.x - (viewCenter.x / (viewScale * viewPort.scale)) + viewPort.offset.x
            offsetY = planCenterOrigin.y - (viewCenter.y / (viewScale * viewPort.scale)) + viewPort.offset.y
        }
    }

    private fun setCanvasArea(canvas: Canvas): Boolean {
        var result = false
        if ((viewWidth > 0) && (viewHeight > 0)) {
            floorPlan?.let { floor ->
                if ((floor.width > 0) && (floor.height > 0)) {
                    canvas.scale(viewScale * viewPort.scale, viewScale * viewPort.scale)
                    canvas.translate((-1) * offsetX, (-1) * offsetY)
                    result = true
                }
            }
        }
        return result
    }

    private fun drawBackground(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = backgroundColor
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
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = wallsColor
        floorPlan?.let { plan ->
            plan.floorItems.forEach { item ->
                if (item is FloorItem.RoomItem) {
                    if (item.walls.isNotEmpty()) {
                        item.walls.forEach { wall ->
                            if (wall.coords.isNotEmpty()) {
                                paint.strokeWidth = wall.width
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

    private fun drawMeshObjects(canvas: Canvas) {
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.SQUARE
        floorPlan?.let { plan ->
            plan.floorItems.forEach { item ->
                if (item is FloorItem.MeshObject) {
                    when (item) {
                        is FloorItem.MeshObject.DoorItem -> {
                            paint.color = doorsColor
                        }
                        is FloorItem.MeshObject.WindowItem -> {
                            paint.color = windowsColor
                        }
                    }
                    paint.strokeWidth = item.lineWidth
                    path.reset()
                    path.moveTo(item.coordStart.x, item.coordStart.y)
                    path.lineTo(item.coordEnd.x, item.coordEnd.y)
                    canvas.drawPath(path, paint)
                }
            }
        }
    }

    fun draw(canvas: Canvas) {
        canvas.save()
        if (setCanvasArea(canvas)) {
            drawBackground(canvas)
            drawWalls(canvas)
            drawMeshObjects(canvas)
        }
        canvas.restore()
    }
}
