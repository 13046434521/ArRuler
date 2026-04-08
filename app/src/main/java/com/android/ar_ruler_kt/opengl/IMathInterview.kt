package com.android.ar_ruler_kt.opengl

import android.util.Log
import com.google.ar.core.Pose
import kotlin.math.*

/**
 * @author：TianLong
 * @date：2022/7/9 11:57
 * @detail：Math 辅助 接口
 */
interface IMathInterview {
    fun length(pose1: Pose, pose2: Pose): Double {
        val tempX = (pose1.tx() - pose2.tx()).toDouble().pow(2.0)
        val tempY = (pose1.ty() - pose2.ty()).toDouble().pow(2.0)
        val tempZ = (pose1.tz() - pose2.tz()).toDouble().pow(2.0)

        val length = sqrt(tempX + tempY + tempZ)

        Log.w("IMathInterview","$length")
        return length
    }

    /**
     * 90度的旋转矩阵
     */
    val rotate get() = floatArrayOf(0f,-1f,1f,0f)

    /**
     * 二维坐标的旋转：顺时针为正，逆时针为负
     * @param vector x0 = x*cos(β) - y*sin(β)
     *               y0 = y*cos(β) + x*sin(β)
     * @return
     */
    fun rotate90(vector: FloatArray): FloatArray {
        val a = FloatArray(2)
        a[0] = vector[0] * rotate[0] + vector[1] * rotate[1]
        a[1] = vector[0] * rotate[2] + vector[1] * rotate[3]
        return a
    }


    /**
     * 将向量旋转一定角度后返回结果向量，默认90度
     * @param vector FloatArray 被旋转向量
     * @param angle Float 角度
     * @return FloatArray 旋转后向量
     */
    fun rotate(vector: FloatArray,angle:Float = 90.0f):FloatArray{
        val res = FloatArray(2)
        val t1 = Math.cos(angle.toDouble())
        val t2 = cos(angle)
        val t3 = Math.sin(angle.toDouble())
        val t4 = sin(angle)
        res[0] = vector[0] * cos(angle) - vector[1] * sin(angle)
        res[1] = vector[0] * sin(angle) + vector[1] * cos(angle)
        return res
    }

    /**
     * 二维向量归一化
     * @param vector FloatArray
     * @return FloatArray
     */
    fun normal(vector: FloatArray):FloatArray{
        val r = sqrt(vector[0].pow(2) + vector[1].pow(2))

        val res = FloatArray(2)

        res[0] = vector[0] / r
        res[1] = vector[1] / r

        return res
    }

    /**
     * 求出目标三维点在近剪切面上时点的位置
     * @param resVector FloatArray 结果向量(四维向量)
     * @param destVector FloatArray 目标向量(四维向量)
     * @param near Float 近剪切面
     */
    fun mappingNear(resVector:FloatArray,destVector: FloatArray,near :Float= -0.1f) {
        val threshold = near / destVector[2]

        resVector[0] = destVector[0] * threshold
        resVector[1] = destVector[1] * threshold
        resVector[2] = near
        resVector[3] = 1.0f
    }
}