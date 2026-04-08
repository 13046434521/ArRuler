package com.android.ar_ruler_kt.opengl

/**
 * @author：TianLong
 * @date：2022/6/27 22:55
 * @detail：基础 Renderer 接口
 */
interface IBaseRenderer {
     val TAG :String
          get() = this.javaClass.simpleName
     fun onSurfaceCreated()
     fun onSurfaceChanged( width: Int, height: Int)
     fun onDrawFrame()
}