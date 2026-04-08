package com.android.ar_ruler_kt.opengl

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import javax.microedition.khronos.opengles.GL

/**
 * @author：TianLong
 * @date：2022/6/27 23:31
 * @detail：背景 渲染 Renderer
 *          使用glBindBuffer来绑定索引缓冲区数据
 *          调用glDrawElement来进行渲染
 */
class BackgroundRenderer(context : Context) : BaseRenderer(context) {
    override var fragmentPath: String = "shader/background_show_camera.frag"
    override var vertexPath: String = "shader/background_show_camera.vert"

    init {
        textureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES
    }

    var u_CameraColorTexture = -1
    var a_Position = -1
    var a_CameraTexCoord = -1

    //默认顶点坐标
    private val vertexCoords = floatArrayOf(
        -1.0f, -1.0f, //第0个点
        -1.0f, +1.0f, //第1个点
        +1.0f, -1.0f, //第2个点
        +1.0f, +1.0f  //第3个点
    )

    // 两个三角形，组成一个正方形，根据顶点坐标顺序来写索引素和顺序
    private val indices = intArrayOf(
        0,2,3,
        0,3,1
    )

    val indicesBuffer: IntBuffer by lazy {
        val buffer: ByteBuffer = ByteBuffer.allocateDirect(indices.size * 4)
        buffer.order(ByteOrder.nativeOrder())
        val index = buffer.asIntBuffer()
        index.put(indices).position(0)

        return@lazy index
    }

    val vertexBuffer: FloatBuffer by lazy {
         val buffer: ByteBuffer = ByteBuffer.allocateDirect(vertexCoords.size * 4)
        buffer.order(ByteOrder.nativeOrder())
         val vertexCoord = buffer.asFloatBuffer()
         vertexCoord.put(vertexCoords).position(0)

         return@lazy vertexCoord
     }

    val textureBuffer: FloatBuffer by lazy {
        val buffer: ByteBuffer = ByteBuffer.allocateDirect(8 * 4)
        buffer.order(ByteOrder.nativeOrder())
        val textureCoord = buffer.asFloatBuffer()
        textureCoord.position(0)
        return@lazy textureCoord
    }

    override fun initShaderParameter() {
        u_CameraColorTexture =  GLES30.glGetUniformLocation(program,"u_CameraColorTexture")
        a_Position =  GLES30.glGetAttribLocation(program,"a_Position")
        a_CameraTexCoord =  GLES30.glGetAttribLocation(program,"a_CameraTexCoord")

        Log.w(TAG,"$TAG,  a_Position:$a_Position    a_CameraTexCoord:$a_CameraTexCoord    u_CameraColorTexture:$u_CameraColorTexture")
        GLError.maybeThrowGLException("initShaderParameter", "initShaderParameter$program")
    }

    override fun onSurfaceCreated() {
        Log.w(TAG,"onSurfaceCreated")
        initProgram()
        initTexture()
//        // 绑定vbo
//        val vbo = IntArray(1)
//        GLES30.glGenBuffers(1,vbo,0)
//        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,vbo[0])
//        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,4 * vertexCoords.size,vertexBuffer,GLES30.GL_STATIC_DRAW)
//        GLES30.glEnableVertexAttribArray(a_Position)
        // 绑定ibo
        val ibo = IntArray(1)
        GLES30.glGenBuffers(1,ibo,0)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER,ibo[0])
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER,4 * indices.size,indicesBuffer,GLES30.GL_STATIC_DRAW)
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
        Log.w(TAG,"onSurfaceChanged:width$width   height:$height")
    }

    override fun onDrawFrame() {
        GLError.maybeThrowGLException("BackgroundRenderer", "onDrawFrame")
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glUseProgram(program)
        GLES30.glBindTexture(textureTarget,textureIds[0])
        GLES30.glUniform1i(u_CameraColorTexture, 0)
        GLES30.glEnableVertexAttribArray(a_Position)
        GLES30.glEnableVertexAttribArray(a_CameraTexCoord)

        GLES30.glEnable(GLES30.GL_CULL_FACE)
        GLES30.glVertexAttribPointer(a_Position, 2, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        GLES30.glVertexAttribPointer(a_CameraTexCoord, 2, GLES30.GL_FLOAT, false, 0, textureBuffer)

        GLES30.glDrawElements(GLES30.GL_TRIANGLES,6,GLES30.GL_UNSIGNED_INT,0)

        GLES30.glDisable(GLES30.GL_CULL_FACE)
        GLES30.glDisableVertexAttribArray(a_Position)
        GLES30.glDisableVertexAttribArray(a_CameraTexCoord)

        GLES30.glBindTexture(textureTarget, 0)
        GLES30.glUseProgram(0)
        GLError.maybeThrowGLException("BackgroundRenderer", "onDrawFrame")
    }
}