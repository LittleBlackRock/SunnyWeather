package com.sunnyweather.android.logic.model

import com.google.gson.annotations.SerializedName

// 此类定义的属性与类是按照搜索城市数据接口返回的JSON格式定义的
data class PlaceResponse(val status: String, val places: List<Place>)

data class Place(val name: String, val location: Location,
                 @SerializedName("formatted_address") val address: String)

data class Location(val lng: String, val lat: String)
