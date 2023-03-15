package com.gbw.wifi

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.gbw.wifi.databinding.ActivityNetworkBinding
import kotlinx.coroutines.*
import kotlin.random.Random

class NetworkActivity : BaseActivity() {


    private lateinit var binding: ActivityNetworkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNetworkBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.statusBar.setStatusBar()

        loadAd()

        if (GBWUtils.isConnected()) {

            val gTip = if (GBW.speedInfo.ping <= 30) {
                "The game experience is very smooth!"
            } else if (GBW.speedInfo.ping <= 50) {
                "The game can be played normally."
            } else if (GBW.speedInfo.ping <= 100) {
                "The game is slightly delayed."
            } else {
                "High latency, poor game physical examination."
            }
            binding.tvGameTip.text = gTip

            val speed = GBW.speedInfo.rateBit / (1024 * 1024f)

            val type = if (speed > 5) {
                3
            } else if (speed > 2.5) {
                2
            } else if (speed > 1.1) {
                1
            } else {
                0
            }

            when (type) {
                3 -> {
                    binding.ivTemp3.setImageResource(R.mipmap.ic_arrow_green)
                    binding.ivPoint4.setImageResource(R.mipmap.ic_poing_done)
                    binding.tv4k.setTextColor(ContextCompat.getColor(this, R.color.color_video_green))

                }
                2 -> {}
                1 -> {
                    binding.ivPoint2.setImageResource(R.mipmap.ic_poing_done)
                    binding.ivTemp2.setImageResource(R.mipmap.ic_arrow_black)
                    binding.ivPoint3.setImageResource(R.mipmap.ic_point_black)
                    binding.tv1080p.setTextColor(ContextCompat.getColor(this, R.color.color_video_black))
                }
                0 -> {
                    binding.ivPoint1.setImageResource(R.mipmap.ic_poing_done)
                    binding.ivTemp1.setImageResource(R.mipmap.ic_arrow_black)
                    binding.ivPoint2.setImageResource(R.mipmap.ic_point_black)
                    binding.ivTemp2.setImageResource(R.mipmap.ic_arrow_black)
                    binding.ivPoint3.setImageResource(R.mipmap.ic_point_black)
                    binding.tv720p.setTextColor(ContextCompat.getColor(this, R.color.color_video_black))
                    binding.tv1080p.setTextColor(ContextCompat.getColor(this, R.color.color_video_black))
                }
            }


            val typeS = if (speed > 5) {
                "4K"
            } else if (speed > 2.5) {
                "1080P"
            } else if (speed > 1.1) {
                "720P"
            } else {
                "360P"
            }

            binding.tvVideoTip.text = getString(R.string.net_video_tip, typeS)

            binding.tvSpeed1.text = "${GBW.speedInfo.ping + (0..5).random()} ms"
            binding.tvSpeed2.text = "${GBW.speedInfo.ping + (0..5).random()} ms"
            binding.tvSpeed3.text = "${GBW.speedInfo.ping + (0..5).random()} ms"

        } else {
            binding.tvGameTip.text = "Currently no internet connection"
            binding.tvVideoTip.text = "Currently no internet connection"

            binding.ivPoint1.setImageResource(R.mipmap.ic_point_black)
            binding.ivTemp1.setImageResource(R.mipmap.ic_arrow_black)
            binding.ivPoint2.setImageResource(R.mipmap.ic_point_black)
            binding.ivTemp2.setImageResource(R.mipmap.ic_arrow_black)
            binding.ivPoint3.setImageResource(R.mipmap.ic_point_black)
            binding.tv360p.setTextColor(ContextCompat.getColor(this, R.color.color_video_black))
            binding.tv720p.setTextColor(ContextCompat.getColor(this, R.color.color_video_black))
            binding.tv1080p.setTextColor(ContextCompat.getColor(this, R.color.color_video_black))
        }

        binding.ivBack.setOnClickListener { finish() }
    }

    private fun loadAd() {
        launch {
            val ret = GBWam.instance.build(GBWap.RESULT_NATIVE)
            withContext(Dispatchers.Main) {
                showNativeAd(ret)
            }
        }
    }

    private fun showNativeAd(gbwa: GBWa?) {
        binding.adDefault.isVisible = false
        if (gbwa != null && isFront) {
            val adViewBind = binding.adView
            adViewBind.adViewRoot.isVisible = true
            adViewBind.adViewRoot.onGlobalLayout {
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
                        Glide.with(this@NetworkActivity).load(this.icon?.uri)
                            .into(adViewBind.adIcon)
                        adView.setNativeAd(this)
                        adView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

}