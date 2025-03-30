package com.example.mindbridge

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

class SwipeGestureDetector(context: Context, private val onSwipe: (String) -> Unit) : GestureDetector.SimpleOnGestureListener() {

    private val gestureDetector = GestureDetector(context, this)

    fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (e1 != null && e2 != null) {
            val deltaX = e2.x - e1.x
            val deltaY = e2.y - e1.y
            if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > SWIPE_THRESHOLD) {
                if (deltaX > 0) {
                    onSwipe("right")
                } else {
                    onSwipe("left")
                }
                return true
            }
        }
        return false
    }

    companion object {
        private const val SWIPE_THRESHOLD = 100
    }
}
