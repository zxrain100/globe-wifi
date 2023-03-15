package com.gbw.wifi

import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.net.wifi.ScanResult
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.gbw.wifi.databinding.DialogConnectBinding

class ConnectDialog(private val context: Activity, private val scaResult: ScanResult) : Dialog(context, R.style.dialog_fullscreen) {

    private lateinit var bind: DialogConnectBinding

    private var sureListener: ((String) -> Unit)? = null
    private var mIsVisibilityPassword = false

    private var animator: ValueAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = DialogConnectBinding.inflate(LayoutInflater.from(context))
        setContentView(bind.root)
        setCanceledOnTouchOutside(false)
        setCancelable(false)

        bind.tvName.text = scaResult.SSID
        bind.tvCancel.setOnClickListener { dismiss() }

        bind.isVisible.setOnClickListener {
            if (mIsVisibilityPassword) {
                bind.isVisible.setImageResource(R.mipmap.ic_visible)
                bind.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            } else {
                bind.isVisible.setImageResource(R.mipmap.ic_unvisible)
                bind.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }

            mIsVisibilityPassword = !mIsVisibilityPassword
        }

        bind.tvConnect.setOnClickListener {
            //startConnect()
            val s = bind.etPassword.text.toString()
            sureListener?.invoke(s)
        }

    }

    fun setClickSureListener(pwd: (String) -> Unit): ConnectDialog {
        this.sureListener = pwd
        return this
    }


    fun setAd(gbwa: GBWa?) {
        if(GBWUtils.isFinish(context)) return
        bind.adDef.isVisible = false
        if (gbwa != null) {
            val adViewBind = bind.adView
            adViewBind.adViewRoot.isVisible = true
            adViewBind.adViewRoot.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    adViewBind.adViewRoot.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val adView = adViewBind.adView
                    gbwa.showNav {
                        if (this == null) {
                            adView.visibility = View.GONE
                        } else {
                            adViewBind.adAction.text = this.callToAction
                            adViewBind.adTitle.text = this.headline
                            adViewBind.adDescription.text = this.body
                            adView.adChoicesView = adViewBind.adChoices
                            adView.callToActionView = adViewBind.adAction
                            adView.imageView = adViewBind.adImage
                            adView.mediaView = adViewBind.adMedia
                            adView.iconView = adViewBind.adIcon
                            adView.headlineView = adViewBind.adTitle
                            adView.bodyView = adViewBind.adDescription
                            Glide.with(context).load(this.icon?.uri).into(adViewBind.adIcon)
                            adView.setNativeAd(this)
                            adView.visibility = View.VISIBLE
                        }
                    }
                }
            })

        }
    }


    override fun show() {
        animator?.cancel()
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