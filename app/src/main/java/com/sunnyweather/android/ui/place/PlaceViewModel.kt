package com.sunnyweather.android.ui.place

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Place


//ViewModel相当于逻辑层和UI层之间的一个桥梁，虽然它更偏向于逻辑层的部分，但是由于ViewModel通常和Activity或Fragment是一一对应的，因此还是习惯将它们放在一起
class PlaceViewModel:ViewModel() {

    private val searchLiveData = MutableLiveData<String>()

    val placeLiveData = Transformations.switchMap(searchLiveData) { query ->
        Repository.searchPlaces(query)
    }

    fun searchPlaces(query: String) {
        searchLiveData.value = query
    }

    //首先PlaceViewModel中也定义searchPlaces()方法，但是这里并没有直接调用仓库层中的searchPlaces()方法，
    //  而是将传入的搜索参数赋值给searchLiveData对象，并使用Transformations的 switchMap()方法来观察这个对象，否则仓库层返回的LiveData对象将无法进行观察。
    //现在每当searchPlaces()函数被调用时， switchMap()方法所对应的转换函数就会执行
    //  在转换函数中，只需要调用仓库层中定义的searchPlaces()方法就可以发起网络请求，同时将仓库层返回的LiveData对象转换成一个可供Activity观察的LiveData对象。

    val placeList = ArrayList<Place>()
    //另外，定义placeList集合，用于对界面上显示的城市数据进行缓存，
    //因为原则上与界面相关的数据都应该放到ViewModel中，这样可以保证它们在手机屏幕发生旋转的时候不会丢失，会在编写UI层代码的时候用到此集合。

    /**
     * Repository接口的业务逻辑是和PlaceViewModel相关的，因此还得在PlaceViewModel中再进行一层封装才行
     * 由于仓库层中的下面几个接口的内部没有开启线程，因此也不必借助LiveData对象来观察数据变化，直接调用仓库层中相应的接口并返回即可
     */
    fun savePlace(place: Place) = Repository.savePlace(place)

    fun getSavedPlace() = Repository.getSavedPlace()

    fun isPlaceSaved() = Repository.isPlaceSaved()
}

