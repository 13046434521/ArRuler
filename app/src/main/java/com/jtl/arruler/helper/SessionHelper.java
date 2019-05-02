package com.jtl.arruler.helper;

import android.content.Context;
import android.util.Log;

import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.socks.library.KLog;

/**
 * 作者:jtl
 * 日期:Created in 2019/5/2 17:01
 * 描述:Session单例类
 * 更改:
 */
public class SessionHelper {
    private static String TAG = SessionHelper.class.getSimpleName();
    private Session mSession;

    private SessionHelper() {
    }

    public static SessionHelper getInstance() {
        return SessionHelperHolder.sSessionHelper;
    }

    public void initSession(Context context) {
        Exception exception = new Exception();
        String message = "";
        try {
            mSession = new Session(context);
            mSession.resume();
        } catch (UnavailableDeviceNotCompatibleException e) {
            exception = e;
            message = "This device does not support AR";
        } catch (UnavailableArcoreNotInstalledException e) {
            exception = e;
            message = "Please install ARCore";
        } catch (UnavailableSdkTooOldException e) {
            exception = e;
            message = "Please update ARCore";
        } catch (UnavailableApkTooOldException e) {
            exception = e;
            message = "Please update this app";
        } catch (CameraNotAvailableException e) {
            exception = e;
            message = "Session resume failed";
        }catch (Exception e){
            exception = e;
            message = e.toString();
        }
        KLog.e(exception + ":" + message);
    }

    public Session getSession(Context context) {
        if (mSession == null) {
            initSession(context);
        }

        return mSession;
    }

    public void resumeSession() {
        try {
            if (mSession != null) {
                mSession.resume();
            }
        } catch (CameraNotAvailableException e) {
            Log.e(TAG, "Camera not available. Please restart the app.");
            mSession = null;
        }
    }

    public void pauseSession() {
        if (mSession != null) {
            mSession.pause();
        }
    }

    public void closeSession(){
        if (mSession != null) {
            mSession.close();
        }
    }

    private static class SessionHelperHolder {
        private final static SessionHelper sSessionHelper = new SessionHelper();
    }
}
