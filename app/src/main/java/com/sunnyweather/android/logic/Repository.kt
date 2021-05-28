package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import java.lang.Exception
import java.lang.RuntimeException

// 此类作为仓库层的统一封装入口
object Repository {

    // 为了能将异步获取的数据以响应式编程的方式通知给上一层，通常会返回一个LiveData对象
    // 此处的LiveData函数是lifecycle-live-ktx库提供的功能，可以自动构建并返回一个LiveData对象
    // 并在代码块中提供一个挂起函数的上下文
    // 将LiveData()函数的线程参数类型指定为Dispatchers.IO，这样代码块中所有代码都运行在子线程中
    // Android不允许在主线程中进行网络请求
    fun searchPlaces(query: String) = liveData(Dispatchers.IO) {
        val result = try {
            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
            if (placeResponse.status == "OK") {
                val places = placeResponse.places
                Result.success(places)
            } else {
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
        } catch (e: Exception) {
            Result.failure<List<Place>>(e)
        }
        // emit相当于调用LiveData的setValue()方法通知数据变化
        emit(result)
    }
}