package com.gbw.wifi

import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.SpeedTestError
import kotlinx.coroutines.*
import java.math.BigDecimal
import kotlin.coroutines.resume

class NetTestHelper {

    private val testData: MutableList<BigDecimal> = mutableListOf()
    private var ping = -1
    private var rateBit = 0.0

    suspend fun start() {
        coroutineScope {
            val t1 = System.currentTimeMillis()
            //加载广告
            withTimeoutOrNull(15000) {
                val job1 = async {
                    GBWam.instance.build(GBWap.RESULT_NATIVE)
                }
                val job2 = async {
                    withContext(Dispatchers.IO) {
                        speedTest()
                    }
                }
                val job3 = async {
                    withContext(Dispatchers.IO) {
                        getPing()
                    }
                }
                job1.await()
                job2.await()
                job3.await()
            }

            if (testData.isNotEmpty()) {
                val aver = testData.takeLast(5).map { it.toDouble() }.average()
                rateBit = aver

            }

            val t2 = System.currentTimeMillis()
            if (t2 - t1 < 15 * 1000L) {
                delay(15 * 1000L - (t2 - t1))
            }


            GBW.speedInfo.ping = ping
            GBW.speedInfo.rateBit = rateBit

            withContext(Dispatchers.Main) { callback?.invoke() }
        }
    }


    private suspend fun speedTest(): Boolean = suspendCancellableCoroutine {
        testData.clear()
        val speedTestSocket = SpeedTestSocket()
        speedTestSocket.addSpeedTestListener(object : ISpeedTestListener {
            override fun onCompletion(report: SpeedTestReport) {
                //Log.e("NetTestHelper", "onCompletion :${report.progressPercent}")
                it.resume(true)
            }

            override fun onProgress(percent: Float, report: SpeedTestReport) {
//                Log.e(
//                    "NetTestHelper",
//                    "onProgress :percent=${percent},rateBit = ${report.transferRateBit},rateOctet=${report.transferRateOctet}"
//                )
                if (report.transferRateBit.toDouble() > 0f) {
                    testData.add(report.transferRateBit)
                }
            }

            override fun onError(speedTestError: SpeedTestError, errorMessage: String) {
                //Log.e("NetTestHelper", "onError :${speedTestError.name},${errorMessage}")
            }

        })
        speedTestSocket.startFixedDownload(
            "https://isos.ubuntu.mirror.constant.com/20.04.5/ubuntu-20.04.5-desktop-amd64.iso",
            10 * 1000, 1000
        )

    }

    private suspend fun getPing(): Boolean = suspendCancellableCoroutine {
        val pingList = mutableListOf<Int>()
        val ms1 = PingUtils.getRTT("https://www.amazon.com", PingUtils.TYPE_MIN, 1, 10)
        //Log.e("NetTestHelper", "ping amazon :$ms1")
        if (ms1 > 0) {
            pingList.add(ms1)
        }
        val ms2 = PingUtils.getRTT("https://www.facebook.com", PingUtils.TYPE_MIN, 1, 10)
        //Log.e("NetTestHelper", "ping facebook :$ms2")
        if (ms2 > 0) {
            pingList.add(ms2)
        }
        val ms3 = PingUtils.getRTT("https://www.baidu.com", PingUtils.TYPE_MIN, 1, 10)
        //Log.e("NetTestHelper", "ping baidu :$ms3")
        if (ms3 > 0) {
            pingList.add(ms3)
        }

        if (pingList.isNotEmpty()) {
            ping = pingList.min()
        }
        it.resume(true)
    }


    private var callback: (() -> Unit)? = null
    fun setCallback(callback: (() -> Unit)) {
        this.callback = callback
    }

}