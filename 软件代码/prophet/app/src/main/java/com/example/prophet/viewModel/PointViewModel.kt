package com.example.prophet.viewModel

import android.content.res.Resources
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.prophet.page.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PointViewModel @Inject constructor(): ViewModel(){

    private val _points = mutableStateOf(listOf(
        Point(0f, 0f),Point(50f, 0f), Point(100f, 0f),
        Point(150f, 0f), Point(200f, 0f), Point(250f, 0f),
        Point(300f, 0f), Point(350f, 0f), Point(400f, 0f),
        Point(450f, 0f), Point(500f, 0f), Point(550f, 0f),
        Point(600f, 0f), Point(650f, 0f), Point(700f, 0f),
        Point(750f, 0f), Point(800f, 0f), Point(850f, 0f),
        Point(900f, 0f), Point(950f, 0f), Point(1000f, 0f),
        Point(1050f, 0f), Point(1100f, 0f), Point(1150f, 0f),
        Point(1200f, 0f), Point(1250f, 0f), Point(1300f, 0f),
        Point(1350f, 0f), Point(1400f, 0f), Point(1450f, 0f),
        Point(1500f, 0f), Point(1550f, 0f), Point(1600f, 0f),
        Point(1650f, 0f), Point(1700f, 0f), Point(1750f, 0f),
        Point(1800f, 0f), Point(1850f, 0f), Point(1900f, 0f),
        Point(1950f, 0f), Point(2000f, 0f), Point(2050f, 0f),
        Point(2100f, 0f), Point(2150f, 0f), Point(2200f, 0f),
        Point(2250f, 0f), Point(2300f, 0f), Point(2350f, 0f),
        Point(2400f, 0f), Point(2450f, 0f), Point(2500f, 0f),
        Point(2550f, 0f), Point(2600f, 0f), Point(2650f, 0f),
        Point(2700f, 0f), Point(2750f, 0f), Point(2800f, 0f),
        Point(2850f, 0f), Point(2900f, 0f), Point(2950f, 0f),
    ))

    val points = mutableStateOf(_points.value)

    fun updatePoint(updateList: List<Point>) {
        clear()
        _points.value = updateList
    }

    fun addPoint(point: Point) {
        val newList = _points.value.toMutableList()
        for (i in 0 until newList.size - 1) {
            newList[i] = Point(newList[i].x, newList[i + 1].y)
        }
        newList[newList.size - 1] = Point(newList.last().x, point.y)
        for (i in 0 until newList.size) {
            Log.d("PointViewModel1", "x: ${newList[i].x}, y: ${newList[i].y}")
        }
        _points.value = newList
        points.value = _points.value
    }

    fun getSize(): Int {
        return _points.value.size
    }

    fun clear() {
        _points.value = listOf()
    }

    fun getPoint(index: Int): Point {
        return _points.value[index]
    }


    init {
        Log.d("PointViewModel", "init")
    }

}