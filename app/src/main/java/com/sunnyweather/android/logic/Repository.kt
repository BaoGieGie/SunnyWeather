package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers


//仓库层 统一封装入口
//主要工作就是判断调用方请求的数据应该是从本地数据源中获取还是从网络数据源中获取，并将获得的数据返回给调用方。
//因此，仓库层有点像是一个数据获取与缓存的中间层，在本地没有缓存数据的情况下就去网络层获取，如果本地已经有缓存了，就直接将缓存数据返回
//不过个人认为，这种搜索城市数据的请求并没有太多缓存的必要，每次都发起网络请求去获取最新的数据即可，因此这里就不进行本地缓存的实现了
object Repository {

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
}

//一般在仓库层中定义的方法，为了能将异步获取的数据以响应式编程的方式通知给上一层，通常会返回一个LiveData对象。
// 这里使用一个新的LiveData技巧。上述代码中的liveData()函数是lifecycle-livedata-ktx库提供的一个非常强大且好用的功能，
// 可以自动构建并返回一个LiveData对象，然后在它的代码块中提供一个挂起函数的上下文，这样就可以在liveData()函数的代码块中调用任意的挂起函数
// 这里调用SunnyWeatherNetwork的searchPlaces()函数来搜索城市数据，然后判断如果服务器响应的状态是ok，那么就使用Kotlin内置的Result.success()方法来包装获取的城市数据列表，
// 否则使用Result.failure()方法来包装一个异常信息。
// 最后使用emit()方法将包装的结果发射出去，这个emit()方法其实类似于调用LiveData的 setValue()方法来通知数据变化，
// 只不过这里无法直接取得返回的LiveData对象，所以 lifecycle-livedata-ktx库提供了这样一个替代方法

//另外需要注意，上述代码中还将liveData()函数的线程参数类型指定成 Dispatchers.IO，这样代码块中的所有代码就都运行在子线程中
//众所周知，Android是 不允许在主线程中进行网络请求的，诸如读写数据库之类的本地数据操作也是不建议在主线程中进行的，因此非常有必要在仓库层进行一次线程转换