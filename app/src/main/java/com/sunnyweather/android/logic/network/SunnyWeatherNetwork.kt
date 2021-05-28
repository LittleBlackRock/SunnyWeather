package com.sunnyweather.android.logic.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

// 统一的网络数据源访问入口，对所有网络请求的API进行封装
object SunnyWeatherNetwork {
    private val placeService = ServiceCreator.create<PlaceService>()

    // 发起搜索城市数据请求，此处await方法是一种协程式写法
    // 协程式写法的好处：Retrofit立刻发起网络请求，当前协程被阻塞，直到将解析出来的数据模型对象取出并返回
    suspend fun searchPlaces(query: String) = placeService.searchPlaces(query).await()

    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body != null) continuation.resume(body)
                    else continuation.resumeWithException(
                            RuntimeException("response body is null")
                    )
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }
}