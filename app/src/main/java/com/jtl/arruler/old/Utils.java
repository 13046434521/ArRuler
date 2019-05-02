package com.jtl.arruler.old;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class Utils {
    @SuppressLint("ResourceAsColor")
    public static Bitmap getNewBitMap(int fillColor, float round, String text){

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(dip2px(64));
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int bitmapWidth = Math.abs(bounds.right - bounds.left);
        int bitmapHeight = Math.abs(bounds.top-bounds.bottom);
//        Log.e("yyy",bitmapWidth+"---"+bounds.right+"---"+bounds.left+"---"+bounds.top+"---"+bounds.bottom);

        Bitmap output = Bitmap.createBitmap(bitmapWidth+100, bitmapHeight+50, Bitmap.Config.ARGB_8888);
        output.eraseColor(Color.argb(255,255,255,255));
        Canvas canvas = new Canvas(output);

        RectF outerRect = new RectF(0, 0, bitmapWidth+100, bitmapHeight+50);
        paint.setColor(fillColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(outerRect, round, round, paint);
//        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.BLACK);
        canvas.drawText(text,50,bitmapHeight+25,paint);

        //output.eraseColor(Color.argb(255,255,0,255));

        return output;
    }

    //dp转px
    public static float dip2px(float dp) {
        return  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                Resources.getSystem().getDisplayMetrics());
    }

    public static int[] getScreenSize(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (context instanceof Activity) {
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        } else {
            displayMetrics = context.getResources().getDisplayMetrics();
        }
        return new int[]{displayMetrics.widthPixels, displayMetrics.heightPixels};
    }
}
