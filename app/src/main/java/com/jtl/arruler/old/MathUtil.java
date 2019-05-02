package com.jtl.arruler.old;

/**
 * 二维坐标归一化，旋转的数学库
 */
public class MathUtil {
    /**
     * 二维坐标归一化
     * @param vector
     * @return
     */
    public static float[] normal2(float[] vector){
        float[] a = new float[2];
        a[0] = (float) (vector[0]/Math.sqrt(vector[0]*vector[0]+vector[1]*vector[1]));
        a[1] = (float) (vector[1]/Math.sqrt(vector[0]*vector[0]+vector[1]*vector[1]));
        return a;
    }

    /**
     * 90度的旋转矩阵
     */
    public static float[] rotate=
            {0,-1, 1,0};

    /**
     * 二维坐标的旋转
     * @param vector
     * @return
     */
    public static float[] rotate90(float[] vector){
        float[] a = new float[2];
        a[0] = vector[0]*rotate[0]+vector[1]*rotate[1];
        a[1] = vector[0]*rotate[2]+vector[1]*rotate[3];
        return a;
    }

    public static float pointMultiplication(float[] vector1,float[] vector2){
        return vector1[0]*vector2[0]+vector1[1]*vector2[1]+vector1[2]*vector2[2];
    }
}
