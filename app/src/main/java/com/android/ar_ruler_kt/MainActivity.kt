package com.android.ar_ruler_kt

import android.graphics.Point
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.ar_ruler_kt.helper.FullScreenHelper
import com.android.ar_ruler_kt.helper.Helper
import com.android.ar_ruler_kt.helper.SessionHelper
import com.android.ar_ruler_kt.opengl.BackgroundSurface
import com.android.ar_ruler_kt.opengl.IBitmapInterview
import com.google.ar.core.Session

class MainActivity : AppCompatActivity(),View.OnClickListener,IViewInterface,IBitmapInterview{
    val TAG = this.javaClass.simpleName
    var session:Session? = null
    val backgroundSurface :BackgroundSurface by lazy{findViewById(R.id.gl_main_background)}
    val contentText:TextView by lazy { findViewById(R.id.tv_main_content) }
    val addImage:ImageView by lazy { findViewById(R.id.iv_main_add) }
    val deleteImage:ImageView by lazy { findViewById(R.id.iv_main_delete) }
    val promptImage:ImageView by lazy { findViewById(R.id.iv_main_prompt) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!SessionHelper.initialize(this)){
            Toast.makeText(this,"ARCore初始化失败",Toast.LENGTH_SHORT).show()
            return
        }

        addImage.setOnClickListener(this)
        deleteImage.setOnClickListener(this)

        session = SessionHelper.session
        backgroundSurface.session = session
        backgroundSurface.iViewInterface = this
    }

    override fun onResume() {
        super.onResume()
        backgroundSurface.onResume()
        session?.resume()
    }

    override fun onPause() {
        super.onPause()
        backgroundSurface.onPause()
        session?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        session?.close()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Helper.setFullScreenOnWindowFocusChanged(this,true)
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.iv_main_add->add()
            R.id.iv_main_delete->delete()
            else->{

            }
        }
    }

    private fun add(){
        backgroundSurface.add()
    }
    private fun delete(){
        backgroundSurface.delete()
    }

    override fun detectSuccess(msg:String) {
        (promptImage.visibility == View.VISIBLE).run {
            runOnUiThread {
                promptImage.visibility = View.GONE
                contentText.text = msg
            }
        }
    }

    override fun detectFailed(msg:String) {
        (promptImage.visibility == View.GONE).run {
            runOnUiThread {
                promptImage.visibility = View.VISIBLE
                contentText.text = msg
            }
        }
    }
}