package com.android.ar_ruler_kt.opengl

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log
import com.google.ar.core.Pose
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

/**
 * @author：TianLong
 * @date：2022/7/9 12:12
 * @detail：距离长度渲染
 */
class PictureRenderer(context: Context) : BaseRenderer(context),IMatrix ,IBitmapInterview,IMathInterview{
    override var fragmentPath: String = "shader/bitmap_shader.frag"
    override var vertexPath: String = "shader/bitmap_shader.vert"
    override var matrix: FloatArray = FloatArray(16)
    lateinit var bitmap :Bitmap

    var u_ColorTexture = -1
    var u_MvpMatrix = -1
    var a_Position = -1
    var a_ColorTexCoord = -1


    val vertex = FloatArray(12)

    var vertexBuffer  = ByteBuffer.allocateDirect(vertex.size * Float.SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer()

    val texture = floatArrayOf(
        +0.0f,+1.0f,
        +1.0f,+1.0f,
        +0.0f,+0.0f,
        +1.0f,+0.0f,
    )

    val textureBuffer by lazy {
        val buffer = ByteBuffer.allocateDirect(texture.size * Float.SIZE_BYTES).order(ByteOrder.nativeOrder())
        val tempBuffer = buffer.asFloatBuffer()
        tempBuffer.put(texture)
        tempBuffer.position(0)
    }


    override fun initShaderParameter() {
        a_Position = GLES30.glGetAttribLocation(program,"a_Position")
        a_ColorTexCoord = GLES30.glGetAttribLocation(program,"a_ColorTexCoord")
        u_MvpMatrix = GLES30.glGetUniformLocation(program,"u_MvpMatrix")
        u_ColorTexture = GLES30.glGetUniformLocation(program,"u_ColorTexture")

        GLError.maybeThrowGLException("PictureRenderer", "initShaderParameter")
    }

    override fun onSurfaceCreated() {
        initProgram()
        initTexture()

        GLError.maybeThrowGLException("PictureRenderer", "onSurfaceCreated")
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        this.width = width
        this.height = height

        GLError.maybeThrowGLException("PictureRenderer", "onSurfaceChanged")
    }

    override fun onDrawFrame() {
        GLError.maybeThrowGLException("PictureRenderer", "onDrawFrame")
        GLES30.glUseProgram(program)
        GLES30.glBindTexture(textureTarget,textureIds[0])

        GLES30.glUniform1i(u_ColorTexture,0)
        GLES30.glUniformMatrix4fv(u_MvpMatrix,1,false,matrix,0)

        GLES30.glEnableVertexAttribArray(a_Position)
        GLES30.glEnableVertexAttribArray(a_ColorTexCoord)
        //开启背面剔除
        GLES30.glEnable(GLES30.GL_CULL_FACE)
        GLES30.glCullFace(GLES30.GL_BACK)
        // 开启混色
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)

        GLES30.glVertexAttribPointer(a_Position,3,GLES30.GL_FLOAT,false,0,vertexBuffer)
        GLES30.glVertexAttribPointer(a_ColorTexCoord,2,GLES30.GL_FLOAT,false,0,textureBuffer)

        GLUtils.texImage2D(textureTarget,0,bitmap,0)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP,0,4)

        GLES30.glDisable(GLES30.GL_CULL_FACE)
        GLES30.glDisable(GLES30.GL_BLEND)

        GLES30.glDisableVertexAttribArray(a_Position)
        GLES30.glDisableVertexAttribArray(a_ColorTexCoord)

        GLES30.glBindTexture(textureTarget,0)
        GLES30.glUseProgram(0)

