package com.jtl.arruler.detail;

import android.content.pm.PackageManager;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.ar.sceneform.ArSceneView;
import com.jtl.androidhelper.PermissionHelper;
import com.jtl.androidhelper.SnackBarHelper;
import com.jtl.arruler.ArRulerSurface;
import com.jtl.arruler.R;
import com.jtl.arruler.helper.SessionHelper;

public class ArRulerActivity extends AppCompatActivity implements ArRulerContract.View {
    private ArRulerPresenter mArRulerPresenter;
    private ArRulerSurface mShowArRulerSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_ruler);
        initPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mArRulerPresenter!=null){
            mArRulerPresenter.resumeSession();
        }
        if (mShowArRulerSurface!=null){
            mShowArRulerSurface.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mArRulerPresenter!=null){
            mArRulerPresenter.pauseSession();
        }

        if (mShowArRulerSurface!=null){
            mShowArRulerSurface.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mArRulerPresenter!=null){
            mArRulerPresenter.closeSession();
            mArRulerPresenter.onDetach();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0]== PackageManager.PERMISSION_GRANTED){
            init();
        }else{
            initPermission();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 私有方法
    ///////////////////////////////////////////////////////////////////////////
    private void init() {
        SessionHelper.getInstance().getSession(this);

        mShowArRulerSurface = findViewById(R.id.gl_ruler_show);
        mArRulerPresenter = new ArRulerPresenter(this);
    }

    private void initPermission() {
        if (!PermissionHelper.hasCameraPermission(this)) {
            PermissionHelper.requestCameraPermission(this);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 实现接口方法
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void initSession() {
        mArRulerPresenter.initSession(this);
    }

    @MainThread
    @Override
    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSnackBar(String msg) {
        SnackBarHelper.getInstance().showMessage(this, msg);
    }
}
