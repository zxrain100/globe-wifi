package com.gbw.wifi

import android.content.Context
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 安装来源
 */
class IRHelper : CoroutineScope by MainScope() {

    private lateinit var client: InstallReferrerClient

    companion object {
        val instance: IRHelper by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { IRHelper() }
    }

    fun build(context: Context) {
        launch {
            suspendCancellableCoroutine {
                if (AppConfig.instance.getRefUrl().isNotEmpty()) {
                    it.resume(Unit)
                    return@suspendCancellableCoroutine
                }

                client = InstallReferrerClient.newBuilder(context).build()
                client.startConnection(object : InstallReferrerStateListener {
                    override fun onInstallReferrerSetupFinished(responseCode: Int) {
                        if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                            try {
                                if (client.isReady) {
                                    client.let { client ->
                                        val response: ReferrerDetails = client.installReferrer
                                        val referrerUrl: String = response.installReferrer
                                        if (referrerUrl.isNotEmpty()) {
                                            AppConfig.instance.setRefUrl(referrerUrl)

                                        }
                                    }
                                }
                                client.endConnection()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        it.resume(Unit)
                    }

                    override fun onInstallReferrerServiceDisconnected() {
                    }
                })

            }
        }
    }


}