package com.jtl.arruler.old;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.google.ar.sceneform.math.Vector3;
import com.jtl.arruler.render.ShaderUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.Matrix.orthoM;

public class PictureRender {
    private static final int FLOAT_SIZE = 4;//每个float 4个字节
    private static final String TAG = PointRender.class.getSimpleName();
    // Shader names.
    private static final String VERTEX_SHADER_NAME = "shaders/picture.vert";
    private static final String FRAGMENT_SHADER_NAME = "shaders/picture.frag";
    private int program;
    private int positionParam;
    private int Color;

    private float[] vpMatrix = new float[16];
    private float[] mvpMatrix = new float[16];
    private float[] projectionMatrix2D = new float[16];
    private FloatBuffer quadVertices;
    //    private int vertexBufferId;
    private int mMvpMatrixHandle;
    private float[] originalPosition = new float[18];//18
    private float[] texturePosition = {
            0.0f,1.0f,
            1.0f,1.0f,
            1.0f,0.0f,
            0.0f,1.0f,
            1.0f,0.0f,
            0.0f,0.0f,
        };


    final float[] colorCorrectionRgba = {1.0f, 0, 0, 1.0f};
    private float T = 0.01f;
    //绘制图片相关
    private final int[] textures = new int[1];
    private static final String A_TEXTURE_COORDINATES = "v_TexCoord";//纹理
    private static final String U_TEXTURE_UNIT = "u_TextureUnit";//纹理
    private int mMTextureUnit;
    private int mTexCoordParam;
    private FloatBuffer quadTextureVertices;
    private int mColor;
    private float mAspectRatio;

