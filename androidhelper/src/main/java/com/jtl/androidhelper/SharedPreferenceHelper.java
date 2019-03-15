package com.jtl.androidhelper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 作者:jtl
 * 日期:Created in 2018/7/10 10:34
 * 描述:SP
 * 更改:
 */

public class SharedPreferenceHelper {
    private static String sTag;
    private static final String TAG="IMI";
    private static SharedPreferences mSharedPreferences;
    private static SharedPreferences.Editor mEditor;
    public static SharedPreferenceHelper getInstance(Context context, String tag){
        sTag=tag;
        mSharedPreferences=context.getSharedPreferences(TAG,Context.MODE_PRIVATE);
        mEditor=mSharedPreferences.edit();
        return SharedPreferenceUtilsHolder.SHARED_PREFRENCE_UTILS;
    }

    private SharedPreferenceHelper(){}

    public void putBoolean (boolean isboolean){
        mEditor.putBoolean(sTag,isboolean);
        mEditor.commit();
    }

    public boolean getBoolean(){
        return mSharedPreferences.getBoolean(sTag,false);
    }

    public void putFloat(float content){
        mEditor.putFloat(sTag,content);
        mEditor.commit();
    }

    public float getFloat(){
        return mSharedPreferences.getFloat(sTag,0f);
    }

    public void putString(String content){
        mEditor.putString(sTag,content);
        mEditor.commit();
    }

    public String getString(){
        return mSharedPreferences.getString(sTag,"");
    }


    private static class SharedPreferenceUtilsHolder {
        private static final SharedPreferenceHelper SHARED_PREFRENCE_UTILS=new SharedPreferenceHelper();
    }
}
