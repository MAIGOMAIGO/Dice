/** DotSphere.kt **/

package com.maigo.dice.dices.ui

import android.opengl.GLES20
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.*

class DotSphere(private val radius: Float = 0.11f, stacks: Int = 30, slices: Int = 30) {

    private val vertexBuffer: FloatBuffer
    private val vertexCount: Int
    private val program: Int

    private val vertexShader = """
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;
        void main() {
            gl_PointSize = 8.0;
            gl_Position = uMVPMatrix * vPosition;
        }
    """.trimIndent()

    private val fragmentShader = """
        precision mediump float;
        uniform vec4 vColor;
        void main() {
            gl_FragColor = vColor;
        }
    """.trimIndent()

    init {
        val vertices = mutableListOf<Float>()

        for (i in 0..stacks) {
            val phi = Math.PI * i / stacks
            val y = cos(phi).toFloat()
            val r = sin(phi).toFloat()

            for (j in 0..slices) {
                val theta = 2.0 * Math.PI * j / slices
                val x = (r * cos(theta)).toFloat()
                val z = (r * sin(theta)).toFloat()
                vertices.addAll(listOf(x * radius, y * radius, z * radius))
            }
        }

        vertexCount = vertices.size / 3
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(vertices.toFloatArray())
                position(0)
            }

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, loadShader(GLES20.GL_VERTEX_SHADER, vertexShader))
            GLES20.glAttachShader(it, loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader))
            GLES20.glLinkProgram(it)
        }
    }

    fun draw(mvpMatrix: FloatArray, center: FloatArray) {
        GLES20.glUseProgram(program)

        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        val colorHandle = GLES20.glGetUniformLocation(program, "vColor")
        val mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")

        val modelMatrix = FloatArray(16)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, center[0], center[1], center[2])
        val finalMatrix = FloatArray(16)
        Matrix.multiplyMM(finalMatrix, 0, mvpMatrix, 0, modelMatrix, 0)

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, finalMatrix, 0)
        GLES20.glUniform4f(colorHandle, 0f, 0f, 0f, 1f)

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, vertexCount) // 変更点

        GLES20.glDisableVertexAttribArray(positionHandle)
    }

    private fun loadShader(type: Int, code: String): Int {
        return GLES20.glCreateShader(type).also {
            GLES20.glShaderSource(it, code)
            GLES20.glCompileShader(it)
        }
    }
}
