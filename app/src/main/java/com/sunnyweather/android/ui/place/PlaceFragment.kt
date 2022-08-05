package com.sunnyweather.android.ui.place

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sunnyweather.android.MainActivity
import com.sunnyweather.android.R

class PlaceFragment : Fragment() {
    //首先，使用lazy函数这种懒加载技术来获取PlaceViewModel的实例，这是一种非常棒的写法，允许在整个类中随时使用viewModel这个变量，而完全不用关心它何时初始化、是否为空等前提条件。
    val viewModel by lazy { ViewModelProvider(this).get(PlaceViewModel::class.java) }

    private lateinit var adapter: PlaceAdapter

    private lateinit var mainActivity: MainActivity
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchPlaceEdit: EditText
    private lateinit var bgImageView: ImageView

    //接下来在onCreateView()方法中加载fragment_place布局，这是Fragment的标准用法
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_place, container, false)
    }

    //最后onActivityCreated()方法，先给RecyclerView设置LayoutManager和适配器，并使用PlaceViewModel中的placeList集合作为数据源
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity != null) mainActivity = activity as MainActivity
        recyclerView = mainActivity.findViewById<RecyclerView>(R.id.recyclerView)
        searchPlaceEdit = mainActivity.findViewById(R.id.searchPlaceEdit)
        bgImageView = mainActivity.findViewById(R.id.bgImageView)

        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        adapter = PlaceAdapter(this, viewModel.placeList)
        recyclerView.adapter = adapter

        //调用EditText的addTextChangedListener()方法来监听搜索框内容的变化情况
        //  每当搜索框中的内容发生了变化，就获取新的内容，然后传递给PlaceViewModel的searchPlaces()方法，这样就可以发起搜索城市数据的网络请求
        //  而当输入搜索框中的内容为空时，就将RecyclerView隐藏起来，同时将那张仅用于美观用途的背景图显示出来。
        searchPlaceEdit.addTextChangedListener { editable ->
            val content = editable.toString()
            if (content.isNotEmpty()) {
                viewModel.searchPlaces(content)
            } else {
                recyclerView.visibility = View.GONE
                bgImageView.visibility = View.VISIBLE
                viewModel.placeList.clear()
                adapter.notifyDataSetChanged()
            }
        }

        //解决了搜索城市数据请求的发起，还要能获取到服务器响应的数据才行，就需要借助LiveData来完成
        //  这里对PlaceViewModel中的placeLiveData对象进行观察，当有任何数据变化时，就会回调到传入的Observer接口实现中
        //  然后对回调的数据进行判断：如果数据不为空，那么就将这些数据添加到PlaceViewModel的placeList集合中，并通知PlaceAdapter刷新界面；
        //  如果数据为空，则说明发生了异常，此时弹出一个Toast提示，并将具体的异常原因打印出来
        viewModel.placeLiveData.observe(mainActivity, Observer { result ->
            val places = result.getOrNull()
            if (places != null) {
                recyclerView.visibility = View.VISIBLE
                bgImageView.visibility = View.GONE
                viewModel.placeList.clear()
                viewModel.placeList.addAll(places)
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(activity, "未能查询到任何地点", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        })
    }
}