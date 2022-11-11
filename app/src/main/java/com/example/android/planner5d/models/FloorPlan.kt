package com.example.android.planner5d.models

import android.graphics.PointF
import android.graphics.RectF

const val PLAN_DOOR_LINE_LENGTH_DEFAULT = 100.0f    // длина условного отрезка двери на плане по умолчанию (для точных размеров нужна 3D модель)
const val PLAN_WINDOW_LINE_LENGTH_DEFAULT = 100.0f  // длина условного отрезка окна на плане по умолчанию
const val PLAN_DOOR_LINE_WIDTH_DEFAULT = 20.0f
const val PLAN_WINDOW_LINE_WIDTH_DEFAULT = 20.0f

data class WallItem (
    val width: Float,         // ширина (толщина) стены
    val coords: List<PointF>  // список точек для построения ломаной линии или отрезка
)

// базовый тип для описания элемента этажа с расширениями (FloorItem - абстрактный)
sealed class FloorItem {
    // тип для описания комнаты (стены)
    class RoomItem (
        val walls: List<WallItem>  // список стен, координаты стен в общей системе координат
    ): FloorItem()
    // базовый тип для дверей, окон и произвольных 3D объектов
    sealed class MeshObject (
        val coordStart: PointF,
        val coordEnd: PointF,
        val lineWidth: Float,
    ): FloorItem() {
        // тип для описания двери
        class DoorItem constructor (
            _coordStart: PointF,
            _coordEnd: PointF,
            _lineWidth: Float
        ): MeshObject(_coordStart, _coordEnd, _lineWidth)
        // тип для описания окна
        class WindowItem constructor (
            _coordStart: PointF,
            _coordEnd: PointF,
            _lineWidth: Float
        ): MeshObject(_coordStart, _coordEnd, _lineWidth)
    }
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
