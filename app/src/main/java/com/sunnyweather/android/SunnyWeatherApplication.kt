package com.sunnyweather.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

// 从ViewModel层开始不再持有Activity的引用，会出现缺“Context”的情况
// 因此在这可以建立一个全局获取Context的方式
// 在AndroidManifest中的name标签声明此类
class SunnyWeatherApplication : Application() {
    companion object {

        const val TOKEN = "iRZ25xcqJ48rKfXH"

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}