package com.sunnyweather.android.logic.dao

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.Place

// 对选中的城市进行记录
// 需要用到持久化技术，且存储数据不属于关系型数据
// 使用SharedPreferences存储
object PlaceDao {
    // 将Place对象存储到SharedPreferences文件中
    fun savePlace(place: Place) {
        sharedPreferences().edit {
            putString("place", Gson().toJson(place)) // 先通过Gson将Place对象转化成JSON字符串，然后用字符串存储方式保存
        }
    }

    // 读取保存数据
    fun getSavedPlace(): Place {
        val placeJson = sharedPreferences().getString("place", "")
        return Gson().fromJson(placeJson, Place::class.java)
    }

    // 判断是否有数据已经被存储
    fun isPlaceSaved() = sharedPreferences().contains("place")

    private fun sharedPreferences() = SunnyWeatherApplication.context.getSharedPreferences(
        "sunny_weather", Context.MODE_PRIVATE
    )
}