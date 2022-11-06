package com.example.android.planner5d.models

import android.graphics.PointF
import android.graphics.RectF

const val PLAN_DOOR_LINE_LENGTH_DEFAULT = 800.0f     // длина отрезка двери на плане по умолчанию
const val PLAN_WINDOW_LINE_LENGTH_DEFAULT = 1200.0f  // длина отрезка окна на плане по умолчанию
const val PLAN_DOOR_LINE_WIDTH_DEFAULT = 10.0f
const val PLAN_WINDOW_LINE_WIDTH_DEFAULT = 10.0f

data class WallItem (
    val width: Float,         // ширина (толщина) стены
    val coords: List<PointF>  // список точек для построения ломаной линии или отрезка
)

sealed class FloorItem {
    data class RoomItem (
        val walls: List<WallItem>  // список стен, координаты стен в общей системе координат
    ): FloorItem()
    data class DoorItem (
        val angle: Float,          // угол поворота
        val coord: PointF,         // координата начала
        val vectorLength: Float,   // длина отрезка
        val vectorWidth: Float,    // ширина отрезка
    ): FloorItem()
    data class WindowItem (
        val angle: Float,          // угол поворота
        val coord: PointF,         // координата начала
        val vectorLength: Float,   // длина отрезка
        val vectorWidth: Float,    // ширина отрезка
    ): FloorItem()
}

data class FloorPlan (
    val projectName: String,  // имя проекта
    val width: Float,         // общие размеры проекта
    val height: Float,        // общие размеры проекта
    val floorItems: List<FloorItem>
) {
    // охатывающий прямоугольник (относительно левого верхнего угла проекта) с учетом стен всех комнат на этаже
    fun enclosingRectangle(): RectF {
        val rect = RectF(width, height, 0.0f, 0.0f)
        if (floorItems.isNotEmpty()) {
            floorItems.forEach { floorItem ->
                if (floorItem is FloorItem.RoomItem) {
                    if (floorItem.walls.isNotEmpty()) {
                        floorItem.walls.forEach { wall ->
                            if (wall.coords.isNotEmpty()) {
                                wall.coords.forEach {
                                    if (it.x < rect.left) {
                                        rect.left = it.x
                                    }
                                    if (it.x > rect.right) {
                                        rect.right = it.x
                                    }
                                    if (it.y < rect.top) {
                                        rect.top = it.y
                                    }
                                    if (it.y > rect.bottom) {
                                        rect.bottom = it.y
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return rect
    }

    companion object {
        fun fillEmpty() = FloorPlan("", 0.0f, 0.0f, listOf())
    }
}
