package com.gbw.wifi

import android.util.Base64
import org.json.JSONObject


object GBWap {
    const val LOADING = "gbw_inter_s"
    const val HOME_NATIVE = "gbw_native_h"
    const val RESULT_NATIVE = "gbw_native_r"

    val cacheList = HashMap<String, GBWa>()

    fun hasCache(key: String): Boolean {
        synchronized(cacheList) {
            if (cacheList.isEmpty()) return false
            val cache = cacheList[key] ?: return false
            return if (cache.isAva()) {
                true
            } else {
                cache.onDestroy()
                cacheList.remove(key)
                false
            }
        }
    }

    fun getCache(key: String): GBWa? {
        synchronized(cacheList) {
            if (cacheList.isEmpty()) return null
            val cache = cacheList[key] ?: return null
            return if (!cache.isAva()) {
                cache.onDestroy()
                null
            } else {
                cache
            }
        }
    }

    fun getRequestLists(sk: String): List<GBWau> {
        try {
            val s = RCHelper.instance.getGBWConfig()
            val json = JSONObject(String(Base64.decode(s.toByteArray(), Base64.DEFAULT)))
            val jsonArray = json.getJSONArray("gbw_config")

            val adReqList = mutableListOf<GBWau>()
            for (i in 0 until jsonArray.length()) {
                val obj: JSONObject = jsonArray.getJSONObject(i)
                val key = obj.getString("gbw_key")
                val jsonArray2 = obj.getJSONArray("gbw_ids")
                val au: MutableList<GBWau> = mutableListOf()
                for (j in 0 until jsonArray2.length()) {
                    val obj2: JSONObject = jsonArray2.getJSONObject(j)
                    val id = obj2.getString("gbw_id")
                    val priority = obj2.getInt("gbw_priority")
                    val t = when (obj2.optString("gbw_type")) {
                        "nav" -> 0
                        "inter" -> 1
                        "open" -> 2
                        else -> 1
                    }
                    au.add(GBWau(id, priority, t))
                }
                au.sortBy { -it.priority }
                if (key == sk) {
                    adReqList.addAll(au)
                }
            }
            return adReqList
        } catch (e: Exception) {
            e.printStackTrace()
            return mutableListOf()
        }
    }

}
