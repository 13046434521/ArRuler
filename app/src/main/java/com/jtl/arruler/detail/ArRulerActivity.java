package com.jtl.arruler.detail;

import android.content.pm.PackageManager;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.constraint.Group;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jtl.androidhelper.PermissionHelper;
import com.jtl.androidhelper.SnackBarHelper;
import com.jtl.arruler.ArRulerSurface;
import com.jtl.arruler.R;
import com.jtl.arruler.callback.ArRulerCallBack;


public class ArRulerActivity extends AppCompatActivity implements ArRulerContract.View, View.OnClickListener, ArRulerCallBack {
    private ArRulerPresenter mArRulerPresenter;
    private ArRulerSurface mShowArRulerSurface;
    private ImageView mAddImage;
    private ImageView mDeleteImage;
    private Group mPromptGroup;
    private TextView mPromptText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_ruler);
        initPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mArRulerPresenter != null) {
            mArRulerPresenter.resumeSession();
        }
        if (mShowArRulerSurface != null) {
            mShowArRulerSurface.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //glSurfaceView  先暂停，否则Session更新时会报SessionPausedException
        if (mShowArRulerSurface != null) {
            mShowArRulerSurface.onPause();
        }

        if (mArRulerPresenter != null) {
            mArRulerPresenter.pauseSession();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mArRulerPresenter != null) {
            mArRulerPresenter.closeSession();
            mArRulerPresenter.onDetach();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            init();
        } else {
            initPermission();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 私有方法
    ///////////////////////////////////////////////////////////////////////////
    private void init() {
        mArRulerPresenter = new ArRulerPresenter(this);

        mShowArRulerSurface = findViewById(R.id.gl_ruler_show);
        mAddImage = findViewById(R.id.iv_ruler_add);
        mDeleteImage = findViewById(R.id.iv_ruler_delete);
        mPromptText=findViewById(R.id.tv_ruler_prompt);
        mPromptGroup=findViewById(R.id.group_ruler_prompt);

        mAddImage.setOnClickListener(this);
        mDeleteImage.setOnClickListener(this);

        mShowArRulerSurface.setPoint(mArRulerPresenter.getPoint());
        mShowArRulerSurface.setTapHelper(mArRulerPresenter.getTapHelper());
        mShowArRulerSurface.setAnchorList(mArRulerPresenter.getPointList());
        mShowArRulerSurface.setMotionEvent(mArRulerPresenter.getMotionEvent());
        mShowArRulerSurface.setArRulerCallBack(this);
    }

    private void initPermission() {
        if (!PermissionHelper.hasCameraPermission(this)) {
            PermissionHelper.requestCameraPermission(this);
        } else {
            init();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 实现接口方法
    ///////////////////////////////////////////////////////////////////////////

    @MainThread
    @Override
    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSnackBar(String msg) {
        SnackBarHelper.getInstance().showMessage(this, msg);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_ruler_add:
                if (mShowArRulerSurface.isHitTest()){
                    mArRulerPresenter.addRuler();
                }
                break;
            case R.id.iv_ruler_delete:
                mArRulerPresenter.deleteRuler();
                break;
        }
    }

    @Override
    public void showPrompt(final boolean isShow) {
        showPrompt(isShow,"");
    }

    @Override
    public void showPrompt(final boolean isShow,final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPromptGroup.setVisibility(isShow?View.VISIBLE:View.GONE);
                mPromptText.setText(msg);
            }
        });
    }
}
