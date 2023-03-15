package com.gbw.wifi

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import com.gbw.wifi.databinding.DialogTipBinding

class TipDialog(private val context: Activity) : Dialog(context, R.style.dialog_fullscreen) {

    private lateinit var bind: DialogTipBinding

    private var sureListener: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = DialogTipBinding.inflate(LayoutInflater.from(context))
        setContentView(bind.root)
        setCanceledOnTouchOutside(false)
        setCancelable(false)

        bind.close.setOnClickListener { dismiss() }

        bind.btnOk.setOnClickListener { sureListener?.invoke() }

    }

    fun setClickSureListener(l: () -> Unit): TipDialog {
        this.sureListener = l
        return this
    }


//    private fun startConnect() {
//        bind.inputLayout.isVisible = false
//        bind.connectLayout.isVisible = true
//        animator?.cancel()
//        animator = ValueAnimator.ofInt(0, 360)
//        animator?.duration = 1600L
//        animator?.repeatCount = -1
//        animator?.addUpdateListener {
//            bind.ivConnect.rotation = (it.animatedValue as Int).toFloat()
//        }
//        animator?.start()
//    }


    override fun show() {
        if (GBWUtils.isFinish(context)) return
        try {
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            )
            super.show()
            window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun dismiss() {
        if (GBWUtils.isFinish(context)) return
        try {
            super.dismiss()
        } catch (e: Exception) {
        }
    }
}