package com.gbw.wifi

import android.content.Context
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAdOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GBWam {
    companion object {
        val instance: GBWam by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { GBWam() }
    }

    private lateinit var context: Context
    private var isLoad = hashMapOf<String, Boolean>()

    fun initialize(context: Context): GBWam {
        this.context = context
        try {
            MobileAds.initialize(context) {
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return this
    }

    suspend fun build(key: String): GBWa? = withContext(Dispatchers.Main) {
        val lists = GBWap.getRequestLists(key)
        var ret: GBWa?
        for (req in lists) {
            if (req.id.isNotEmpty()) {
                ret = build(key, req)
                if (ret != null) {
                    return@withContext ret
                }
            }
        }
        return@withContext null
    }

    suspend fun builds(vararg key: String) = withContext(Dispatchers.Main) {
        val list = mutableListOf<GBWa>()
        for (k in key) {
            val ret = build(k)
            if (ret != null) {
                list.add(ret)
            }
        }
        return@withContext list
    }


    fun get(vararg key: String): GBWa? = getCache(*key, index = 0)


    private suspend fun build(key: String, abpAu: GBWau): GBWa? =
        withContext(Dispatchers.Main) {
            if (isLoad[key] == true) {
                return@withContext null
            }
            if (GBWap.hasCache(key)) {
                val abpA = get(key)
                if (abpA != null) {
                    return@withContext abpA
                }
            }
            isLoad[key] = true
            val ret = runCatching {
                when (abpAu.type) {
                    0 -> buildNativeAd(key, abpAu.id)
                    1 -> buildInterstitialAd(key, abpAu.id)
                    2 -> buildOpenAd(key, abpAu.id)
                    else -> buildInterstitialAd(key, abpAu.id)
                }
            }
            isLoad[key] = false

            if (ret.isSuccess) {
                val ad = ret.getOrNull()
                if (ad != null) {
                    return@withContext ad
                }
            }
            return@withContext null
        }


    private fun getCache(vararg key: String, index: Int): GBWa? {
        val count = key.size
        val abpA = GBWap.getCache(key[index])
        return abpA ?: if (index < count - 1) {
            getCache(*key, index = index + 1)
        } else {
            null
        }
    }

    private suspend fun buildNativeAd(key: String, id: String): GBWa {
        return suspendCancellableCoroutine {
            //广告需要ui线程上加载
            val adLoader = AdLoader.Builder(context, id)
                .forNativeAd { abpa ->
                    val nativeAd = GBWa(abpa)
                    GBWap.cacheList[key]?.onDestroy()
                    GBWap.cacheList[key] = nativeAd
                    it.resume(nativeAd)
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
                        it.resumeWithException(Exception(p0.code.toString()))
                    }
                })
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .build()
                )
                .build()
            adLoader.loadAd(AdRequest.Builder().build())
        }
    }


    private suspend fun buildInterstitialAd(key: String, id: String): GBWa {
        return suspendCancellableCoroutine {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(context, id, adRequest, object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    it.resumeWithException(Exception(adError.code.toString()))
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    val abpa = GBWa(interstitialAd)
                    //之前有缓存的广告，先将之前的广告销毁
                    GBWap.cacheList[key]?.onDestroy()
                    GBWap.cacheList[key] = abpa
                    it.resume(abpa)
                }
            })
        }
    }

    private suspend fun buildOpenAd(key: String, id: String): GBWa {
        return suspendCancellableCoroutine {
            AppOpenAd.load(
                context,
                id,
                AdRequest.Builder().build(),
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        super.onAdFailedToLoad(adError)
                        it.resumeWithException(Exception(adError.code.toString()))
                    }

                    override fun onAdLoaded(p0: AppOpenAd) {
                        super.onAdLoaded(p0)
                        val abpa = GBWa(p0)
                        //之前有缓存的广告，先将之前的广告销毁
                        GBWap.cacheList[key]?.onDestroy()
                        GBWap.cacheList[key] = abpa
                        it.resume(abpa)
                    }
                }
            )
        }
    }

}