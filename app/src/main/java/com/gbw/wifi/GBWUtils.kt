package com.gbw.wifi

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.TypedValue
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*
import kotlin.experimental.and

object GBWUtils {

    fun px2dip(px: Float): Float = px / GBW.context.resources.displayMetrics.density + 0.5f

    fun dip2px(dp: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            GBW.context.resources.displayMetrics
        ).toInt()


    @SuppressLint("DiscouragedApi", "InternalInsetResource")
    fun getStatusBarH(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = runCatching { context.resources.getDimensionPixelSize(resourceId) }.getOrDefault(0)
        }

        if (result == 0) {
            result = GBWUtils.dip2px(24)
        }

        return result
    }


    /**
     * 当前网络是否已经链接
     */
    fun isConnected(): Boolean {
        val cm = GBW.context.getSystemService(ConnectivityManager::class.java) ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val nc = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
                if (!nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    return false
                }

                return !isOnlyVpnTransport(nc)
            } catch (e: SecurityException) {
                // android 11 bug: https://issuetracker.google.com/issues/175055271
            }
        }

        @Suppress("DEPRECATION")
        return cm.activeNetworkInfo?.isConnected == true
    }


    private fun isOnlyVpnTransport(nc: NetworkCapabilities): Boolean {
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return false
        }
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE)) {
            return false
        }
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)) {
            return false
        }
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            return false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && nc.hasTransport(
                NetworkCapabilities.TRANSPORT_LOWPAN
            )
        ) {
            return false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && nc.hasTransport(NetworkCapabilities.TRANSPORT_USB)) {
            return false
        }
        return nc.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    }


    /**
     * 当前连接的网络是否是WIFI
     */
    @Suppress("DEPRECATION")
    fun isWifi(): Boolean {
        val cm = GBW.context.getSystemService(ConnectivityManager::class.java) ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val nc = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }


    fun getLocalIpAddress(): String? {
        try {
            val en: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf: NetworkInterface = en.nextElement()
                val enumIpAddr: Enumeration<InetAddress> = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress: InetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        return inetAddress.hostAddress?.toString()
                    }
                }
            }
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
        return null
    }


    //根据IP获取本地Mac
    fun getLocalMacAddress(): String? {
        var mac: String? = ""
        try {
            val ip = getLocalIpAddress()
            val ipAddress = InetAddress.getByName(ip)
            val hard: ByteArray = NetworkInterface.getByInetAddress(ipAddress).hardwareAddress
            mac = byte2hex(hard)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return mac
    }

    private fun byte2hex(b: ByteArray): String {
        var hs = StringBuffer(b.size)
        var stmp: String
        val len = b.size
        for (n in 0 until len) {
            stmp = Integer.toHexString((b[n] and 0xFF.toByte()).toInt())
            hs = if (stmp.length == 1) hs.append("0").append(stmp) else {
                hs.append(stmp)
            }
        }
        return hs.toString()
    }

    fun isFinish(context: Activity): Boolean {
        return context.isFinishing || context.isDestroyed
    }


    fun checkGBS(activity: Activity): Boolean {
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val agps = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return gps || agps
    }
}