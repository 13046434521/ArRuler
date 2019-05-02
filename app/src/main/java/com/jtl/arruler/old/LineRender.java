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

public class LineRender {
    private static final int FLOAT_SIZE = 4;//每个float 4个字节
    private static final String TAG = LineRender.class.getSimpleName();
    // Shader names.
    private static final String VERTEX_SHADER_NAME = "shaders/line.vert";
    private static final String FRAGMENT_SHADER_NAME = "shaders/line.frag";
    private int program;

    private int positionParam;

    private int Color;


    private float[] mvpMatrix = new float[16];
    private FloatBuffer quadVertices;
    //    private int vertexBufferId;
    private int mMvpMatrixHandle;

    private float[] originalPosition = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};

    public void createOnGlThread(Context context) {

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
//
        if(state == Config.DrawState.DOING){
            colorCorrectionRgba[0] = Config.COLOR_ORANGE_R;
            colorCorrectionRgba[1] = Config.COLOR_ORANGE_G;
            colorCorrectionRgba[2] = Config.COLOR_ORANGE_B;
            colorCorrectionRgba[3] = 1.0f;
        }else{
            colorCorrectionRgba[0] = 1.0f;
            colorCorrectionRgba[1] = 1.0f;
            colorCorrectionRgba[2] = 1.0f;
            colorCorrectionRgba[3] = 1.0f;
        }
        GLES20.glUseProgram(program);
        GLES20.glUniformMatrix4fv(mMvpMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glVertexAttribPointer(positionParam, 3, GLES20.GL_FLOAT, false, 0, quadVertices);
        GLES20.glEnableVertexAttribArray(positionParam);

        GLES20.glUniform4fv(Color, 1, colorCorrectionRgba, 0);
        GLES20.glLineWidth(10);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);
    }

    public void upData(float[] viewMatrix, float[] projectionMatrix, float[] p1, float[] p2) {
        //计算向量
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        originalPosition[0] = p1[0];
        originalPosition[1] = p1[1];
        originalPosition[2] = p1[2];
        originalPosition[3] = p2[0];
        originalPosition[4] = p2[1];
        originalPosition[5] = p2[2];

        ByteBuffer bbVertices = ByteBuffer.allocateDirect(6 * FLOAT_SIZE);
        bbVertices.order(ByteOrder.nativeOrder());
        quadVertices = bbVertices.asFloatBuffer();
        quadVertices.put(originalPosition);
        quadVertices.position(0);
//        initVertexData();
    }

//    画球的方法
//    final int angleSpan = 10;// 将球进行单位切分的角度
//    private static final float UNIT_SIZE = 1.0f;// 单位尺寸
//    private float r = 0.6f; // 球的半径
//    int vCount = 0;// 顶点个数，先初始化为0
//    // 数组中每个顶点的坐标数
//    private static final int COORDS_PER_VERTEX = 3;
//
//    private static final int BYTES_PER_FLOAT = 4;
//
//    public void initVertexData() {
//        ArrayList<Float> alVertix = new ArrayList<Float>();// 存放顶点坐标的ArrayList
//        for (int vAngle = 0; vAngle < 180; vAngle = vAngle + angleSpan)// 垂直方向angleSpan度一份
//        {
//            for (int hAngle = 0; hAngle <= 360; hAngle = hAngle + angleSpan)// 水平方向angleSpan度一份
//            {
//                // 纵向横向各到一个角度后计算对应的此点在球面上的坐标
//                float x0 = (float) (r * UNIT_SIZE
//                        * Math.sin(Math.toRadians(vAngle)) * Math.cos(Math
//                        .toRadians(hAngle)));
//                float y0 = (float) (r * UNIT_SIZE
//                        * Math.sin(Math.toRadians(vAngle)) * Math.sin(Math
//                        .toRadians(hAngle)));
//                float z0 = (float) (r * UNIT_SIZE * Math.cos(Math
//                        .toRadians(vAngle)));
//                // Log.w("x0 y0 z0","" + x0 + "  "+y0+ "  " +z0);
//
//                float x1 = (float) (r * UNIT_SIZE
//                        * Math.sin(Math.toRadians(vAngle)) * Math.cos(Math
//                        .toRadians(hAngle + angleSpan)));
//                float y1 = (float) (r * UNIT_SIZE
//                        * Math.sin(Math.toRadians(vAngle)) * Math.sin(Math
//                        .toRadians(hAngle + angleSpan)));
//                float z1 = (float) (r * UNIT_SIZE * Math.cos(Math
//                        .toRadians(vAngle)));
//                // Log.w("x1 y1 z1","" + x1 + "  "+y1+ "  " +z1);
//
//                float x2 = (float) (r * UNIT_SIZE
//                        * Math.sin(Math.toRadians(vAngle + angleSpan)) * Math
//                        .cos(Math.toRadians(hAngle + angleSpan)));
//                float y2 = (float) (r * UNIT_SIZE
//                        * Math.sin(Math.toRadians(vAngle + angleSpan)) * Math
//                        .sin(Math.toRadians(hAngle + angleSpan)));
//                float z2 = (float) (r * UNIT_SIZE * Math.cos(Math
//                        .toRadians(vAngle + angleSpan)));
//                // Log.w("x2 y2 z2","" + x2 + "  "+y2+ "  " +z2);
//                float x3 = (float) (r * UNIT_SIZE
//                        * Math.sin(Math.toRadians(vAngle + angleSpan)) * Math
//                        .cos(Math.toRadians(hAngle)));
//                float y3 = (float) (r * UNIT_SIZE
//                        * Math.sin(Math.toRadians(vAngle + angleSpan)) * Math
//                        .sin(Math.toRadians(hAngle)));
//                float z3 = (float) (r * UNIT_SIZE * Math.cos(Math
//                        .toRadians(vAngle + angleSpan)));
//                // Log.w("x3 y3 z3","" + x3 + "  "+y3+ "  " +z3);
//                // 将计算出来的XYZ坐标加入存放顶点坐标的ArrayList
//                alVertix.add(x1);
//                alVertix.add(y1);
//                alVertix.add(z1);
//                alVertix.add(x3);
//                alVertix.add(y3);
//                alVertix.add(z3);
//                alVertix.add(x0);
//                alVertix.add(y0);
//                alVertix.add(z0);
//
//                alVertix.add(x1);
//                alVertix.add(y1);
//                alVertix.add(z1);
//                alVertix.add(x2);
//                alVertix.add(y2);
//                alVertix.add(z2);
//                alVertix.add(x3);
//                alVertix.add(y3);
//                alVertix.add(z3);
//            }
//        }
//        vCount = alVertix.size() / COORDS_PER_VERTEX;// 顶点的数量
//        // 将alVertix中的坐标值转存到一个float数组中
//        float vertices[] = new float[vCount * COORDS_PER_VERTEX];
//        for (int i = 0; i < alVertix.size(); i++) {
//            vertices[i] = alVertix.get(i);
//        }
//        quadVertices = ByteBuffer
//                .allocateDirect((vertices.length + 1) * BYTES_PER_FLOAT)
//                .order(ByteOrder.nativeOrder())
//                .asFloatBuffer();
//        // 把坐标们加入FloatBuffer中
//        quadVertices.put(vertices);
//        // 设置buffer，从第一个坐标开始读
//        quadVertices.position(0);
//    }


    final float[] colorCorrectionRgba = {1.0f, 0, 0, 1.0f};
}
