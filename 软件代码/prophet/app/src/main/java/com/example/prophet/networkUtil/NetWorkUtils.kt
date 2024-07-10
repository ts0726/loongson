package com.example.prophet.networkUtil

import android.util.Log
import okhttp3.FormBody
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class NetWorkUtils {

    private val url = "http://47.120.71.177:7777/value/getCurrentValues"
    private var netCode: Int = 200

    fun getValues(): ArrayList<Float> {
        val list = ArrayList<Float>()
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()
            val requestBody = FormBody.Builder()
                .build()
            val request = okhttp3.Request.Builder()
                .post(requestBody)
                .url(url)
                .build()
            val response = client.newCall(request).execute()
            if (response.code != 200) {
                Log.d("NetCode", response.code.toString())
                netCode = response.code
            } else {
                val responseData = response.body?.string()
                if (responseData != null) {
                    val rootObject = JSONObject(responseData)
                    val userArray = rootObject.getJSONArray("data")
                    if (userArray.length() == 0) {
                        return list
                    }
                    for (i in 0 until userArray.length()) {
                        val userObject = userArray.getJSONObject(i)
                        val value = userObject.getInt("deviceData")
                        list.add(value.toFloat())
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun getNetCode(): Int {
        return netCode
    }

}