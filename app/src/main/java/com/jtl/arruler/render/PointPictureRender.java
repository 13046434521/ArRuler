package com.jtl.arruler.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.jtl.arruler.base.BaseRender;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 作者:jtl
 * 日期:Created in 2019/5/3 0:32
 * 描述:
 * 更改:
 */
public class PointPictureRender extends BaseRender {
    private static final String TAG = PointPictureRender.class.getSimpleName();
    private static final String VERTEX_SHADER_NAME = "shaders/point_circle.vert";
    private static final String FRAGMENT_SHADER_NAME = "shaders/point_circle.frag";
    private static final int FLOAT_SIZE = 4;//每个float 4个字节
    private FloatBuffer quadVertices;
    private FloatBuffer quadTextureVertices;
    private Bitmap mBitmap;
    private int program;
    //Shader中的句柄
    private int a_Position;
    private int mvpMatrix;
    private int a_TexCoord;
    private int u_TextureUnit;

    private int[] texture = new int[1];
    private float[] mMvpMatrix = new float[16];
    //    private float[] originalPosition = new float[18];
//    private float[] originalPosition = new float[18];
    private static final float position = 0.06f;
    private static final float[] originalPosition =
            new float[]{
                    -position, 0.0f, -position,
                    +position, 0.0f, -position,
                    +position, 0.0f, +position,
                    -position, 0.0f, -position,
                    +position, 0.0f, +position,
                    -position, 0.0f, +position,
            };
    private static final float[] texturePosition = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
    };

    @Override
    public void createOnGlThread(Context context) {
        try {
            mBitmap = BitmapFactory.decodeStream(context.getAssets().open("models/pointCircle.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        loadShader(context);
        createTexture();
    }

    private void loadShader(Context context) {
        int vertexShader = ShaderUtil.loadGLShader(TAG, context, GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_NAME);
        int fragmentShader = ShaderUtil.loadGLShader(TAG, context, GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_NAME);
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        GLES20.glUseProgram(program);
        //获取着色器中的参数句柄
        a_Position = GLES20.glGetAttribLocation(program, "a_Position");
        a_TexCoord = GLES20.glGetAttribLocation(program, "a_TexCoord");
        mvpMatrix = GLES20.glGetUniformLocation(program, "mvpMatrix");
        u_TextureUnit = GLES20.glGetAttribLocation(program, "u_TextureUnit");
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);
        ShaderUtil.checkGLError(TAG, "loadShader Error");
    }

    private void createTexture() {
        GLES20.glGenTextures(texture.length, texture, 0);//生成纹理id
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);//表示把活动的纹理单元设置为纹理单元0
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);//将textureId指向的纹理绑定到纹理单元0
        GLES20.glUniform1i(u_TextureUnit, 0);//把选定的纹理单元传递给片元着色器中的u_TextureUnit（sampler2D）

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);//设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);//设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);//设置缩小时为双线性过滤
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);//设置放大时为双线性过滤

        ShaderUtil.checkGLError(TAG, "createTexture Error");
    }

    public void upData(float[] pose, float[] viewMatrix, float[] projectMatrix) {
        Matrix.setIdentityM(mMvpMatrix, 0);
        Matrix.multiplyMM(mMvpMatrix, 0, viewMatrix, 0, pose, 0);//视矩阵 * 世界坐标 = 相机坐标系坐标
        Matrix.multiplyMM(mMvpMatrix, 0, projectMatrix, 0, mMvpMatrix, 0);// 相机坐标 * 投影矩阵 = 裁剪坐标系坐标

//        originalPosition[0] = -0.06f;
//        originalPosition[1] = 0f;
//        originalPosition[2] = -0.06f;
//        originalPosition[3] = 0.06f;
//        originalPosition[4] = 0f;
//        originalPosition[5] = -0.06f;
//        originalPosition[6] = 0.06f;
//        originalPosition[7] = 0f;
//        originalPosition[8] = 0.06f;
//        originalPosition[9] = -0.06f;
//        originalPosition[10] = 0f;
//        originalPosition[11] = -0.06f;
//        originalPosition[12] = 0.06f;
//        originalPosition[13] = 0f;
//        originalPosition[14] = 0.06f;
//        originalPosition[15] = -0.06f;
//        originalPosition[16] = 0f;
//        originalPosition[17] = 0.06f;


        //坐标点的缓冲流
        ByteBuffer bbVertices = ByteBuffer.allocateDirect(originalPosition.length * FLOAT_SIZE);
        bbVertices.order(ByteOrder.nativeOrder());
        quadVertices = bbVertices.asFloatBuffer();
        quadVertices.put(originalPosition);
        quadVertices.position(0);

        //贴图坐标的缓冲流
        ByteBuffer vertexBuffer = ByteBuffer.allocateDirect(texturePosition.length * FLOAT_SIZE);
        vertexBuffer.order(ByteOrder.nativeOrder());
        quadTextureVertices = vertexBuffer.asFloatBuffer();
        quadTextureVertices.put(texturePosition);
        quadTextureVertices.position(0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

        ShaderUtil.checkGLError(TAG, "upData Error");
    }

    public void onDraw() {
        GLES20.glUseProgram(program);
        GLES20.glUniformMatrix4fv(mvpMatrix, 1, false, mMvpMatrix, 0);
        GLES20.glVertexAttribPointer(a_Position, 3, GLES20.GL_FLOAT, false, 0, quadVertices);
        GLES20.glVertexAttribPointer(a_TexCoord, 2, GLES20.GL_FLOAT, false, 0, quadTextureVertices);

        GLES20.glEnableVertexAttribArray(a_Position);
        GLES20.glEnableVertexAttribArray(a_TexCoord);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glDepthMask(false);//否则会出现遮挡效果
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        GLES20.glDisableVertexAttribArray(a_Position);
        GLES20.glDisableVertexAttribArray(a_TexCoord);
        GLES20.glDisable(GLES20.GL_BLEND);
        ShaderUtil.checkGLError(TAG, "onDraw Error");
    }
}
