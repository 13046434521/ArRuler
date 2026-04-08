package com.android.ar_ruler_kt.opengl

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.util.Log
import com.android.ar_ruler_kt.helper.ShaderHelper

/**
 * @author：TianLong
 * @date：2022/6/27 22:39
 * @detail：基础 Renderer 类
 */
 abstract class BaseRenderer(override var context: Context) : IBaseRenderer,ShaderImpl{
    var textureIds = IntArray(1)
    var program : Int = -1
    var textureTarget = GLES30.GL_TEXTURE_2D
    override lateinit var vertexSource: String
    override lateinit var fragmentSource: String
    var width = -1
    var height = -1

    fun initProgram(){
        program = GLES30.glCreateProgram()
        val fragShader = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER)
        val vertexShader = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER)
        vertexSource =  ShaderHelper.readRawTextFileFromAssets(context,vertexPath)
        fragmentSource = ShaderHelper.readRawTextFileFromAssets(context,fragmentPath)
        GLES30.glShaderSource(vertexShader,vertexSource)
        GLES30.glShaderSource(fragShader,fragmentSource)
        GLES30.glCompileShader(vertexShader)
        GLES30.glCompileShader(fragShader)
        Log.e(TAG,"${this.javaClass.simpleName} vertShader：${GLES30.glGetShaderInfoLog(vertexShader)} fragShader：${GLES30.glGetShaderInfoLog(fragShader)}")
        GLES30.glAttachShader(program,vertexShader)
        GLES30.glAttachShader(program,fragShader)
        GLES30.glLinkProgram(program)
        GLES30.glUseProgram(program)
        initShaderParameter()

        GLES30.glDetachShader(program,vertexShader)
        GLES30.glDetachShader(program,fragShader)
        GLES30.glDeleteShader(vertexShader)
        GLES30.glDeleteShader(fragShader)
        GLES30.glUseProgram(0)
        GLError.maybeThrowGLException("initProgram", "initProgram：$program")
    }

    abstract fun initShaderParameter()

    fun initTexture(){
        GLES30.glGenTextures(1,textureIds,0)
        GLES30.glBindTexture(textureTarget,textureIds[0])
        GLES30.glTexParameteri(textureTarget, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(textureTarget, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(textureTarget, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST)
        GLES30.glTexParameteri(textureTarget, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glBindTexture(textureTarget,0)

        GLError.maybeThrowGLException("initTexture", "initTexture")
    }
}