    public void createOnGlThread(Context context){
        int vertexShader = ShaderUtil.loadGLShader(TAG, context, GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_NAME);
        int fragmentShader = ShaderUtil.loadGLShader(TAG, context, GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_NAME);
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        GLES20.glUseProgram(program);
        ShaderUtil.checkGLError(TAG, "Program creation");
        //获取顶点着色器的两个参数（用于往着色器里传值，类似指针）
        positionParam = GLES20.glGetAttribLocation(program, "a_Position");
        mMvpMatrixHandle = GLES20.glGetUniformLocation(program, "mvpMatrix");

        mTexCoordParam = GLES20.glGetAttribLocation(program, "a_TexCoord");
        mMTextureUnit = GLES20.glGetAttribLocation(program, "u_TextureUnit");

//        Color = GLES20.glGetUniformLocation(program, "u_Color");

        //        //创建纹理对象
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glGenTextures(textures.length, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        GLES20.glUniform1i(mMTextureUnit, 0);
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        //设置缩小时为三线性过滤
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        //设置放大时为双线性过滤
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        //初始化单位变换矩阵
        Matrix.setIdentityM(mvpMatrix,0);
        int width =  Resources.getSystem().getDisplayMetrics().widthPixels;
        int height =  Resources.getSystem().getDisplayMetrics().heightPixels;

        mAspectRatio = width > height ? (float)width / (float)height : (float)height / (float)width;

        if (width > height) {
            orthoM(projectionMatrix2D,0,-mAspectRatio, mAspectRatio,-1f,1f,-1f,1f);
        }
        else{
            orthoM(projectionMatrix2D,0,-1f,1f,-mAspectRatio, mAspectRatio,-1f,1f);
        }
    }

    public void onDraw() {
        //draw
//        Log.e("yyy");
        GLES20.glUseProgram(program);
        GLES20.glUniformMatrix4fv(mMvpMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glVertexAttribPointer(positionParam, 3, GLES20.GL_FLOAT, false, 0, quadVertices);
//        GLES20.glUniform4fv(Color, 1, colorCorrectionRgba, 0);

        GLES20.glVertexAttribPointer(mTexCoordParam, 2,
                GLES20.GL_FLOAT, false, 0, quadTextureVertices);

        GLES20.glEnableVertexAttribArray(mTexCoordParam);
        GLES20.glEnableVertexAttribArray(positionParam);

//        GLES20.glLineWidth(4);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,6); //(GLES20.GL_TRIANGLES, 0, 6);
    }


    public void upData(float[] p1, float[] p2,float[] viewMatrix,float[] projectionMatrix,String lineLength,@Config.DrawState int state) {
        //计算P点的坐标有两种方式：1.向量的减法。2.向量的加法。
        if(state == Config.DrawState.DOING){
            mColor = 0xFFFFA500;
        }else{
            mColor = 0xFEFEFEFE;
        }
//        Log.e("yyy","viewMatrix:\n"+
//                viewMatrix[0]+" "+ viewMatrix[1] +" "+ viewMatrix[2]+" "+viewMatrix[3]+"\n"+
//                viewMatrix[4]+" "+ viewMatrix[5] +" "+ viewMatrix[6]+" "+viewMatrix[7]+"\n"+
//                viewMatrix[8]+" "+ viewMatrix[9] +" "+ viewMatrix[10]+" "+viewMatrix[11]+"\n"+
//                viewMatrix[12]+" "+ viewMatrix[13] +" "+ viewMatrix[14]+" "+viewMatrix[15]+"\n"+
//                "projectionMatrix:\n"+
//                projectionMatrix[0]+" "+ projectionMatrix[1] +" "+ projectionMatrix[2]+" "+projectionMatrix[3]+"\n"+
//                projectionMatrix[4]+" "+ projectionMatrix[5] +" "+ projectionMatrix[6]+" "+projectionMatrix[7]+"\n"+
//                projectionMatrix[8]+" "+ projectionMatrix[9] +" "+ projectionMatrix[10]+" "+projectionMatrix[11]+"\n"+
//                projectionMatrix[12]+" "+ projectionMatrix[13] +" "+ projectionMatrix[14]+" "+projectionMatrix[15]+"\n"
//        );

        //转成相机矩阵
//        Matrix.invertM(viewMatrix,0,viewMatrix,0);
        //世界坐标系上的点
        float[] point1 = new float[4];
        point1[0] = p1[0];
        point1[1] = p1[1];
        point1[2] = p1[2];
        point1[3] = 1;

        float[] point2 = new float[4];
        point2[0] = p2[0];
        point2[1] = p2[1];
        point2[2] = p2[2];
        point2[3] = 1;
        //1.将世界坐标系上的点转换到相机坐标系：view矩阵乘以点
        Matrix.multiplyMV(point1,0,viewMatrix,0,point1,0);
        Matrix.multiplyMV(point2,0,viewMatrix,0,point2,0);
        //2.求近剪切面的平面方程 Z=0.1,此时计算直线方程中的t值
        float t1 = (float) (-0.1/point1[2]);
        float t2 = (float) (-0.1/point2[2]);
        //3.在计算平面上的交点坐标
        point1[0] = point1[0]*t1;
        point1[1] = point1[1]*t1;
        point1[2] = (float) -0.1;
        point1[3] = 1;

        point2[0] = point2[0]*t2;
        point2[1] = point2[1]*t2;
        point2[2] = (float) -0.1;
        point2[3] = 1;
        //求中心点
        float[] center = new float[3];
        center[0] = (point2[0] + point1[0])/2;
        center[1] = (point2[1] + point1[1])/2;
        center[2] = (point2[2] + point1[2])/2;

        //直线方向向量数组表示
        float[] vectorX = new float[2];
        vectorX[0] = (point2[0] - point1[0]);
        vectorX[1] = (point2[1] - point1[1]);

        float[] vector2X = new float[2];
        vector2X[0] = vectorX[0];
        vector2X[1] = vectorX[1];

        //获取垂直向量
        float[] vector2Y = new float[4];
        vector2Y = MathUtil.rotate90(vector2X);

//        Log.e("yyy","直线垂直向量(没归一化)"+vector2Y[0]+" "+vector2Y[1]+" "+"\n"+
//                "检测两个向量是否垂直："+(vector2X[0]*vector2Y[0] + vector2X[1]*vector2Y[1])+"\n");

//        Log.e("yyy","===================");
        vector2Y = MathUtil.normal2(vector2Y);
        vector2X = MathUtil.normal2(vector2X);

//        Log.e("yyy","直线垂直向量(归一化)"+vector2Y[0]+" "+vector2Y[1]+" "+"\n"+
//                "检测两个向量是否垂直："+(vector2X[0]*vector2Y[0] + vector2X[1]*vector2Y[1])+"\n");


        //y归一化
        vector2Y[0] = (float) (vector2Y[0]/Math.sqrt(vector2Y[0]*vector2Y[0]+vector2Y[1]*vector2Y[1]));
        vector2Y[1] = (float) (vector2Y[1]/Math.sqrt(vector2Y[0]*vector2Y[0]+vector2Y[1]*vector2Y[1]));

        //计算dx ,dy
        final float DX = 0.01f;
        final float DY = 0.005f;
        float dxx = DX/2*vector2X[0];
        float dxy = DX/2*vector2X[1];

        float dyx = DY/2*vector2Y[0];
        float dyy = DY/2*vector2Y[1];

        originalPosition[0] = center[0]-dxx-dyx; originalPosition[1] = center[1]-dxy-dyy; originalPosition[2] = center[2];//p1
        originalPosition[3] = center[0]+dxx-dyx;originalPosition[4] = center[1]+dxy-dyy;originalPosition[5] = center[2];//p2
        originalPosition[6] = center[0]+dxx+dyx;originalPosition[7] = center[1]+dxy+dyy;originalPosition[8] = center[2];//p3
        originalPosition[9] = center[0]-dxx-dyx;originalPosition[10] = center[1]-dxy-dyy;originalPosition[11] = center[2];
        originalPosition[12] = center[0]+dxx+dyx;originalPosition[13] = center[1]+dxy+dyy;originalPosition[14] = center[2];
        originalPosition[15] = center[0]-dxx+dyx;originalPosition[16] = center[1]-dxy+dyy;originalPosition[17] = center[2];//p4

        //计算贴图坐标
        if(originalPosition[6]>originalPosition[15]){
            texturePosition[0] = 0.0f;texturePosition[1] = 1.0f;//p1
            texturePosition[2] = 1.0f;texturePosition[3] = 1.0f;//p2
            texturePosition[4] = 1.0f;texturePosition[5] = 0.0f;//p3
            texturePosition[6] = 0.0f;texturePosition[7] = 1.0f;//p1
            texturePosition[8] = 1.0f;texturePosition[9] = 0.0f;//p3
            texturePosition[10] = 0.0f;texturePosition[11] = 0.0f;//p4
        }else{
            texturePosition[0] = 1.0f;texturePosition[1] = 0.0f;//p3
            texturePosition[2] = 0.0f;texturePosition[3] = 0.0f;//p4
            texturePosition[4] = 0.0f;texturePosition[5] = 1.0f;//p1
            texturePosition[6] = 1.0f;texturePosition[7] = 0.0f;//p3
            texturePosition[8] = 0.0f;texturePosition[9] = 1.0f;//p1
            texturePosition[10] = 1.0f;texturePosition[11] = 1.0f;//p2
        }

//        texturePosition = {
//                0.0f,1.0f,
//                1.0f,1.0f,
//                1.0f,0.0f,
//                0.0f,1.0f,
//                1.0f,0.0f,
//                0.0f,0.0f,
//        };

        mvpMatrix = projectionMatrix;

        float[] danwei = new float[3];
        danwei[0] = originalPosition[6]-originalPosition[15];
        danwei[1] = originalPosition[7]-originalPosition[16];
        danwei[2] = originalPosition[8]-originalPosition[17];
        danwei[0] = (float) (danwei[0]/Math.sqrt(danwei[0]*danwei[0]+danwei[1]*danwei[1]));
        danwei[1] = (float) (danwei[1]/Math.sqrt(danwei[0]*danwei[0]+danwei[1]*danwei[1]));

        //坐标点的缓冲流
        ByteBuffer bbVertices  = ByteBuffer.allocateDirect(originalPosition.length * FLOAT_SIZE);
        bbVertices.order(ByteOrder.nativeOrder());
        quadVertices = bbVertices.asFloatBuffer();
        quadVertices.put(originalPosition);
        quadVertices.position(0);

        //绘制图片
        Bitmap bitmap = Utils.getNewBitMap(mColor,Utils.dip2px(20),lineLength);



        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        bitmap.recycle();

        //贴图坐标的缓冲流
        ByteBuffer vertexBuffer = ByteBuffer.allocateDirect(texturePosition.length * FLOAT_SIZE);
        vertexBuffer.order(ByteOrder.nativeOrder());
        quadTextureVertices = vertexBuffer.asFloatBuffer();
        quadTextureVertices.put(texturePosition);
        quadTextureVertices.position(0);


    }

    /**
     * 效果图像现实的贴图会一直朝上。
     * 计算贴图的旋转矩阵中的Vx坐标
     * @param vz
     * @return
     */
    private float[] calculateVx(float[] vz) {
        float [] vx = new float[3];
        float [] vy1 = new float[3];
        vy1[0] = 0;
        vy1[1] = 1;
        vy1[2] = 0;
        Vector3 vectorVY1 = new Vector3(vy1[0],vy1[1],vy1[2]);
        Vector3 vectorVZ = new Vector3(vz[0],vz[1],vz[2]);
        Vector3 vectorx = Vector3.cross(vectorVY1,vectorVZ).normalized();
        vx[0] = vectorx.x;
        vx[1] = vectorx.y;
        vx[2] = vectorx.z;
        return vx;
    }

}
