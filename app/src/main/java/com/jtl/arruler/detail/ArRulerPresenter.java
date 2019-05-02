package com.jtl.arruler.detail;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.MotionEvent;

import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.jtl.androidhelper.ScreenHelper;
import com.jtl.arruler.base.BasePresenter;
import com.jtl.arruler.helper.SessionHelper;
import com.jtl.arruler.helper.TapHelper;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者:jtl
 * 日期:Created in 2019/3/14 15:29
 * 描述:
 * 更改:
 */
public class ArRulerPresenter extends BasePresenter<ArRulerActivity> implements ArRulerContract.Presenter<ArRulerContract.View> {
    private static final String TAG = ArRulerPresenter.class.getSimpleName();
    private boolean isARCoreInstall;
    private Session mSession;
    private TapHelper mTapHelper;
    private Point mPoint;
    private MotionEvent mMotionEvent;
    private List<Anchor> mPointList;

    public ArRulerPresenter(ArRulerActivity t1) {
        super(t1);
    }


    @Override
    public void start() {
        mPoint = ScreenHelper.getInstance().getScreenPoint(getView());
        mTapHelper = new TapHelper();
        mPointList = new ArrayList<>(16);
        mMotionEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, mPoint.x / 2f, mPoint.y / 2f, 0);
    }

    @Override
    public boolean initSession(Context context) {
        Exception exception = new Exception();
        String message = "";
        if (mSession == null) {
            try {
                switch (ArCoreApk.getInstance().requestInstall((Activity) getView(), !isARCoreInstall)) {
                    case INSTALLED:
                        getView().showToast("已安装");
                        break;
                    case INSTALL_REQUESTED:
                        isARCoreInstall = true;
                        break;
                }

                mSession = SessionHelper.getInstance().getSession(context);
            } catch (UnavailableDeviceNotCompatibleException e) {
                exception = e;
                message = "This device does not support AR";
            } catch (UnavailableUserDeclinedInstallationException e) {
                exception = e;
                message = "Please install ARCore";
            }
            KLog.e(exception + ":" + message);
            getView().showSnackBar(message);
        }
        return mSession != null;//mSession 初始化成功时不为NULL
    }

    @Override
    public void resumeSession() {
        SessionHelper.getInstance().resumeSession();

    }

    @Override
    public void pauseSession() {
        SessionHelper.getInstance().pauseSession();
    }

    @Override
    public void closeSession() {
        SessionHelper.getInstance().closeSession();
    }

    @Override
    public void addRuler() {
        mTapHelper.push(mMotionEvent);
    }

    @Override
    public void deleteRuler() {
        synchronized (mPointList){
            int count = mPointList.size() % 2;
            if (mPointList.size() == 0) {
                KLog.e(TAG, "无数据");
            } else if (count == 1) {
                mPointList.remove(mPointList.size() - 1);
            } else {
                mPointList.remove(mPointList.size() - 1);
                mPointList.remove(mPointList.size() - 1);
            }
        }
    }

    public List<Anchor> getPointList(){
        return mPointList;
    }

    public Point getPoint(){
        return mPoint;
    }

    public MotionEvent getMotionEvent() {
        return mMotionEvent;
    }

    public TapHelper getTapHelper() {
        return mTapHelper;
    }
}
