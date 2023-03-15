package com.gbw.wifi

import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.gbw.wifi.databinding.ActivitySecurityBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat

class SecurityActivity : BaseActivity() {

    private lateinit var binding: ActivitySecurityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecurityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.statusBar.setStatusBar()

        loadAd()

        if (GBWUtils.isConnected()) {

            binding.imgNetStatus.setImageResource(R.mipmap.ic_security_net)
            if (GBWUtils.isWifi()) {
                val wifiManager = getSystemService(WifiManager::class.java)
                val ssid = wifiManager?.connectionInfo?.ssid?.replace("\"", "")
                binding.tvWifiName.text = ssid
            } else {
                binding.tvWifiName.text = "Cellular network"
            }

            if (GBW.speedInfo.rateBit > 0) {
                binding.tvSpeed.text = getSpeed(GBW.speedInfo.rateBit)
                binding.tvSpeed.setTextColor(ContextCompat.getColor(this, R.color.color_speed))
                binding.tvRateByte.text = getRateByte(GBW.speedInfo.rateBit)
            }
            binding.tvIP.text = GBWUtils.getLocalIpAddress()
            val mac = GBWUtils.getLocalMacAddress()
            if (mac.isNullOrEmpty()) {
                binding.macLayout.isVisible = false
            } else {
                binding.tvMac.text = mac
                binding.macLayout.isVisible = true
            }

        } else {
            binding.imgNetStatus.setImageResource(R.mipmap.ic_security_no_net)
            binding.tvSpeed.text = "0KB/s"
            binding.tvSpeed.setTextColor(ContextCompat.getColor(this, R.color.white))
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
                        Glide.with(this@SecurityActivity).load(this.icon?.uri)
                            .into(adViewBind.adIcon)
                        adView.setNativeAd(this)
                        adView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private var mFormat: DecimalFormat = DecimalFormat("0.00")
    private fun getSpeed(speed: Double): String {
        return if (speed > 1024 * 1024f) {
            mFormat.format(speed / (8 * 1024 * 1024f)) + "MB/s"
        } else if (speed > 1024f) {
            mFormat.format(speed / 8 * 1024f) + "KB/s"
        } else {
            mFormat.format(speed / 8f) + "B/s"
        }
    }


    private fun getRateByte(speed: Double): String {
        return if (speed > 1024 * 1024f) {
            mFormat.format(speed / (1024 * 1024f)) + "Mbps"
        } else if (speed > 1024f) {
            mFormat.format(speed / 1024f) + "Kbps"
        } else {
            mFormat.format(speed) + "bps"
        }
    }

}