package com.jtl.arruler;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;


import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.jtl.arruler.helper.DisplayRotationHelper;
import com.jtl.arruler.helper.SessionHelper;
import com.jtl.arruler.render.BackgroundRenderer;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 作者:jtl
 * 日期:Created in 2019/5/2 16:50
 * 描述:
 * 更改:
 */
public class ArRulerSurface extends GLSurfaceView implements GLSurfaceView.Renderer {
    private Session mSession;
    private Frame mFrame;
    private BackgroundRenderer mBackgroundRenderer;
    private DisplayRotationHelper displayRotationHelper;
    public ArRulerSurface(Context context) {
        this(context,null);
    }

    public ArRulerSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        this.setEGLContextClientVersion(2);
        this.setPreserveEGLContextOnPause(true);
        this.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        this.setRenderer(this);
        this.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        displayRotationHelper=new DisplayRotationHelper(getContext());
        mBackgroundRenderer=new BackgroundRenderer();
        try {
            mBackgroundRenderer.createOnGlThread(getContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mSession=SessionHelper.getInstance().getSession(getContext());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        GLES20.glViewport(0,0,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);
        if (mSession==null){
            return;
        }
        displayRotationHelper.updateSessionIfNeeded(mSession);
        try {
            mSession.setCameraTextureName(mBackgroundRenderer.getTextureId());
            mFrame= mSession.update();
            mBackgroundRenderer.draw(mFrame);
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        displayRotationHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        displayRotationHelper.onPause();
    }
}
