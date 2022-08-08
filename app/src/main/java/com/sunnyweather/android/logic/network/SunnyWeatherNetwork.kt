package com.sunnyweather.android.logic.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


//定义统一的网络数据源访问入口，对所有网络请求的API进行封装
//首先使用ServiceCreator创建PlaceService接口的动态代理对象，然后定义searchPlaces()函数，并在这里调用刚刚在PlaceService接口中定义的searchPlaces()方法，以发起搜索城市数据请求。
//但是为了让代码变得更加简洁，使用技巧来简化Retrofit回调的写法。由于是需要借助协程技术来实现的，因此这里又定义了一个await()函数，并将searchPlaces()函数也声明成挂起函数

//这样，当外部调用SunnyWeatherNetwork的searchPlaces()函数时，Retrofit就会立即发起网络请求，同时当前的协程也会被阻塞住。
//直到服务器响应请求之后，await()函数会将解析出来的数据模型对象取出并返回，同时恢复当前协程的执行，searchPlaces()函数在得到await()函数的返回值后会将该数据再返回到上一层
object SunnyWeatherNetwork {

    private val placeService = ServiceCreator.create<PlaceService>()

    //suspend关键字 将函数声明成挂起函数，无法提供协程作用域
    //suspendCoroutine函数  必须在协程作用域或挂起函数中才能调用，接收Lambda参数，立即将当前协程挂起，然后在一个普通的线程中执行Lambda代码
    //Lambda表达式的参数列表上会传入一个Continuation参数，调用它的resume()方法或resumeWithException()可以让协程恢复执行
    //服务器响应的数据会回调到enqueue()方法中传入的Callback实现里面
    // 注意：当发起请求的时候，Retrofit会自动在内部开启子线程，当数据回调到Callback中之后，Retrofit又会自动切换回主线程，整个操作过程中都不用考虑线程切换问题
    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body =
                        response.body()                      //在Callback的onResponse()方法中，调用response.body()方法将会得到Retrofit解析后的对象
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

    //调用接口searchPlace()方法 返回Call<PlaceResponse>对象，再调用它的enqueue()方法，Retrofit就会根据注解中配置的服务器接口地址去进行网络请求
    suspend fun searchPlaces(query: String) = placeService.searchPlaces(query).await()

    //对WeatherService接口的封装和之前对PlaceService接口的封装写法几乎是一模一样的
    private val weatherService = ServiceCreator.create(WeatherService::class.java)

    suspend fun getDailyWeather(lng: String, lat: String) = weatherService.getDailyWeather(lng, lat).await()

    suspend fun getRealtimeWeather(lng: String, lat: String) = weatherService.getRealtimeWeather(lng, lat).await()
}