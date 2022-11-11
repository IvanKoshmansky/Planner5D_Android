package com.example.android.planner5d.paint

import android.graphics.PointF

private const val VIEWPORT_OFFSET_X_DEFAULT = 0.0f
private const val VIEWPORT_OFFSET_Y_DEFAULT = 0.0f
private const val VIEWPORT_SCALE_DEFAULT = 0.75f

// точка обзора (смещение и масштаб)
class ViewPort constructor (
    _offsetX: Float, _offsetY: Float, _scale: Float
) {
    val offset = PointF(_offsetX, _offsetY)
    var scale = _scale

    fun setDefault() {
        offset.x = VIEWPORT_OFFSET_X_DEFAULT
        offset.y = VIEWPORT_OFFSET_Y_DEFAULT
        scale = VIEWPORT_SCALE_DEFAULT
    }

    fun zoomIn() {
        scale += (scale / 10.0f)
        if (scale > 10.0f) scale = 10.0f
    }

    fun zoomOut() {
        scale -= (scale / 10.0f)
        if (scale < 0.1f) scale = 0.1f
    }

    companion object {
        fun createDefault() = ViewPort(
            VIEWPORT_OFFSET_X_DEFAULT,
            VIEWPORT_OFFSET_Y_DEFAULT,
            VIEWPORT_SCALE_DEFAULT
        )
    }
}
