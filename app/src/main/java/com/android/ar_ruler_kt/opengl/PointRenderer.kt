package com.android.ar_ruler_kt.opengl

import android.content.Context
import android.opengl.GLES30
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * @author：TianLong
 * @date：2022/7/6 23:14
 * @detail：
 */
class PointRenderer(context: Context) : BaseRenderer(context) ,IMatrix{
    override var vertexPath: String = "shader/point_shader.vert"
    override var fragmentPath: String= "shader/point_shader.frag"

    override var matrix = FloatArray(16)

    var a_Position = -1
    var u_MvpMatrix = -1
    var u_Size = -1
    val vertexCoord = floatArrayOf(
        0f , 0f, 0f
    )

    val vertexBuffer by lazy {
        val buffer = ByteBuffer.allocateDirect(vertexCoord.size * 4)
        buffer.order(ByteOrder.nativeOrder())
        val temp = buffer.asFloatBuffer()
        temp.put(vertexCoord)
        temp.position(0)
    }


    override fun initShaderParameter() {
        a_Position = GLES30.glGetAttribLocation(program,"a_Position")
        GLError.maybeThrowGLException("PointRenderer", "onSurfaceCreated")
        u_MvpMatrix = GLES30.glGetUniformLocation(program,"u_MvpMatrix")
        GLError.maybeThrowGLException("PointRenderer", "onSurfaceCreated")
        u_Size = GLES30.glGetUniformLocation(program,"u_Size")
        GLError.maybeThrowGLException("PointRenderer", "onSurfaceCreated")
    }

    override fun onSurfaceCreated() {
        initProgram()
        GLError.maybeThrowGLException("PointRenderer", "onSurfaceCreated")
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
        GLError.maybeThrowGLException("PointRenderer", "onSurfaceChanged")
    }

    override fun onDrawFrame() {
        GLES30.glUseProgram(program)
        GLES30.glEnable(GLES30.GL_CULL_FACE)
        GLES30.glEnableVertexAttribArray(a_Position)
        GLES30.glUniformMatrix4fv(u_MvpMatrix,1,false,matrix,0)
        GLES30.glUniform1f(u_Size,20f)

        GLES30.glVertexAttribPointer(a_Position,3,GLES30.GL_FLOAT,false,0,vertexBuffer)
        GLES30.glDrawArrays(GLES30.GL_POINTS,0,1)

        GLES30.glDisableVertexAttribArray(a_Position)
        GLES30.glDisable(GLES30.GL_CULL_FACE)
        GLES30.glUseProgram(0)
        GLError.maybeThrowGLException("PointRenderer", "onDrawFrame")
    }
}