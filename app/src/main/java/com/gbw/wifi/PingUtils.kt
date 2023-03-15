package com.gbw.wifi

import com.blankj.utilcode.util.RegexUtils.isMatch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URI
import kotlin.math.roundToInt


object PingUtils {

    //ip地址正则表达式
    private const val ipRegex =
        "((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))"

    //最小RTT
    const val TYPE_MIN = 1

    //平均RTT
    const val TYPE_AVG = 2

    //最大RTT
    const val TYPE_MAX = 3

    //RTT偏差
    const val TYPE_MDEV = 4


    /**
     * 获取ping url的RTT
     *
     * @param url     需要ping的url地址
     * @param type    ping的类型
     * @param count   需要ping的次数
     * @param timeout 需要ping的超时时间，单位 ms
     * @return RTT值，单位 ms 注意：-1是默认值，返回-1表示获取失败
     */
    fun getRTT(url: String, type: Int, count: Int, timeout: Int): Int {
        val domain: String = getDomain(url) ?: return -1
        val pingString: String? = ping(createSimplePingCommand(count, timeout, domain))
        if (null != pingString) {
            try {
                //获取以"min/avg/max/mdev"为头的文本，分别获取此次的ping参数
                val tempInfo = pingString.substring(pingString.indexOf("min/avg/max/mdev") + 19)
                val temps = tempInfo.split("/").toTypedArray()
                return when (type) {
                    TYPE_MIN -> {
                        temps[0].toFloat().roundToInt()
                    }
                    TYPE_AVG -> {
                        temps[1].toFloat().roundToInt()
                    }
                    TYPE_MAX -> {
                        temps[2].toFloat().roundToInt()
                    }
                    TYPE_MDEV -> {
                        temps[3].toFloat().roundToInt()
                    }
                    else -> {
                        -1
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return -1
    }


    /**
     * 域名获取
     *
     * @param url 网址
     * @return
     */
    private fun getDomain(url: String): String? {
        var domain: String? = null
        try {
            domain = URI.create(url).host
            if (null == domain && isMatch(ipRegex, url)) {
                domain = url
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return domain
    }

    /**
     * ping方法，调用ping指令
     *
     * @param command ping指令文本
     * @return
     */
    private fun ping(command: String): String? {
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec(command) //执行ping指令
            val inputStream: InputStream = process.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = StringBuilder()
            var line: String?
            while (null != reader.readLine().also { line = it }) {
                sb.append(line)
                sb.append("\n")
            }
            reader.close()
            inputStream.close()
            return sb.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            process?.destroy()
        }
        return null
    }

    /**
     * ping指令格式文本
     *
     * @param count   调用次数
     * @param timeout 超时时间
     * @param domain  地址
     * @return
     */
    private fun createSimplePingCommand(count: Int, timeout: Int, domain: String): String {
        return "/system/bin/ping -c $count -w $timeout $domain"
    }
}