/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jtl.arruler.render;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.google.ar.core.Session;
import com.google.ar.core.Frame;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * This class renders the AR background from camera feed. It creates and hosts the texture given to
 * ARCore to be filled with the camera image.
 */
public class BackgroundRenderer {
    private static final String TAG = BackgroundRenderer.class.getSimpleName();

    // Shader names.
    private static final String VERTEX_SHADER_NAME = "shaders/screenquad.vert";
    private static final String FRAGMENT_SHADER_NAME = "shaders/screenquad.frag";

    private static final int COORDS_PER_VERTEX = 3;//每个顶点坐标数量
    private static final int TEXCOORDS_PER_VERTEX = 2;//每个顶点的TEX坐标
    private static final int FLOAT_SIZE = 4;//每个float 4个字节

    private FloatBuffer quadVertices;//四个定点
    private FloatBuffer quadTexCoord;//
    private FloatBuffer quadTexCoordTransformed;

    private int quadProgram;

    private int quadPositionParam;
    private int quadTexCoordParam;
    private int textureId = -1;

    public BackgroundRenderer() {
    }

    public int getTextureId() {
        return textureId;
    }

    /**
     * Allocates and initializes OpenGL resources needed by the background renderer. Must be called on
     * the OpenGL thread, typically in {@link GLSurfaceView.Renderer #onSurfaceCreated(GL10,
     * EGLConfig)}.
     *
     * @param context Needed to access shader source.
     */
    public void createOnGlThread(Context context) throws IOException {
        // Generate the background texture.
        //用于存储返回的纹理对象ID
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        textureId = textures[0];
        //纹理目标
        int textureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
        GLES20.glBindTexture(textureTarget, textureId);
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        //设置放大过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        //顶点数
        int numVertices = 4;
        if (numVertices != QUAD_COORDS.length / COORDS_PER_VERTEX) {
            throw new RuntimeException("Unexpected number of vertices in BackgroundRenderer.");
        }
        //创建顶点的内存空间，并将归一化的顶点转化为 floatBuffer
        ByteBuffer bbVertices = ByteBuffer.allocateDirect(QUAD_COORDS.length * FLOAT_SIZE);
        bbVertices.order(ByteOrder.nativeOrder());
        quadVertices = bbVertices.asFloatBuffer();
        quadVertices.put(QUAD_COORDS);
        quadVertices.position(0);

        //创建着色顶点的内存空间，并将归一化的顶点转化为 floatBuffer
        ByteBuffer bbTexCoords =
                ByteBuffer.allocateDirect(numVertices * TEXCOORDS_PER_VERTEX * FLOAT_SIZE);
        bbTexCoords.order(ByteOrder.nativeOrder());
        quadTexCoord = bbTexCoords.asFloatBuffer();
        quadTexCoord.put(QUAD_TEXCOORDS);
        quadTexCoord.position(0);
        //比上一个少两步为啥？
        ByteBuffer bbTexCoordsTransformed =
                ByteBuffer.allocateDirect(numVertices * TEXCOORDS_PER_VERTEX * FLOAT_SIZE);
        bbTexCoordsTransformed.order(ByteOrder.nativeOrder());
        quadTexCoordTransformed = bbTexCoordsTransformed.asFloatBuffer();
        //定点着色器创建
        int vertexShader =
                ShaderUtil.loadGLShader(TAG, context, GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_NAME);
        //片源着色器创建
        int fragmentShader =
                ShaderUtil.loadGLShader(TAG, context, GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_NAME);
        //创建一个空的OpengGL程序
        quadProgram = GLES20.glCreateProgram();
        //将顶点着色器加入到程序
        GLES20.glAttachShader(quadProgram, vertexShader);
        //将片元着色器加入到程序中
        GLES20.glAttachShader(quadProgram, fragmentShader);
        //连接到着色器程序
        GLES20.glLinkProgram(quadProgram);
        //制定使用某套shader程序，向顶点着色器中传递数据前需要调用这个房方法
        GLES20.glUseProgram(quadProgram);

        ShaderUtil.checkGLError(TAG, "Program creation");
        //获取顶点着色器的两个参数（用于往着色器里传值，类似指针）
        quadPositionParam = GLES20.glGetAttribLocation(quadProgram, "a_Position");
        quadTexCoordParam = GLES20.glGetAttribLocation(quadProgram, "a_TexCoord");

        ShaderUtil.checkGLError(TAG, "Program parameters");
    }

