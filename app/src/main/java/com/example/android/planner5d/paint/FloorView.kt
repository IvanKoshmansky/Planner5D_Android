package com.example.android.planner5d.paint

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import com.example.android.planner5d.models.FloorPlan
import timber.log.Timber

// GestureDetector.OnGestureListener
// When you instantiate a GestureDetectorCompat object, one of the parameters it takes is a class
// that implements the GestureDetector.OnGestureListener interface.
// GestureDetector.OnGestureListener notifies users when a particular touch event has occurred.
// To make it possible for your GestureDetector object to receive events, you override the View or
// Activity's onTouchEvent() method, and pass along all observed events to the detector instance.

// GestureDetectorCompat.onTouchEvent()
// Analyzes the given motion event and if applicable triggers the appropriate callbacks
// on the OnGestureListener supplied.

class FloorView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GestureDetector.OnGestureListener {

    private lateinit var mDetector: GestureDetectorCompat

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mDetector = GestureDetectorCompat(context, this)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (mDetector.onTouchEvent(event)) {
            true
        } else {
            performClick()
            super.onTouchEvent(event)
        }
    }

    // Call this view's OnClickListener, if it is defined.
    // True there was an assigned OnClickListener that was called, false otherwise is returned.
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

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

    fun submitData(floorPlan: FloorPlan, viewPort: ViewPort) {
        painter.submitData(floorPlan, viewPort)
        invalidate()
    }

    fun setViewPort(viewPort: ViewPort) {
        painter.setViewPort(viewPort)
        invalidate()
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

    override fun onDown(p0: MotionEvent?): Boolean {
        return true
    }

    override fun onShowPress(p0: MotionEvent?) {}

    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        return true
    }

    override fun onScroll(event1: MotionEvent, event2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        Timber.d("debug_regex: dx = $distanceX dy = $distanceY")
        if ((Math.abs(distanceX) > 10.0) || (Math.abs(distanceY) > 10.0)) {
            painter.moveViewPort(distanceX, distanceY)
            invalidate()
        }
        return true
    }

    override fun onLongPress(p0: MotionEvent?) {}

    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        return true
    }
}
