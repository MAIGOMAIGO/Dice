package com.maigo.dice.dices.ui

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent

class DiceGLView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private val renderer: DiceRenderer
    private var previousX = 0f
    private var previousY = 0f

    init {
        setEGLContextClientVersion(2)
        renderer = DiceRenderer()
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun rotate(angleX: Float, angleY: Float) {
        renderer.angleX = angleX
        renderer.angleY = angleY
    }

    fun rotateTo(angleX: Float, angleY: Float) {
        renderer.angleX = angleX
        renderer.angleY = angleY
        requestRender()
    }

    fun getRenderer(): DiceRenderer = renderer

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                previousX = event.x
                previousY = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.x - previousX
                val deltaY = event.y - previousY

                renderer.angleY += deltaX * 0.5f
                renderer.angleX += deltaY * 0.5f

                previousX = event.x
                previousY = event.y
            }
        }
        return true
    }

    fun isRolled(): Boolean = renderer.isRotating()

    /**
     * DiceRenderer に処理を委譲
     */
    fun rollTo(face: Int, sides: Int) {
        if (sides == 6 && !renderer.isRotating()) {
            renderer.animateToFace(face, sides)
        }
    }
}
