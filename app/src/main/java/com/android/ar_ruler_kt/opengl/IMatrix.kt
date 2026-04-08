package com.android.ar_ruler_kt.opengl

import android.opengl.Matrix

/**
 * @author：TianLong
 * @date：2022/7/1 15:36
 * @detail：矩阵接口
 */
interface IMatrix {
    var matrix: FloatArray

    fun upDateMatrix(pose:FloatArray = FloatArray(16),viewMatrix:FloatArray = FloatArray(16),projectMatrix:FloatArray = FloatArray(16)){
        Matrix.setIdentityM(matrix,0)
        Matrix.multiplyMM(matrix,0,viewMatrix,0,pose,0)
        Matrix.multiplyMM(matrix,0,projectMatrix,0,matrix,0)
    }

    fun upDateMatrix(viewMatrix:FloatArray = FloatArray(16),projectMatrix:FloatArray = FloatArray(16)){
        Matrix.setIdentityM(matrix,0)
        Matrix.multiplyMM(matrix,0,projectMatrix,0,viewMatrix,0)
    }

    fun upDateVPMatrix(position:FloatArray = FloatArray(4),viewMatrix:FloatArray = FloatArray(16),projectMatrix:FloatArray = FloatArray(16)){
        Matrix.setIdentityM(matrix,0)
        Matrix.multiplyMV(matrix,0,viewMatrix,0,position,0)
        Matrix.multiplyMM(matrix,0,projectMatrix,0,matrix,0)
    }

    fun upDatePMatrix(projectMatrix:FloatArray = FloatArray(16)){
        Matrix.setIdentityM(matrix,0)
        Matrix.multiplyMM(matrix,0,projectMatrix,0,matrix,0)
    }
}