package com.android.ar_ruler_kt.opengl

import android.content.Context
import com.google.ar.core.Session

/**
 * @author：TianLong
 * @date：2022/6/28 14:21
 * @detail：着色器变量接口
 */
interface ShaderImpl {
    var context:Context

    var vertexPath: String

    var fragmentPath: String

    var vertexSource: String

    var fragmentSource: String
}