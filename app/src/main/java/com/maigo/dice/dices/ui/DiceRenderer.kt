/** DiceRenderer.kt **/

package com.maigo.dice.dices.ui

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class DiceRenderer : GLSurfaceView.Renderer {

    private lateinit var cube: Cube

    var angleX = 0f
    var angleY = 0f

    private var rotating = false
    private var currentFrame = 0
    private var totalFrames = 60
    private var fromAngleX = 0f
    private var fromAngleY = 0f
    private var toAngleX = 0f
    private var toAngleY = 0f

    private val faceToAngles = mapOf(
        1 to (0f to 0f),
        2 to (90f to 0f),
        3 to (0f to -90f),
        4 to (0f to 90f),
        5 to (-90f to 0f),
        6 to (180f to 0f)
    )

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.2f, 0.2f, 0.2f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        cube = Cube()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 2f, 10f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        if (rotating) {
            val t = currentFrame.toFloat() / totalFrames
            angleX = fromAngleX + (toAngleX - fromAngleX) * t
            angleY = fromAngleY + (toAngleY - fromAngleY) * t
            currentFrame++
            if (currentFrame > totalFrames) {
                rotating = false
            }
        }

        // ビュー行列: カメラ位置を設定
        Matrix.setLookAtM(viewMatrix, 0,
            0f, 0f, 5f,    // eye
            0f, 0f, 0f,    // center
            0f, 1f, 0f     // up
        )

        // 回転行列
        val tempMatrix = FloatArray(16)
        Matrix.setRotateM(rotationMatrix, 0, angleX, 1f, 0f, 0f)
        Matrix.setRotateM(tempMatrix, 0, angleY, 0f, 1f, 0f)
        Matrix.multiplyMM(rotationMatrix, 0, rotationMatrix, 0, tempMatrix, 0)

        // MVP行列 = P * V * R
        val vpMatrix = FloatArray(16)
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, rotationMatrix, 0)

        cube.draw(mvpMatrix)
    }

    fun animateToFace(face: Int, sides: Int) {
        faceToAngles[face]?.let { (targetX, targetY) ->
            val spinCount = 2
            fromAngleX = angleX % 360f
            fromAngleY = angleY % 360f
            toAngleX = targetX + 360f * spinCount
            toAngleY = targetY + 360f * spinCount
            currentFrame = 0
            rotating = true
        }
    }

    fun isRotating(): Boolean = rotating
}
