package com.sunnyweather.android.logic.dao

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.Place

/**
 * 在PlaceDao类中，封装几个必要的存储和读取数据的接口。
 * savePlace()方法用于将Place对象存储到SharedPreferences文件中，
 * 这里使用一个技巧，先通过GSON将Place对象转成一个JSON字符串，然后就可以用字符串存储的方式来保存数据了。
 * 读取则是相反的过程，在getSavedPlace()方法中，先将JSON字符串从SharedPreferences文件中读取出来，然后再通过GSON将JSON字符串解析成Place对象并返回。
 * 另外，提供isPlaceSaved()方法，用于判断是否有数据已被存储
 */
object PlaceDao {
    fun savePlace(place: Place) {
        sharedPreferences().edit {
            putString("place", Gson().toJson(place))
        }
    }

    fun getSavedPlace(): Place {
        val placeJson = sharedPreferences().getString("place", "")
        return Gson().fromJson(placeJson, Place::class.java)
    }

    fun isPlaceSaved() = sharedPreferences().contains("place")

    private fun sharedPreferences() =
        SunnyWeatherApplication.context.getSharedPreferences("sunny_weather", Context.MODE_PRIVATE)
}