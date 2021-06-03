package com.sunnyweather.android.ui.place

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Place

// ViewModel层，通常和Activity或Fragment一一对应，所以放在ui包下
// 与界面相关的数据都应该放到ViewModel中，以保证数据在屏幕旋转时不会丢失
class PlaceViewModel: ViewModel() {
    private val searchLiveData = MutableLiveData<String>()

    // 缓存在界面上显示的城市数据
    val placeList = ArrayList<Place>()

    val placeLiveData = Transformations.switchMap(searchLiveData) { query ->
        Repository.searchPlaces(query)
    }

    // 将传入的参数赋值给searchLiveData对象，之后用Transformations的switchMap()方法观察对象
    fun searchPlaces(query: String) {
        searchLiveData.value = query
    }

    // 如下几个接口的业务逻辑和PlaceViewModel有关，因此除了在Repository封装，也必须在这里封装一层
    fun savePlace(place: Place) = Repository.savePlace(place)
    fun getSavedPlace() = Repository.getSavedPlace()
    fun isPlaceSaved() = Repository.isPlaceSaved()
}