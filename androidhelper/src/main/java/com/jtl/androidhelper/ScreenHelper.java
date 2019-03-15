package com.jtl.androidhelper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.View;
import android.view.WindowManager;

import java.lang.ref.WeakReference;

/**
 * 作者:jtl
 * 日期:Created in 2018/11/16 23:12
 * 描述:
 * 更改:
 */

public class ScreenHelper {
    public static ScreenHelper getInstance() {
        return ScreenHelperHolder.sScreenHelper;
    }

    private static class ScreenHelperHolder {
        private static ScreenHelper sScreenHelper = new ScreenHelper();
    }


    public void setFullScreenOnWindowFocusChanged(Activity activity, boolean hasFocus) {
        if (hasFocus) {
            activity
                    .getWindow()
                    .getDecorView()
                    .setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    /**
     * dp转换成px
     */
    public int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * px转换成dp
     */
    public int px2dp(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public Point getScreenPoint(Activity activity) {
        WeakReference<Activity> weakReference = new WeakReference<Activity>(activity);
        Point mScreenPoint = new Point();
        weakReference.get().getWindowManager().getDefaultDisplay().getSize(mScreenPoint);
        return mScreenPoint;
    }
}
