package com.hydroh.yamibo.common

import android.app.Application
import com.hydroh.yamibo.util.PrefUtils

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        PrefUtils.setFirstLaunch(this, true)
    }
}