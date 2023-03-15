package com.gbw.wifi

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import com.gbw.wifi.databinding.ActStartBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class StartActivity : BaseActivity() {

    private var isBack = false
    private var anim: ValueAnimator? = null

    private lateinit var binding: ActStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isBack = intent.getBooleanExtra("isB", false)
        start()
        anim()
    }


    private fun start() {
        launch {
            withTimeoutOrNull(16000) {
                GBWam.instance.builds(GBWap.LOADING, GBWap.HOME_NATIVE, GBWap.RESULT_NATIVE)

            }
            withContext(Dispatchers.Main) {
                anim?.cancel()
                val progress = binding.indexBar.progress
                if (progress < 100) {
                    val animator = ValueAnimator.ofInt(progress, 100)
                    animator.duration = 500L
                    animator.addUpdateListener {
                        val v = it.animatedValue as Int
                        binding.indexBar.progress = v
                        if (v >= 100) {
                            showAd()
                        }
                    }
                    animator.start()
                } else {
                    showAd()
                }
            }
        }
    }


    private fun anim() {
        anim = ValueAnimator.ofInt(0, 90)
        anim?.duration = 9 * 1000L
        anim?.addUpdateListener {
            binding.indexBar.progress = it.animatedValue as Int
        }
        anim?.start()
    }

    private fun showAd() {
        val ad = GBWam.instance.get(GBWap.LOADING)
        if (ad != null && isFront) {
            ad.onClose {
                if (!isBack) {
                    startToMain()
                }
                finish()
            }
            ad.show(this)
        } else {
            if (!isBack) {
                startToMain()
            }
            finish()
        }
    }


    private fun startToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

}