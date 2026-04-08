package com.android.ar_ruler_kt.helper

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.util.DisplayMetrics
import android.view.View

/**
 * @author：TianLong
 * @date：2022/6/27 21:53
 * @detail：
 */
object Helper :HelperInterface {
    fun obtainScreenSize(context: Context): Point {
        var displayMetrics = DisplayMetrics()
        if (context is Activity) {
            context.windowManager.defaultDisplay.getMetrics(displayMetrics)
        } else {
            displayMetrics = context.resources.displayMetrics
        }
        return Point(displayMetrics.widthPixels,displayMetrics.heightPixels)
    }

    /**
     * Sets the Android fullscreen flags. Expected to be called from [ ][Activity.onWindowFocusChanged].
     *
     * @param activity the Activity on which the full screen mode will be set.
     * @param hasFocus the hasFocus flag passed from the [Activity.onWindowFocusChanged] callback.
     */
    fun setFullScreenOnWindowFocusChanged(activity: Activity, hasFocus: Boolean) {
        if (hasFocus) {
            // https://developer.android.com/training/system-ui/immersive.html#sticky
            activity
                .window
                .decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }
}