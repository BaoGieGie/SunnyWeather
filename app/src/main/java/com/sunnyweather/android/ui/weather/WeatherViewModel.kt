package com.sunnyweather.android.ui.weather

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Location
import com.sunnyweather.android.logic.model.Place


/*
* 定义refreshWeather()方法来刷新天气信息，并将传入的经纬度参数封装成一个Location对象后赋值给locationLiveData对象，
* 然后使用Transformations的switchMap()方法来观察这个对象，并在switchMap()方法的转换函数中调用仓库层中定义的refreshWeather()方法。
* 这样，仓库层返回的LiveData对象就可以转换成一个可供Activity观察的LiveData对象。
*
* 另外，还在WeatherViewModel中定义locationLng、locationLat和placeName这3个变量，它们都是和界面相关的数据，放到ViewModel中可以保证它们在手机屏幕发生旋转的时候不会丢失，在UI层代码中会用到这几个变量
* */
class WeatherViewModel : ViewModel() {
    private val locationLiveData = MutableLiveData<Location>()

    val weatherLiveData = Transformations.switchMap(locationLiveData) { location ->
        Repository.refreshWeather(location.lng, location.lat)
    }

    fun refreshWeather(lng: String, lat: String) {
        locationLiveData.value = Location(lng, lat)
    }

    var locationLng = ""

    var locationLat = ""

    var placeName = ""
}