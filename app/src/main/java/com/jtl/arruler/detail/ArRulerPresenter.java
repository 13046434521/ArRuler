package com.jtl.arruler.detail;

import android.app.Activity;
import android.content.Context;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.jtl.arruler.base.BasePresenter;
import com.jtl.arruler.helper.SessionHelper;
import com.socks.library.KLog;

/**
 * 作者:jtl
 * 日期:Created in 2019/3/14 15:29
 * 描述:
 * 更改:
 */
public class ArRulerPresenter extends BasePresenter<ArRulerContract.View> implements ArRulerContract.Presenter<ArRulerContract.View> {
    private boolean isARCoreInstall;
    private Session mSession;

    public ArRulerPresenter(ArRulerContract.View view) {
        super(view);
    }

    @Override
    public void start() {

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
}
