package com.example.prophet.viewModel

import androidx.lifecycle.ViewModel
import javax.inject.Inject

class UserViewModel @Inject constructor(): ViewModel(){

    private var _userName = "admin"
    private var _weight = 0
    private var _height = 0
    private var _age = 0
    private var _BMI = 0.0

    val userName = mutableListOf(_userName)
    val weight = mutableListOf(_weight)
    val height = mutableListOf(_height)
    val age = mutableListOf(_age)
    val BMI = mutableListOf(_BMI)

    fun updateUserInfo(name: String, weight: Int, height: Int, age: Int) {
        _userName = name
        _weight = weight
        _height = height
        _age = age
        _BMI = weight / (height * height).toDouble()
    }

    fun getUserName(): String {
        return _userName
    }

    fun getWeight(): Int {
        return _weight
    }

    fun getHeight(): Int {
        return _height
    }

    fun getAge(): Int {
        return _age
    }

    fun getBMI(): Double {
        return _BMI
    }

}