package com.jtl.arruler.old;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.jtl.arruler.render.ShaderUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.glGetUniformLocation;

public class PointRender {
    private static final int FLOAT_SIZE = 4;//每个float 4个字节
    private static final String TAG = PointRender.class.getSimpleName();
    // Shader names.
    private static final String VERTEX_SHADER_NAME = "shaders/point.vert";
    private static final String FRAGMENT_SHADER_NAME = "shaders/point.frag";
    private int program;
    private int positionParam;
    private int Color;


    private float[] vpMatrix = new float[16];
    private FloatBuffer quadVertices;
    //    private int vertexBufferId;
    private int mMvpMatrixHandle;

    private float[] originalPosition = {0.0f, 0.0f, 0.0f};

    //颜色
    private float[] colorCorrectionRgba = new float[4];

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

        Color = glGetUniformLocation(program, "u_Color");
    }

    public void onDraw(@Config.DrawState int state) {
        ShaderUtil.checkGLError(TAG, "Before draw");

        if(state == Config.DrawState.DOING){
            colorCorrectionRgba[0] = Config.COLOR_ORANGE_R;
            colorCorrectionRgba[1] = Config.COLOR_ORANGE_G;
            colorCorrectionRgba[2] = Config.COLOR_ORANGE_B;
            colorCorrectionRgba[3] = 1.0f;
        }else{
            colorCorrectionRgba[0] = Config.COLOR_WHITE_R;
            colorCorrectionRgba[1] = Config.COLOR_WHITE_G;
            colorCorrectionRgba[2] = Config.COLOR_WHITE_B;
            colorCorrectionRgba[3] = 1.0f;
        }

        GLES20.glUseProgram(program);
        GLES20.glUniformMatrix4fv(mMvpMatrixHandle, 1, false, vpMatrix, 0);
        GLES20.glVertexAttribPointer(positionParam, 3, GLES20.GL_FLOAT, false, 0, quadVertices);

        GLES20.glEnableVertexAttribArray(positionParam);
        GLES20.glUniform4fv(Color, 1, colorCorrectionRgba, 0);

//        GLES20.glEnable(GL_POINT_SMOOTH);
//        GLES20.glEnable(GL_BLEND);
//        GLES20.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
    }

    public void upData(float[] modleMatrix,float[] viewMatrix,float[] projectionMatrix) {
        originalPosition = modleMatrix;

//        Matrix.multiplyMM(modleMatrix, 0, viewMatrix, 0, modleMatrix, 0);
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
//        vpMatrix = viewMatrix;

//        mvpMatrix = mvpMatrix;
        ByteBuffer bbVertices = ByteBuffer.allocateDirect(3 * FLOAT_SIZE);
        bbVertices.order(ByteOrder.nativeOrder());
        quadVertices = bbVertices.asFloatBuffer();
        quadVertices.put(originalPosition);
        quadVertices.position(0);
//        initVertexData();
    }

}
