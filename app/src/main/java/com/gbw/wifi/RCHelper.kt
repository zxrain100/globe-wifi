package com.gbw.wifi

import com.google.firebase.remoteconfig.FirebaseRemoteConfig

class RCHelper {

    companion object {
        val instance: RCHelper by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { RCHelper() }
    }

    private lateinit var config: FirebaseRemoteConfig

    fun initFetchAndActivate() {
        config = FirebaseRemoteConfig.getInstance()
        config.setDefaultsAsync(R.xml.remote_config_default)

        config.fetchAndActivate()
    }

    fun getGBWConfig() = config.getString("gbw_config")

}