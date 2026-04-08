package com.android.ar_ruler_kt.opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author：TianLong
 * @date：2022/6/27 22:38
 * @detail：基础 GLSurface 类
 */
open class GLSurface : GLSurfaceView ,GLSurfaceView.Renderer{
    protected val TAG = this.javaClass.simpleName
    var iBaseRenderer:IBaseRenderer? = null
    var glWidth = 0
    var glHeight = 0
    constructor(context: Context?) : this(context,null)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){ initialize()}

    private fun initialize(){
        this.preserveEGLContextOnPause = true // GLSurfaceView  onPause和onResume切换时，是否保留EGLContext上下文
        this.setEGLContextClientVersion(3) //OpenGL ES 的版本
        this.setEGLConfigChooser(8, 8, 8, 8, 24, 0) //深度位数，在setRender之前调用

        this.setRenderer(this)
        this.renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0F, 0F, 0F,1F)

        iBaseRenderer?.run {
            this.onSurfaceCreated()
        }
        Log.w(TAG,"onSurfaceCreated")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        glHeight = height
        glWidth = width

        iBaseRenderer?.run {
            this.onSurfaceChanged(width,height)
        }
        Log.w(TAG,"onSurfaceChanged:width:$width  height:$height")
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        iBaseRenderer?.run {
            this.onDrawFrame()
        }
        Log.w(TAG,"onDrawFrame")
    }
}