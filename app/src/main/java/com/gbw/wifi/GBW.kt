package com.gbw.wifi

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Process
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.Utils
import com.google.firebase.FirebaseApp

//wifi2023324
class GBW : Application() {

    companion object {
        internal var instance: GBW? = null
        private val baseInstance: GBW by lazy { instance!! }
        val context: Context by lazy { baseInstance.applicationContext }

        var speedInfo: SpeedInfo = SpeedInfo(-1, 0.0)
    }

    override fun attachBaseContext(base: Context?) {
        instance = this
        super.attachBaseContext(base)
    }


    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
        }

        if (getProcess(this) != packageName) return

        //初始化广告
        GBWam.instance.initialize(this)
        AppUtils.registerAppStatusChangedListener(object : Utils.OnAppStatusChangedListener {
            override fun onForeground(activity: Activity?) {
                if (activity !is StartActivity) {
                    val intent = Intent(activity, StartActivity::class.java)
                    intent.putExtra("isB", true)
                    activity?.startActivity(intent)
                }
            }

            override fun onBackground(activity: Activity?) {
            }

        })
        RCHelper.instance.initFetchAndActivate()
    }


    private fun getProcess(cxt: Context): String? {
        val pid = Process.myPid()
        val am = cxt.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val runningApps = am.runningAppProcesses ?: return null
        for (app in runningApps) {
            if (app.pid == pid) {
                return app.processName
            }
        }
        return null
    }

}