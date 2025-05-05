/** Cube.kt **/

package com.maigo.dice.dices.ui

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Cube {

    private val lineBuffer: FloatBuffer
    private val program: Int
    private val dotSphere = DotSphere()
    private val dotPositions: List<FloatArray>

    // ダイスの各面における目の数（順に +X, -X, +Y, -Y, +Z, -Z）
    private val faceDotsMap = intArrayOf(1, 6, 2, 5, 3, 4)

    // 面バッファ
    private val faceVertexBuffer: FloatBuffer

    // 各面を6頂点で構成（2つの三角形）
    private val cubeFaces = floatArrayOf(
        // +X face (右面)
        1f, 1f, 1f,     1f, -1f, 1f,     1f, -1f, -1f,
        1f, 1f, 1f,     1f, -1f, -1f,    1f, 1f, -1f,

        // -X face (左面)
        -1f, 1f, -1f,   -1f, -1f, -1f,   -1f, -1f, 1f,
        -1f, 1f, -1f,   -1f, -1f, 1f,    -1f, 1f, 1f,

        // +Y face (上面)
        -1f, 1f, -1f,   -1f, 1f, 1f,     1f, 1f, 1f,
        -1f, 1f, -1f,   1f, 1f, 1f,      1f, 1f, -1f,

        // -Y face (下面)
        -1f, -1f, 1f,   -1f, -1f, -1f,   1f, -1f, -1f,
        -1f, -1f, 1f,   1f, -1f, -1f,    1f, -1f, 1f,

        // +Z face (前面)
        -1f, 1f, 1f,    -1f, -1f, 1f,    1f, -1f, 1f,
        -1f, 1f, 1f,    1f, -1f, 1f,     1f, 1f, 1f,

        // -Z face (背面)
        1f, 1f, -1f,    1f, -1f, -1f,    -1f, -1f, -1f,
        1f, 1f, -1f,    -1f, -1f, -1f,   -1f, 1f, -1f
    )

    // サイコロのエッジ（線分を描画）
    private val lineIndices = floatArrayOf(
        -1f, 1f, 1f,  -1f, -1f, 1f,  // 前面左縁
        -1f, -1f, 1f, 1f, -1f, 1f,   // 前面下縁
        1f, -1f, 1f,  1f, 1f, 1f,    // 前面右縁
        1f, 1f, 1f,  -1f, 1f, 1f,    // 前面上縁
        -1f, 1f, -1f, -1f, -1f, -1f,  // 背面左縁
        -1f, -1f, -1f, 1f, -1f, -1f,  // 背面下縁
        1f, -1f, -1f, 1f, 1f, -1f,   // 背面右縁
        1f, 1f, -1f, -1f, 1f, -1f,   // 背面上縁
        -1f, 1f, 1f,  -1f, 1f, -1f,  // 左面
        -1f, -1f, 1f, -1f, -1f, -1f,  // 下面
        1f, -1f, 1f,  1f, -1f, -1f,  // 右面
        1f, 1f, 1f,   1f, 1f, -1f    // 上面
    )

    init {
        lineBuffer = ByteBuffer.allocateDirect(lineIndices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(lineIndices)
            .apply { position(0) }

        faceVertexBuffer = ByteBuffer.allocateDirect(cubeFaces.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(cubeFaces)
            .apply { position(0) }

        // シェーダープログラムの作成
        val vertexShaderCode = """
            uniform mat4 uMVPMatrix;
            attribute vec4 vPosition;
            void main() {
                gl_Position = uMVPMatrix * vPosition;
                gl_PointSize = 30.0;
            }
        """.trimIndent()

        val fragmentShaderCode = """
            precision mediump float;
            uniform vec4 vColor;
            void main() {
                gl_FragColor = vColor;
            }
        """.trimIndent()

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        // ドット座標を計算
        dotPositions = generateDotPositions()
    }

    fun draw(mvpMatrix: FloatArray) {
        GLES20.glUseProgram(program)

        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        val colorHandle = GLES20.glGetUniformLocation(program, "vColor")
        val mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        // 面を白で描画
        GLES20.glUniform4f(colorHandle, 1f, 1f, 1f, 1f)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, faceVertexBuffer)
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, cubeFaces.size / 3)

        // エッジを灰色で描画
        GLES20.glUniform4f(colorHandle, 0.6f, 0.6f, 0.6f, 1f)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, lineBuffer)
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, lineIndices.size / 3)

        GLES20.glDisableVertexAttribArray(positionHandle)

        // ドットを球体で描画
        for (pos in dotPositions) {
            dotSphere.draw(mvpMatrix, pos)
        }
    }

    private fun loadShader(type: Int, code: String): Int {
        return GLES20.glCreateShader(type).also {
            GLES20.glShaderSource(it, code)
            GLES20.glCompileShader(it)
        }
    }

    private fun generateDotPositions(): List<FloatArray> {
        val result = mutableListOf<FloatArray>()

        val dotCoords = arrayOf(
            arrayOf(floatArrayOf(0f, 0f, 0.03f)), // 1
            arrayOf(floatArrayOf(-0.3f, -0.3f, 0.03f), floatArrayOf(0.3f, 0.3f, 0.03f)), // 2
            arrayOf(floatArrayOf(-0.3f, -0.3f, 0.03f), floatArrayOf(0f, 0f, 0.03f), floatArrayOf(0.3f, 0.3f, 0.03f)), // 3
            arrayOf(floatArrayOf(-0.3f, -0.3f, 0.03f), floatArrayOf(0.3f, -0.3f, 0.03f), floatArrayOf(-0.3f, 0.3f, 0.03f), floatArrayOf(0.3f, 0.3f, 0.03f)), // 4
            arrayOf(floatArrayOf(-0.3f, -0.3f, 0.03f), floatArrayOf(0.3f, -0.3f, 0.03f), floatArrayOf(0f, 0f, 0.03f), floatArrayOf(-0.3f, 0.3f, 0.03f), floatArrayOf(0.3f, 0.3f, 0.03f)), // 5
            arrayOf(floatArrayOf(-0.3f, -0.4f, 0.03f), floatArrayOf(-0.3f, 0f, 0.03f), floatArrayOf(-0.3f, 0.4f, 0.03f), floatArrayOf(0.3f, -0.4f, 0.03f), floatArrayOf(0.3f, 0f, 0.03f), floatArrayOf(0.3f, 0.4f, 0.03f)) // 6
        )

        val normals = arrayOf(
            floatArrayOf(1f, 0f, 0f), floatArrayOf(-1f, 0f, 0f),
            floatArrayOf(0f, 1f, 0f), floatArrayOf(0f, -1f, 0f),
            floatArrayOf(0f, 0f, 1f), floatArrayOf(0f, 0f, -1f)
        )

        val faceDots = intArrayOf(1, 6, 2, 5, 3, 4)

        for (i in normals.indices) {
            val normal = normals[i]
            val faceIndex = faceDots[i] - 1
            for (coord in dotCoords[faceIndex]) {
                val (x, y, z) = coord
                val pos = when {
                    normal[0] != 0f -> floatArrayOf(normal[0] + z, y, x)
                    normal[1] != 0f -> floatArrayOf(x, normal[1] + z, y)
                    else -> floatArrayOf(x, y, normal[2] + z)
                }
                result.add(pos)
            }
        }

        return result
    }
}
