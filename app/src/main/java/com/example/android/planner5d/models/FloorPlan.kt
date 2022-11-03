package com.example.android.planner5d.models

import android.graphics.PointF

const val PLAN_DOOR_LINE_LENGTH_DEFAULT = 800.0f     // длина отрезка двери на плане по умолчанию
const val PLAN_WINDOW_LINE_LENGTH_DEFAULT = 1200.0f  // длина отрезка окна на плане по умолчанию
const val PLAN_DOOR_LINE_WIDTH_DEFAULT = 10.0f
const val PLAN_WINDOW_LINE_WIDTH_DEFAULT = 10.0f

// TODO: проработать какой конкретный паттерн проектирования можно применить
// для постороения модели этажа: фабрика, адаптер, мост, строитель?

// при преобразовании в эту модель все масштабы автоматически пересчитываются

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
    val width: Double,        // общие размеры проекта
    val height: Double,       // общие размеры проекта
    val floorItems: List<FloorItem>
) {
    companion object {
        fun fillEmpty() = FloorPlan("", 0.0, 0.0, listOf())
    }
}
