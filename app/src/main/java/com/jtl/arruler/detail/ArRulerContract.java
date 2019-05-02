package com.jtl.arruler.detail;

import android.content.Context;
import android.view.MotionEvent;

import com.jtl.arruler.base.BasePresenterImpl;
import com.jtl.arruler.base.BaseViewImpl;

/**
 * 作者:jtl
 * 日期:Created in 2019/3/14 15:28
 * 描述:
 * 更改:
 */
public class ArRulerContract {
    interface View extends BaseViewImpl{
        void showToast(String msg);
        void showSnackBar(String msg);
    }

    interface Presenter <BaseViewImpl> extends BasePresenterImpl{
        boolean initSession(Context context);
        void resumeSession();
        void pauseSession();
        void closeSession();

        void addRuler();
        void deleteRuler();
    }
}
