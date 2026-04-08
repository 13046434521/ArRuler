package com.android.ar_ruler_kt

/**
 * @author：TianLong
 * @date：2022/7/7 15:56
 * @detail：view 操作接口
 */
interface IViewInterface {
    fun detectSuccess(msg:String)

    fun detectFailed(msg:String)
}