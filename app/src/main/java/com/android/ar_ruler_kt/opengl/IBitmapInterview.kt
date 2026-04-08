package com.android.ar_ruler_kt.opengl

import android.graphics.*

/**
 * @author：TianLong
 * @date：2022/7/9 0:33
 * @detail：
 */
interface IBitmapInterview {
    val paint: Paint
        get() {
            val tempPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            tempPaint.color = Color.WHITE
            tempPaint.textSize = 250f
            tempPaint.style = Paint.Style.FILL
            return tempPaint
        }

    val paintText: Paint
        get() {
            val tempPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            tempPaint.color = Color.BLACK
            tempPaint.textSize = 50f
            tempPaint.style = Paint.Style.FILL
            return tempPaint
        }
    val paintCircle: Paint
        get() {
            val tempPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            tempPaint.color = Color.BLUE
            tempPaint.textSize = 50f
            tempPaint.style = Paint.Style.FILL
            return tempPaint
        }
    val canvas: Canvas
        get() = Canvas()


    fun drawBitmap(width:Int,height:Int,content:String):Bitmap{
        val bitmap =Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
        // 保证bitmap是可编辑的
//        val bitmap = data.copy(Bitmap.Config.ARGB_8888, true)

        val rectF = RectF(0f, 0f, (width).toFloat(), height.toFloat())

        // 创建Canvas
        val canvas = Canvas(bitmap)

        canvas.drawRoundRect(rectF, 25f, 25f, paint)
        // 获取文字的宽高
        val rect = Rect()
        paintText.getTextBounds(content,0, content.length,rect)
        val textWidth = rect.width()
        val textHeight = rect.height()
        // 绘制文字，view向下为y轴正方向，向左为x轴正方向，（0，0）位置在屏幕左上角
        // 位置设置为0，0时，会将文字的左下角绘制到（0.0）的位置。
        // 所以要将文字中心绘制到想要的位置上，宽需要往左（x軸负方向）便宜textWidth/2，高需要往下（y軸正方向）。
        // 原因：想想文字左下角绘制在屏幕左上角（0，0）时的效果
        canvas.drawText( content,(width.toFloat()-textWidth)/2, (height.toFloat()+textHeight)/2,paintText)

//        canvas.drawCircle( width.toFloat()/2, height.toFloat()/2,60.0f,paintCircle)
        return bitmap
    }

    /**
     * Change bitmap
     * 示例方法，修改bitmap rgb通道，以及 左右翻转，上下翻转
     * @param bitmap
     * @return 返回修改过的bitmap
     */
    fun changeBitmap(bitmap: Bitmap):Bitmap{
        val newbitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val width = newbitmap.width
        val height = newbitmap.height

        val data = IntArray(width * height)
        val newData = IntArray(width * height)
        newbitmap.getPixels(data,0,width,0,0,width,height)
        //int color = (A & 0xff) << 24 | (B & 0xff) << 16 | (G & 0xff) << 8 | (R & 0xff);
        for (i in 0 until height){
            for (j in 0 until width){
                val temp  = data[i * width + j]
                // 一个像素4个通道，每个通道是1个自己
                // int占4个字节。
                val a = temp shr 24 and 0xff
                val b = temp shr 16 and 0xff
                val g = temp shr 8 and 0xff
                val r = temp shr 0 and 0xff

                val color = ((a and 0xff) shl 24) or ((b and 0xff) shl 16) or((g and 0xff) shl 8 )or (r and 0xff)
                newData[(height -i-1) * width + (width-j-1)] = color
            }
        }

        newbitmap.setPixels(newData,0,width,0,0,width,height)
        return newbitmap
    }
}