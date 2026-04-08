package com.android.ar_ruler_kt.helper

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController

/**
 * @author：TianLong
 * @date：2022/6/27 21:53
 * @detail：
 */
object Helper : HelperInterface {
    fun obtainScreenSize(context: Context): Point {
        val point = Point()
        if (context is Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display?.getRealSize(point)
            } else {
                @Suppress("DEPRECATION")
                val displayMetrics = DisplayMetrics()
                context.windowManager.defaultDisplay.getRealMetrics(displayMetrics)
                point.x = displayMetrics.widthPixels
                point.y = displayMetrics.heightPixels
            }
            if (point.x <= 0 || point.y <= 0) {
                // Final fallback
                val displayMetrics = context.resources.displayMetrics
                point.x = displayMetrics.widthPixels
                point.y = displayMetrics.heightPixels
            }
        } else {
            val displayMetrics = context.resources.displayMetrics
            point.x = displayMetrics.widthPixels
            point.y = displayMetrics.heightPixels
        }
        return point
    }

    /**
     * Sets the Android fullscreen flags. Expected to be called from [ ][Activity.onWindowFocusChanged].
     *
     * @param activity the Activity on which the full screen mode will be set.
     * @param hasFocus the hasFocus flag passed from the [Activity.onWindowFocusChanged] callback.
     */
    fun setFullScreenOnWindowFocusChanged(activity: Activity, hasFocus: Boolean) {
        if (hasFocus) {
            // 使用新的 WindowInsetsController API 实现全屏沉浸模式
            val window = activity.window
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // New API for Android 11+
                val insetsController = window.insetsController
                insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                insetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                // Legacy API for Android < 11
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            }
        }
    }
}