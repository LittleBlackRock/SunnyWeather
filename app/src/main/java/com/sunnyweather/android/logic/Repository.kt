package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.dao.PlaceDao
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.lang.Exception
import java.lang.RuntimeException
import kotlin.coroutines.CoroutineContext

// 此类作为仓库层的统一封装入口
object Repository {

    // 为了能将异步获取的数据以响应式编程的方式通知给上一层，通常会返回一个LiveData对象
    // 此处的LiveData函数是lifecycle-live-ktx库提供的功能，可以自动构建并返回一个LiveData对象
    // 并在代码块中提供一个挂起函数的上下文
    // 将LiveData()函数的线程参数类型指定为Dispatchers.IO，这样代码块中所有代码都运行在子线程中
    // Android不允许在主线程中进行网络请求
//    fun searchPlaces(query: String) = liveData(Dispatchers.IO) {
//        val result = try {
//            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
//            if (placeResponse.status == "ok") {
//                val places = placeResponse.places
//                Result.success(places)
//            } else {
//                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
//            }
//        } catch (e: Exception) {
//            Result.failure<List<Place>>(e)
//        }
//        // emit相当于调用LiveData的setValue()方法通知数据变化
//        emit(result)
//    }

    //
    fun searchPlaces(query: String) = fire(Dispatchers.IO) {
        val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
        if (placeResponse.status == "ok") {
            val places = placeResponse.places
            Result.success(places)
        } else {
            Result.failure(RuntimeException("response status is ${placeResponse.status}"))
        }
    }

    // 由于原写法使用协程简化网络回调，导致SunnyWeather中封装的每个网络请求接口都可能抛出异常
    // 因此必须在每个网络请求中都进行try-catch处理，增加代码的复杂度
    // 其实可以在某个统一入口函数中进行封装，使得只进行一次try-catch处理就行
    // 按照liveData()参数标准接收标准定义。
    // 注意在liveData函数的代码块中是有挂起函数上下文的，但回到Lambda表达式中代码就没有挂起函数的上下文了，
    // 但实际上Lambda表达式中的代码一定也是在挂起函数中运行的，
    // 为此需要声明suspend关键字以表示所有传入的Lambda表达式中的代码也是有挂起函数上下文的
    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) = liveData<Result<T>>(context) {
        val result = try {
            block()
        } catch (e: Exception) {
            Result.failure<T>(e)
        }
        emit(result)
    }


    // 这里没有分别提供两个获取实时天气信息和未来天气信息的方法，而是提供refreshWeather方法刷新天气信息
//    fun refreshWeather(lng: String, lat: String) = liveData(Dispatchers.IO) {
//        val result = try {
//            coroutineScope {
//                // 协程式写法，在两个async函数中发起网络请求，再分别调用await方法，就可以保证两个网络请求都成功响应后才进一步执行程序
//                // 由于async函数必须在协程作用域内才能调用，所以使用coroutineScope函数创建协程作用域
//                val deferredRealtime = async {
//                    SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
//                }
//                val deferredDialy = async {
//                    SunnyWeatherNetwork.getDailyWeather(lng, lat)
//                }
//                val realtimeResponse = deferredRealtime.await()
//                val dailyResponse = deferredDialy.await()
//                if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
//                    val weather = Weather(realtimeResponse.result.realtime, dailyResponse.result.daily)
//                    Result.success(weather)
//                } else {
//                    Result.failure(
//                        RuntimeException("realtime response status is ${realtimeResponse.status}" +
//                                "daily response status is ${dailyResponse.status}")
//                    )
//                }
//            }
//        } catch (e: Exception) {
//            Result.failure<Weather>(e)
//        }
//        emit(result)
//    }

    fun refreshWeather(lng: String, lat: String) = fire(Dispatchers.IO) {
        coroutineScope {
            // 协程式写法，在两个async函数中发起网络请求，再分别调用await方法，就可以保证两个网络请求都成功响应后才进一步执行程序
            // 由于async函数必须在协程作用域内才能调用，所以使用coroutineScope函数创建协程作用域
            val deferredRealtime = async {
                SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
            }
            val deferredDialy = async {
                SunnyWeatherNetwork.getDailyWeather(lng, lat)
            }
            val realtimeResponse = deferredRealtime.await()
            val dailyResponse = deferredDialy.await()
            if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                val weather = Weather(realtimeResponse.result.realtime, dailyResponse.result.daily)
                Result.success(weather)
            } else {
                Result.failure(
                    RuntimeException(
                        "realtime response status is ${realtimeResponse.status}" +
                                "daily response status is ${dailyResponse.status}"
                    )
                )
            }
        }
    }

    // 对PlaceDao进行接口封装
    // 这里实现方式不标准，不太建议在主线程中进行
    // 最佳实现方式是开启新线程执行这些耗时任务，通过LiveData对象进行数据返回
    fun savePlace(place: Place) = PlaceDao.savePlace(place)

    fun getSavedPlace() = PlaceDao.getSavedPlace()

    fun isPlaceSaved() = PlaceDao.isPlaceSaved()
}