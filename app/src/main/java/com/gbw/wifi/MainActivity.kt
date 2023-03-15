package com.gbw.wifi

import android.Manifest
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gbw.wifi.databinding.ActivityMainBinding
import com.thanosfisherman.wifiutils.WifiUtils
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionErrorCode
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : BaseActivity(), View.OnClickListener {

    private var adapter: WiFiListAdapter? = null
    private lateinit var connectDialog: ConnectDialog
    private lateinit var tipDialog: TipDialog

    private lateinit var binding: ActivityMainBinding
    private var actLauncher: ActivityResultLauncher<Intent>? = null

    private var anim: ValueAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.statusBar.setStatusBar()

        binding.mWiFiRecycler.layoutManager = LinearLayoutManager(this)
        adapter = WiFiListAdapter()

        adapter?.setOnItemClickListener { it ->
            connectDialog = ConnectDialog(this, it)
            connectDialog.setClickSureListener { pwd ->
                connectDialog.dismiss()
                connect(it, pwd)
            }
            connectDialog.show()
            launch {
                val ret = GBWam.instance.build(GBWap.RESULT_NATIVE)
                withContext(Dispatchers.Main) {
                    connectDialog.setAd(ret)
                }
            }
        }
        binding.mWiFiRecycler.adapter = adapter

        binding.itemSecurity.setOnClickListener(this)
        binding.itemNetTest.setOnClickListener(this)
        tipDialog = TipDialog(this)

        actLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                checkPermission()
            }
        }

        checkPermission()

    }

    override fun onResume() {
        super.onResume()
        loadAd()
    }

    private fun checkPermission() {
        //android10及以上检查权限的同时需要检测gps开关
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !GBWUtils.checkGBS(this)) {
            tipDialog.setClickSureListener {
                actLauncher?.launch(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }.show()
        } else {
            tipDialog.dismiss()
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 555)
            } else {
                scanWifi()
            }
        }
    }

    private fun scanWifi() {
        val list = ScanDataHelper.getData()
        if (list.isNotEmpty()) {
            scanSuccess(list)
        } else {
            binding.noResultTip.isVisible = true
            binding.noContent.isVisible = false
            binding.wifiLayout.isVisible = false
            binding.topConnection.isVisible = false
            startAnim()
            WifiUtils.withContext(applicationContext).scanWifi(this::getScanResults).start()
        }
    }

    private fun startAnim() {
        binding.imgLoading.isVisible = true
        anim = ValueAnimator.ofFloat(0f, 360f)
        anim?.duration = 1500L
        anim?.repeatCount = -1
        anim?.addUpdateListener {
            binding.imgLoading.rotation = it.animatedValue as Float
        }
        anim?.start()
    }

    private fun getScanResults(results: List<ScanResult>) {
        val list = results.filter { it.SSID.isNotEmpty() }

        if(list.isNotEmpty()){
            ScanDataHelper.setData(list)
        }
        scanSuccess(list)
    }

    private fun loadAd() {
        launch {
            val ret = GBWam.instance.build(GBWap.HOME_NATIVE)
            withContext(Dispatchers.Main) {
                showNativeAd(ret)
            }
            GBWam.instance.build(GBWap.RESULT_NATIVE)
        }
    }

    private fun connect(data: ScanResult, password: String) {
        binding.noResultTip.isVisible = true
        binding.noContent.isVisible = false
        binding.wifiLayout.isVisible = false
        binding.topConnection.isVisible = false
        startAnim()

        WifiUtils.withContext(applicationContext)
            .connectWith(data.SSID, data.BSSID, password)
            .setTimeout(30000)
            .onConnectionResult(object : ConnectionSuccessListener {
                override fun success() {
                    onWifiConnectComplete(true)
                }

                override fun failed(errorCode: ConnectionErrorCode) {
                    onWifiConnectComplete(false)
                }
            })
            .start()

    }


    private fun scanSuccess(results: List<ScanResult>) {
        anim?.cancel()
        if (results.isEmpty()) {
            binding.noResultTip.isVisible = true
            binding.noContent.isVisible = true
            binding.imgLoading.isVisible = false
            binding.wifiLayout.isVisible = false
            binding.topConnection.isVisible = false
        } else {
            val wifiManager = getSystemService(WifiManager::class.java)
            binding.noResultTip.isVisible = false
            binding.wifiLayout.isVisible = true
            if (WifiUtils.withContext(this).isWifiConnected) {
                val ssid = wifiManager?.connectionInfo?.ssid?.replace("\"", "")

                val connectItem = results.find { it.SSID == ssid }
                if (connectItem != null) {
                    binding.topConnection.isVisible = true
                    binding.connectLayout.isVisible = true
                    binding.itemLabel.text = ssid
                    binding.topTitle.text = ssid
                } else {
                    binding.connectLayout.isVisible = false
                    binding.topConnection.isVisible = false
                }

                val list = results.filter { (it.SSID != ssid) && it.SSID.isNotEmpty() }
                adapter?.submitList(list)
            } else {
                binding.connectLayout.isVisible = false
                adapter?.submitList(results)
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode ==555){
            checkPermission()
        }
    }


    private fun onWifiConnectComplete(isSuccess: Boolean) {
        //toast("onWifiConnectComplete isSuccess =$isSuccess")
        connectDialog.dismiss()

        launch {
            delay(2000)
            withContext(Dispatchers.Main) {
                scanWifi()
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding.itemSecurity -> {
                if (!GBWUtils.isConnected()) {
                    Toast.makeText(this, "Please connect to the Internet first", Toast.LENGTH_SHORT).show()
                    return
                }
                val intent = Intent(this, AminActivity::class.java)
                intent.putExtra("type", 0)
                startActivity(intent)
            }
            binding.itemNetTest -> {
                if (!GBWUtils.isConnected()) {
                    Toast.makeText(this, "Please connect to the Internet first", Toast.LENGTH_SHORT).show()
                    return
                }
                val intent = Intent(this, AminActivity::class.java)
                intent.putExtra("type", 1)
                startActivity(intent)
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
                        Glide.with(this@MainActivity).load(this.icon?.uri)
                            .into(adViewBind.adIcon)
                        adView.setNativeAd(this)
                        adView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}