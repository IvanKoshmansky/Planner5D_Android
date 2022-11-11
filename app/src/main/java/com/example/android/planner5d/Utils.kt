package com.example.android.planner5d

import android.graphics.PointF

// функция расширения для получения следующей точки по длине отрезка и углу поворота
fun PointF.nextPoint(length: Float, angle: Float) = PointF(
    (x + length * Math.cos(Math.toRadians(angle.toDouble()))).toFloat(),
    (y + length * Math.sin(Math.toRadians(angle.toDouble()))).toFloat()
)
