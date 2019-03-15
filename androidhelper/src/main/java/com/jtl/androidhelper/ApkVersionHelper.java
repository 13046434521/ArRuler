package com.jtl.androidhelper;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * 作者:jtl
 * 日期:Created in 2018/12/13 21:22
 * 描述:
 * 更改:
 */

public class ApkVersionHelper {
    /**
     * 获取当前本地apk的版本
     * 获取软件版本号，对应AndroidManifest.xml下android:versionCode
     *
     * @param mContext
     * @return
     */
    public static int getVersionCode(Context mContext) {
        int versionCode = 0;
        try {
            versionCode = mContext.getPackageManager().
                    getPackageInfo(mContext.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取版本号名称
     * 获取软件版本号，对应AndroidManifest.xml下android:versionName
     *
     * @param context 上下文
     * @return
     */
    public static String getVerName(Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }
}