    /**
     * Draws the AR background image. The image will be drawn such that virtual content rendered with
     * the matrices provided by {@link com.google.ar.core.Camera#getViewMatrix(float[], int)} and
     * {@link com.google.ar.core.Camera#getProjectionMatrix(float[], int, float, float)} will
     * accurately follow static physical objects. This must be called <b>before</b> drawing virtual
     * content.
     *
     * @param frame The last {@code Frame} returned by {@link Session #update()}.
     */
    public void draw(Frame frame) {
        // If display rotation changed (also includes view size change), we need to re-query the uv
        // coordinates for the screen rect, as they may have changed as well.
        if (frame.hasDisplayGeometryChanged()) {
            Log.e("backGroundRender发生改变frame.hasDisplayGeometryChanged()", frame.hasDisplayGeometryChanged() + "");
            frame.transformDisplayUvCoords(quadTexCoord, quadTexCoordTransformed);
        }
        //相机没有生成第一帧，就会抑制渲染
        if (frame.getTimestamp() == 0) {
            // Suppress rendering if the camera did not produce the first frame yet. This is to avoid
            // drawing possible leftover data from previous sessions if the texture is reused.
            return;
        }

        // No need to test or write depth, the screen quad has arbitrary depth, and is expected
        // to be drawn first.
        //关闭深度检测
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        //深度缓冲区的可读性为不可读
        GLES20.glDepthMask(false);
        //绑定，后续操作都是这个纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        //顶点着色器修改数据的开关
        GLES20.glUseProgram(quadProgram);

        /**
         * 设置着色器如何从quadVertices中读取坐标。（如何渲染）
         */
        // Set the vertex positions.
        //顶点位置变量，表示一个顶点的位数，顶点坐标数据类型，？？？，偏移量，归一化顶点坐标的floatBuffer
        GLES20.glVertexAttribPointer(
                quadPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, quadVertices);

        // Set the texture coordinates.
        //材质位置变量
        GLES20.glVertexAttribPointer(
                quadTexCoordParam,
                TEXCOORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                0,
                quadTexCoordTransformed);

        // Enable vertex arrays
        //使用着色器
        GLES20.glEnableVertexAttribArray(quadPositionParam);
        GLES20.glEnableVertexAttribArray(quadTexCoordParam);

        //以三角的形式绘制从0开始绘制4个坐标，对于顶点就是12个数，对于纹理是8个数
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        // Disable vertex arrays
        //禁用着色器
        GLES20.glDisableVertexAttribArray(quadPositionParam);
        GLES20.glDisableVertexAttribArray(quadTexCoordParam);

        // Restore the depth state for further drawing.
        //恢复深度状态
        GLES20.glDepthMask(true);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        ShaderUtil.checkGLError(TAG, "Draw");
    }

    private static final float[] QUAD_COORDS =
            new float[]{
                    -1.0f, -1.0f, 0.0f,//左下
                    -1.0f, +1.0f, 0.0f,//左上
                    +1.0f, -1.0f, 0.0f,//右下
                    +1.0f, +1.0f, 0.0f,//右上
            };

    private static final float[] QUAD_TEXCOORDS =
            new float[]{
                    0.0f, 1.0f,//左下
                    0.0f, 0.0f,//左上
                    1.0f, 1.0f,//右下
                    1.0f, 0.0f,//右上
            };
}
