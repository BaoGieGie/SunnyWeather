package com.sunnyweather.android.logic.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


//Retrofit构建器 使用PlaceService
object ServiceCreator {
    private const val BASE_URL = "https://api.caiyunapp.com/"       //指定请求跟路径

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())         //指定Retrofit在解析数据时所使用的转换库
        .build()

    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)       //调用Retrofit对象的create()方法创建PlaceService接口的动态代理对象，有此对象后可以随意调用接口中定义的所有方法
                                                                                    //该方法的使用： ServiceCreator.create(PlaceService::class.java)

    inline fun <reified T> create(): T = create(T::class.java)                //利用泛型实化  该方法的使用： ServiceCreator.create<PlaceService>()
}