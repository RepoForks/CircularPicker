package com.agilie.agtimepicker.timepicker

import android.graphics.*
import android.view.MotionEvent
import com.agilie.agtimepicker.OnSwipeTouchListener
import com.agilie.agtimepicker.animation.HoursPickerPath
import com.agilie.agtimepicker.animation.MinutesPickerPath
import com.agilie.agtimepicker.animation.TrianglePath
import com.agilie.volumecontrol.calculateAngleWithTwoVectors
import com.agilie.volumecontrol.distance
import com.agilie.volumecontrol.getPointOnBorderLineOfCircle
import com.agilie.volumecontrol.pointInCircle
import java.lang.Math.max
import java.lang.Math.min

class AGTimePickerImpl(val hoursPickerPath: HoursPickerPath,
                       val minutesPickerPath: MinutesPickerPath,
                       val trianglePath: TrianglePath,
                       var hoursColors: IntArray = intArrayOf(
                               Color.parseColor("#0080ff"),
                               Color.parseColor("#53FFFF")),
                       var minutesColor: IntArray = intArrayOf(
                               Color.parseColor("#FF8D00"),
                               Color.parseColor("#FF0058"),
                               Color.parseColor("#920084")
                       )) : AGTimePicker, OnSwipeTouchListener.OnSwipeAction {

    private val MAX_PULL_UP = 65f

    override fun onDraw(canvas: Canvas) {
        hoursPickerPath.onDraw(canvas)
        minutesPickerPath.onDraw(canvas)
        trianglePath.onDraw(canvas)
    }

    override fun onSizeChanged(width: Int, height: Int) {
        val center = PointF(width / 2f, height / 2f)
        val radius = Math.min(width, height) / 4f
        updatePaint(center, radius)
        drawShapes(center, radius)
    }

    fun updatePaint(center: PointF, radius: Float) {
        val startPoint = getPointOnBorderLineOfCircle(center, radius, 180)
        val endPoint = getPointOnBorderLineOfCircle(center, radius, 0)
        hoursPickerPath.paint.apply {
            shader = LinearGradient(startPoint.x, startPoint.y, endPoint.x, endPoint.y, hoursColors,
                    null,
                    Shader.TileMode.CLAMP)
        }
        minutesPickerPath.paint.apply {
            shader = LinearGradient(startPoint.x, startPoint.y, endPoint.x, endPoint.y, minutesColor,
                    null,
                    Shader.TileMode.CLAMP)
        }
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                onActionDown(PointF(event.x, event.y))
            }
            MotionEvent.ACTION_MOVE -> {
                onActionMove(PointF(event.x, event.y))
            }
            MotionEvent.ACTION_UP -> {
                onActionUp()
            }
        }
        return true
    }

    override fun onSwipeRight() {
    }

    override fun onSwipeLeft() {
    }

    override fun onSwipeTop() {
    }

    override fun onSwipeBottom() {
    }

    private fun drawShapes(center: PointF, radius: Float) {
        hoursPickerPath.center = center
        minutesPickerPath.center.apply {
            y = center.y
            x = center.x + 2.09f * radius + MAX_PULL_UP
        }
        trianglePath.center = center

        hoursPickerPath.radius = radius
        minutesPickerPath.radius = radius
        trianglePath.radius = radius

        hoursPickerPath.createPickerPath()
        minutesPickerPath.createPickerPath()
        trianglePath.createTrianglePath()
    }

    private fun onActionDown(pointF: PointF) {
        val pointInCircle = pointInCircle(pointF, hoursPickerPath.center, hoursPickerPath.radius)
        hoursPickerPath.lockMove = !pointInCircle
        trianglePath.lockMove = !pointInCircle
    }


    private fun onActionMove(pointF: PointF) {
        val angle = calculateAngleWithTwoVectors(pointF, hoursPickerPath.center)
        val distance = distance(pointF, hoursPickerPath.center) - hoursPickerPath.radius
        //TODO clean up code
        val pullUp = min(MAX_PULL_UP, max(distance, 0f))
        hoursPickerPath.onActionMove(angle, pullUp)

        if (pullUp != 0f) trianglePath.onActionMove(angle, pullUp)
    }

    private fun onActionUp() {
        hoursPickerPath.lockMove = true
        trianglePath.lockMove = true

        hoursPickerPath.onActionUp()
        trianglePath.onActionUp()
    }


}