package com.example.android.planner5d.paint

import android.graphics.PointF

// точка обзора (смещение и масштаб)
class ViewPort constructor (
  _offsetX: Float, _offsetY: Float, _scale: Float
) {
    val offset = PointF(_offsetX, _offsetY)
    var scale = _scale

    fun zoomIn() {
        if (scale < 10) scale += 0.1f
    }

    fun zoomOut() {
        if (scale > 0.1) scale -= 0.1f
    }

    fun setDefault() {
        offset.x = 0.0f
        offset.y = 0.0f
        scale = 0.75f
    }

    companion object {
        fun createDefault(): ViewPort = ViewPort(0.0f, 0.0f, 0.75f)
    }
}
