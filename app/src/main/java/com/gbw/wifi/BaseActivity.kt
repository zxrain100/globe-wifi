package com.gbw.wifi

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

@Suppress("DEPRECATION")
open class BaseActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    //是否在前台
    protected var isFront = false

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window?.decorView?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window?.statusBarColor = Color.TRANSPARENT

        //设置状态栏模式
        val wc = WindowCompat.getInsetsController(window, window.decorView)
        wc.isAppearanceLightStatusBars = false

        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        isFront = true
    }

    override fun onPause() {
        super.onPause()
        isFront = false
    }

    internal fun View.setStatusBar(): Int {
        val sH = GBWUtils.getStatusBarH(this@BaseActivity)
        val newPaddingTop = paddingTop + sH
        setPadding(paddingLeft, newPaddingTop, paddingRight, bottom)
        return layoutParams.apply {
            height += sH
        }.apply {
            layoutParams = this
        }.run { height }
    }


    inline fun View.onGlobalLayout(crossinline callback: () -> Unit) {
        this.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                callback()
            }
        })
    }
}