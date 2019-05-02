package com.jtl.arruler.old;

import android.support.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.jtl.arruler.old.Config.DrawState.DOING;
import static com.jtl.arruler.old.Config.DrawState.END;

public class Config {
    //橙色
    public static final float COLOR_ORANGE_R = 255f;//255
    public static final float COLOR_ORANGE_G = 165f;//165
    public static final float COLOR_ORANGE_B = 0.0f;//0

    //白色
    public static final float COLOR_WHITE_R = 1.0f;
    public static final float COLOR_WHITE_G = 1.0f;
    public static final float COLOR_WHITE_B = 1.0f;

    /**
     * 尺子线段，开始，结束
     */
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @IntDef({DOING, END})
    public @interface DrawState {
        int DOING = 0;
        int END = 1;
    }
}
