package com.example.prophet.MQTT

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.prophet.page.Point
import com.example.prophet.viewModel.PointViewModel
import org.json.JSONObject


class MqttUtil {

    private lateinit var pointViewModel: PointViewModel
    private var patientState = mutableStateOf("----")

    fun initViewModelMqttService(pointView: PointViewModel) {
        pointViewModel = pointView
    }

    fun initPatientStateMqttService(patientState: MutableState<String>) {
        Log.d("TEST","initPatientStateMqttService")
        this.patientState = patientState
    }

    private fun getIntent(context: Context): Intent {
        val mIntent = Intent(context, MqttService::class.java)
        return mIntent
    }

    fun startMqttService(context: Context) {
        Log.d("TEST","startMqttService")
        context.startService(getIntent(context))
    }

    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val receiverStr = intent?.extras?.getString("MQTT_RevMsg") ?: return
            Log.d("MqttUtil", "Receive message: $receiverStr")
        }
    }

    fun addTopic(context: Context, topic: String) {
        Log.d("TEST","addTopic")
        val intent = Intent("com.example.prophet.MQTT.MqttService")
        intent.putExtra("topic", topic)
        context.sendBroadcast(intent)
    }


    fun addNewPoint(x: Float, y: Float) {
        val point = Point(x, y)
        pointViewModel.addPoint(point)
    }

    fun setPatientState(state: Int) {
        if (state == 0) {
            patientState.value = "无需排尿"
        } else {
            patientState.value = "需要排尿"
        }
    }

    fun getPatientState(): MutableState<String> {
        return patientState
    }

}