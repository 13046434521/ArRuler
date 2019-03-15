package com.jtl.arruler.detail;

import android.opengl.GLSurfaceView;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.jtl.androidhelper.PermissionHelper;
import com.jtl.androidhelper.SnackBarHelper;
import com.jtl.arruler.R;

public class ArRulerActivity extends AppCompatActivity implements ArRulerContract.View {
    private ArRulerPresenter mArRulerPresenter;
    private GLSurfaceView mShowGLSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_ruler);
        initPermission();
        init();
    }

    private void init(){
        mArRulerPresenter=new ArRulerPresenter(this);

        mShowGLSurface=findViewById(R.id.gl_ruler_show);

    }

    private void initPermission(){
        if (!PermissionHelper.hasCameraPermission(this)){
            PermissionHelper.requestCameraPermission(this);
        }
    }

    @Override
    public void initSession() {
        mArRulerPresenter.initSession(this);
    }

    @MainThread
    @Override
    public void showToast(String msg) {
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSnackBar(String msg) {
        SnackBarHelper.getInstance().showMessage(this,msg);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mArRulerPresenter.resumeSession();
        mShowGLSurface.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mArRulerPresenter.pauseSession();
        mShowGLSurface.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mArRulerPresenter.onDetach();
    }
}
