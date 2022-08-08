package com.sunnyweather.android.ui.weather

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sunnyweather.android.R
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {
    val viewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java) }

    lateinit var placeName:TextView
    lateinit var currentTemp:TextView
    lateinit var currentSky:TextView
    lateinit var currentAQI:TextView
    lateinit var nowLayout:RelativeLayout
    lateinit var forecastLayout:LinearLayout
    lateinit var coldRiskText:TextView
    lateinit var dressingText:TextView
    lateinit var ultravioletText:TextView
    lateinit var carWashingText:TextView
    lateinit var weatherLayout:ScrollView
    lateinit var swipeRefresh: SwipeRefreshLayout
    lateinit var navBtn:Button
    lateinit var drawerLayout:DrawerLayout

    /**
     * 在onCreate()方法中，首先从Intent中取出经纬度坐标和地区名称，并赋值到WeatherViewModel的相应变量中；
     * 然后对weatherLiveData对象进行观察，当获取到服务器返回的天气数据时，就调用showWeatherInfo()方法进行解析与展示；
     * 最后，调用了WeatherViewModel的refreshWeather()方法来执行一次刷新天气的请求。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //背景图并没有和状态栏融合到一起
        //调用getWindow().getDecorView()方法拿到当前Activity的DecorView，再调用它的setSystemUiVisibility()方法来改变系统UI的显示，
        // 这里传入View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN和 View.SYSTEM_UI_FLAG_LAYOUT_STABLE就表示Activity的布局会显示在状态栏上面，
        // 最后调用一下setStatusBarColor()方法将状态栏设置成透明色
        val decorView = window.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.activity_weather)

        placeName = findViewById(R.id.placeName)
        currentTemp = findViewById(R.id.currentTemp)
        currentSky = findViewById(R.id.currentSky)
        currentAQI = findViewById(R.id.currentAQI)
        nowLayout = findViewById(R.id.nowLayout)
        forecastLayout = findViewById(R.id.forecastLayout)
        coldRiskText = findViewById(R.id.coldRiskText)
        dressingText = findViewById(R.id.dressingText)
        ultravioletText = findViewById(R.id.ultravioletText)
        carWashingText = findViewById(R.id.carWashingText)
        weatherLayout = findViewById(R.id.weatherLayout)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        navBtn = findViewById(R.id.navBtn)
        drawerLayout = findViewById(R.id.drawerLayout)

        /**
         * 做两件事：第一，在切换城市按钮的点击事件中调用DrawerLayout的openDrawer()方法来打开滑动菜单；
         * 第二，监听DrawerLayout的状态，当滑动菜单被隐藏的时候，同时也要隐藏输入法。
         * 之所以要做这样一步操作，是因为在滑动菜单中搜索城市时会弹出输入法，而如果滑动菜单隐藏后输入法却还显示在界面上，就会是一种非常怪异的情况。
         */
        navBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        })

        if (viewModel.locationLng.isEmpty()) {
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if (viewModel.locationLat.isEmpty()) {
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }
        /*viewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()
            if (weather != null) {
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        })
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)*/

        /**
         * 修改的代码，首先将之前用于刷新天气信息的代码提取到了一个新的refreshWeather()方法中，在里面调用WeatherViewModel的refreshWeather()方法，
         * 并将SwipeRefreshLayout的isRefreshing属性设置成true，从而让下拉刷新进度条显示出来。
         * 然后在onCreate()方法中调用SwipeRefreshLayout的setColorSchemeResources()方法，来设置下拉刷新进度条的颜色，使用colors.xml中的colorPrimary作为进度条的颜色。
         * 接着调用setOnRefreshListener()方法给SwipeRefreshLayout设置一个下拉刷新的监听器，当触发了下拉刷新操作的时候，就在监听器的回调中调用refreshWeather()方法来刷新天气信息。
         * 另外，当请求结束后，还需要将SwipeRefreshLayout的isRefreshing属性设置成false，用于表示刷新事件结束，并隐藏刷新进度条
         */
        viewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()
            if (weather != null) {
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            swipeRefresh.isRefreshing = false              //当请求结束后，还需要将SwipeRefreshLayout的isRefreshing属性设置成false，用于表示刷新事件结束，并隐藏刷新进度条
        })
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary)      //设置下拉刷新进度条的颜色
        refreshWeather()
        swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }
    }

    /**
    * showWeatherInfo()方法，是从Weather对象中获取数据，然后显示到相应的控件上。
    * 注意，在未来几天天气预报的部分，使用for-in循环来处理每天的天气信息，在循环中动态加载forecast_item.xml布局并设置相应的数据，然后添加到父布局中。
    * 另外，生活指数方面虽然服务器会返回很多天的数据，但是界面上只需要当天的数据就可以了，因此这里对所有的生活指数都取了下标为零的那个元素的数据
    * 设置完了所有数据之后，记得要让ScrollView变成可见状态。
    */
    private fun showWeatherInfo(weather: Weather) {
        placeName.text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily

        // 填充now.xml布局中的数据
        val currentTempText = "${realtime.temperature.toInt()} ℃"
        currentTemp.text = currentTempText
        currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        currentAQI.text = currentPM25Text
        nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)

        // 填充forecast.xml布局中的数据
        forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view = LayoutInflater.from(this).inflate(
                R.layout.forecast_item,
                forecastLayout, false
            )
            val dateInfo = view.findViewById(R.id.dateInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
            temperatureInfo.text = tempText
            forecastLayout.addView(view)
        }
        // 填充life_index.xml布局中的数据
        val lifeIndex = daily.lifeIndex
        coldRiskText.text = lifeIndex.coldRisk[0].desc
        dressingText.text = lifeIndex.dressing[0].desc
        ultravioletText.text = lifeIndex.ultraviolet[0].desc
        carWashingText.text = lifeIndex.carWashing[0].desc
        weatherLayout.visibility = View.VISIBLE
    }
    fun refreshWeather() {
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        swipeRefresh.isRefreshing = true            //将SwipeRefreshLayout的isRefreshing属性设置成true，从而让下拉刷新进度条显示出来
    }
}