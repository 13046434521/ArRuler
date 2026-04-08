package com.android.ar_ruler_kt

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.android.ar_ruler_kt", appContext.packageName)
    }

    @Test
    fun youyisi(){
        val a = 4
        val b = a.toDouble().pow(2)

        val c = 16.0
        val d = sqrt(c)
        Log.w("测试",":${b}   $d")
    }
}