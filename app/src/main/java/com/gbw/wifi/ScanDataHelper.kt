package com.gbw.wifi

import android.net.wifi.ScanResult

object ScanDataHelper {

    private val scanResults = mutableListOf<ScanResult>()
    private var time: Long = 0


    fun setData(list: List<ScanResult>) {
        scanResults.clear()
        scanResults.addAll(list)
        time = System.currentTimeMillis()
    }

    fun getData(): MutableList<ScanResult> {

        val currentTime = System.currentTimeMillis()
        if (currentTime - time > 2 * 1000 * 60L) {
            return mutableListOf()
        }
        return scanResults
    }

}