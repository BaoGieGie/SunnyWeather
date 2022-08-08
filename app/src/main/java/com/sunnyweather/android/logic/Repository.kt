package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.dao.PlaceDao
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext


//仓库层 统一封装入口
//主要工作就是判断调用方请求的数据应该是从本地数据源中获取还是从网络数据源中获取，并将获得的数据返回给调用方。
//因此，仓库层有点像是一个数据获取与缓存的中间层，在本地没有缓存数据的情况下就去网络层获取，如果本地已经有缓存了，就直接将缓存数据返回
//不过个人认为，这种搜索城市数据的请求并没有太多缓存的必要，每次都发起网络请求去获取最新的数据即可，因此这里就不进行本地缓存的实现了
object Repository {
    /**
    //一般在仓库层中定义的方法，为了能将异步获取的数据以响应式编程的方式通知给上一层，通常会返回一个LiveData对象。
    // 这里使用一个新的LiveData技巧。上述代码中的liveData()函数是lifecycle-livedata-ktx库提供的一个非常强大且好用的功能，
    // 可以自动构建并返回一个LiveData对象，然后在它的代码块中提供一个挂起函数的上下文，这样就可以在liveData()函数的代码块中调用任意的挂起函数
    // 这里调用SunnyWeatherNetwork的searchPlaces()函数来搜索城市数据，然后判断如果服务器响应的状态是ok，那么就使用Kotlin内置的Result.success()方法来包装获取的城市数据列表，
    // 否则使用Result.failure()方法来包装一个异常信息。
    // 最后使用emit()方法将包装的结果发射出去，这个emit()方法其实类似于调用LiveData的 setValue()方法来通知数据变化，
    // 只不过这里无法直接取得返回的LiveData对象，所以 lifecycle-livedata-ktx库提供了这样一个替代方法

    //另外需要注意，上述代码中还将liveData()函数的线程参数类型指定成 Dispatchers.IO，这样代码块中的所有代码就都运行在子线程中
    //众所周知，Android是 不允许在主线程中进行网络请求的，诸如读写数据库之类的本地数据操作也是不建议在主线程中进行的，因此非常有必要在仓库层进行一次线程转换
    fun searchPlaces(query: String) = liveData(Dispatchers.IO) {
        val result = try {
            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
            if (placeResponse.status == "ok") {
                val places = placeResponse.places
                Result.success(places)
            } else {
                Result.failure(RuntimeException("response status is${placeResponse.status}"))
            }
        } catch (e: Exception) {
            Result.failure<List<Place>>(e)
        }
        emit(result)
    }

    //注意，并没有提供两个分别用于获取实时天气信息和未来天气信息的方法，而是提供refreshWeather()方法用来刷新天气信息。因为对于调用方而言，需要调用两次请求才能获得其想要的所有天气数据明显是比较烦琐的行为，因此最好的做法就是在仓库层再进行一次统一的封装。
    //不过，获取实时天气信息和获取未来天气信息这两个请求是没有先后顺序的，因此让它们并发执行可以提升程序的运行效率，但是要在同时得到它们的响应结果后才能进一步执行程序。
    //  这种需求有没有让你想起什么呢？恰好协程使用的async函数的作用  只需要分别在两个async函数中发起网络请求，然后再分别调用它们的await()方法，就可以保证只有在两个网络请求都成功响应之后，才会进一步执行程序。
    //  另外，由于async函数必须在协程作用域内才能调用，所以这里又使用coroutineScope函数创建了一个协程作用域。
    //接下来的逻辑就比较简单了，在同时获取到RealtimeResponse和DailyResponse之后，如果它们的响应状态都是ok，那么就将Realtime和Daily对象取出并封装到一个Weather对象中，
    //  然后使用Result.success()方法来包装这个Weather对象，否则就使用Result.failure()方法来包装一个异常信息，最后调用emit()方法将包装的结果发射出去。
    fun refreshWeather(lng: String, lat: String) = liveData(Dispatchers.IO) {
        val result = try {
            coroutineScope {
                val deferredRealtime = async {
                    SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
                }
                val deferredDaily = async {
                    SunnyWeatherNetwork.getDailyWeather(lng, lat)
                }
                val realtimeResponse = deferredRealtime.await()
                val dailyResponse = deferredDaily.await()
                if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                    val weather = Weather(
                        realtimeResponse.result.realtime,
                        dailyResponse.result.daily
                    )
                    Result.success(weather)
                } else {
                    Result.failure(
                        RuntimeException(
                            "realtime response status is ${realtimeResponse.status}" + "daily response status is ${dailyResponse.status}"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure<Weather>(e)
        }
        emit(result)
    }
    */

    //这是按照liveData()函数的参数接收标准定义的一个高阶函数
    //在fire()函数的内部会先调用liveData()函数，然后在liveData()函数的代码块中统一进行了try catch处理，
    //并在try语句中调用传入的Lambda表达式中的代码，最终获取Lambda表达式的执行结果并调用emit()方法发射出去。
    //另外注意，在liveData()函数的代码块中，是拥有挂起函数上下文的，可是当回调到Lambda表达式中，代码就没有挂起函数上下文了，但实际上Lambda表达式中的代码一定也是在挂起函数中运行的。
    //  为了解决这个问题，需要在函数类型前声明一个suspend关键字，以表示所有传入的Lambda表达式中的代码也是拥有挂起函数上下文的。
    //定义好fire()函数之后，需要分别将searchPlaces()和refreshWeather()方法中调用的liveData()函数替换成fire()函数，然后把诸如try catch语句、emit()方法之类的逻辑移除即可
    //  这样，仓库层中的代码就变得更加简洁清晰了
    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure<T>(e)
            }
            emit(result)
        }

    fun searchPlaces(query: String) = fire(Dispatchers.IO) {
        val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
        if (placeResponse.status == "ok") {
            val places = placeResponse.places
            Result.success(places)
        } else {
            Result.failure(RuntimeException("response status is ${placeResponse.status}"))
        }
    }

    fun refreshWeather(lng: String, lat: String) = fire(Dispatchers.IO) {
        coroutineScope {
            val deferredRealtime = async {
                SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
            }
            val deferredDaily = async {
                SunnyWeatherNetwork.getDailyWeather(lng, lat)
            }
            val realtimeResponse = deferredRealtime.await()
            val dailyResponse = deferredDaily.await()
            if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                val weather = Weather(
                    realtimeResponse.result.realtime,
                    dailyResponse.result.daily
                )
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

    /**
     * 仓库层只是做一层接口封装而已。其实这里的实现方式并不标准，
     * 因为即使是对SharedPreferences文件进行读写的操作，也是不太建议在主线程中进行，虽然它的执行速度通常会很快。
     * 最佳的实现方式肯定还是开启一个线程来执行这些比较耗时的任务，然后通过LiveData对象进行数据返回，
     * 不过这里为了让代码看起来更加简单一些，就不使用那么标准的写法
     */
    fun savePlace(place: Place) = PlaceDao.savePlace(place)

    fun getSavedPlace() = PlaceDao.getSavedPlace()

    fun isPlaceSaved() = PlaceDao.isPlaceSaved()
}