        GLError.maybeThrowGLException("PictureRenderer", "onDrawFrame")
    }

    /**
     * 给bitmap绘制内容
     * @param content String
     */
    fun setLength2Bitmap(content:String){
        bitmap = drawBitmap(200,80,content)
    }

    /**
     * 设置顶点坐标
     * @param pose1 Pose
     * @param pose2 Pose
     * @param viewMatrix FloatArray
     */
    fun upDataVertex(pose1:Pose,pose2:Pose,viewMatrix:FloatArray) {
        val pos1_world = floatArrayOf(pose1.tx(),pose1.ty(),pose1.tz(),1f)
        val pos2_world = floatArrayOf(pose2.tx(),pose2.ty(),pose2.tz(),1f)

        // 转为相机坐标系下的两个点
        val pos1_camera = FloatArray(4)
        val pos2_camera = FloatArray(4)
        Matrix.multiplyMV(pos1_camera, 0, viewMatrix, 0, pos1_world, 0)
        Matrix.multiplyMV(pos2_camera, 0, viewMatrix, 0, pos2_world, 0)


        val mappingNear = true // 是否投影在近剪切面上
        // 转为近剪切面上的两个点
        var newpose1 = FloatArray(4)
        var newpose2 = FloatArray(4)
        if (mappingNear){
            mappingNear(newpose1,pos1_camera,- 0.1f)
            mappingNear(newpose2,pos2_camera,- 0.1f)
        }else{
            newpose1 = pos1_camera
            newpose2 = pos2_camera
        }

        // newpose1，newpose2的中点
        val centerpose = floatArrayOf(
            (newpose2[0] + newpose1[0])/2,
            (newpose2[1] + newpose1[1])/2,
            (newpose2[2] + newpose1[2])/2,
            +1.0f
        )

        // 求出这两个点 在 z = -0.1时，在这个平面上的二维向量
        val vectorX = FloatArray(2)
        vectorX[0] = newpose2[0] - newpose1[0]
        vectorX[1] = newpose2[1] - newpose1[1]

        // vector进行归一化
        val normalX = normal(vectorX)
        // 旋转90度，垂直向量，此处容易有问题，原因是Java的cos90°不为0
        val normalY = rotate90(normalX)

        val pointA = FloatArray(3)
        val pointB = FloatArray(3)
        val pointC = FloatArray(3)
        val pointD = FloatArray(3)

        val th1: Float
        val th2: Float
        if (mappingNear){
            th1 = 0.01f / 2
            th2 = 0.025f / 8
        }else{
            th1 = 0.01f / 2 * 10
            th2 = 0.025f / 8 * 10
        }
        val vector1X = th1 * normalX[0]
        val vector1Y = th1 * normalX[1]

        val vector2X = th2 * normalY[0]
        val vector2Y = th2 * normalY[1]

        pointA[0] = centerpose[0] - vector1X - vector2X
        pointA[1] = centerpose[1] - vector1Y - vector2Y
        pointA[2] = centerpose[2]
        pointB[0] = centerpose[0] + vector1X - vector2X
        pointB[1] = centerpose[1] + vector1Y - vector2Y
        pointB[2] = centerpose[2]
        pointC[0] = centerpose[0] - vector1X + vector2X
        pointC[1] = centerpose[1] - vector1Y + vector2Y
        pointC[2] = centerpose[2]
        pointD[0] = centerpose[0] + vector1X + vector2X
        pointD[1] = centerpose[1] + vector1Y + vector2Y
        pointD[2] = centerpose[2]

        // 保证看的时候，纹理始终朝上
        // A点的Y 小于 C点的Y时。顶点坐标按A-B-C-D排序
        // A点的Y不小于C点的Y时，顶点坐标按D-C-B-A排序
        if (pointA[1]<pointC[1]){
            vertex[0] = pointA[0]
            vertex[1] = pointA[1]
            vertex[2] = pointA[2]

            vertex[3] = pointB[0]
            vertex[4] = pointB[1]
            vertex[5] = pointB[2]

            vertex[6] = pointC[0]
            vertex[7] = pointC[1]
            vertex[8] = pointC[2]

            vertex[9] = pointD[0]
            vertex[10] = pointD[1]
            vertex[11] = pointD[2]
        }else{
            vertex[0] = pointD[0]
            vertex[1] = pointD[1]
            vertex[2] = pointD[2]

            vertex[3] = pointC[0]
            vertex[4] = pointC[1]
            vertex[5] = pointC[2]

            vertex[6] = pointB[0]
            vertex[7] = pointB[1]
            vertex[8] = pointB[2]

            vertex[9] = pointA[0]
            vertex[10] = pointA[1]
            vertex[11] = pointA[2]
        }

        Log.e("CenterPose", "${centerpose.contentToString()}  ${normalX.contentToString()}  ${normalY.contentToString()} ")
        vertexBuffer.put(vertex).position(0)
    }
}