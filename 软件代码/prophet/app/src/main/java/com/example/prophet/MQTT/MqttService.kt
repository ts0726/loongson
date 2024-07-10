package com.example.prophet.MQTT

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.prophet.mqttUtil

import org.eclipse.paho.mqttv5.client.IMqttToken
import org.eclipse.paho.mqttv5.client.MqttActionListener
import org.eclipse.paho.mqttv5.client.MqttAsyncClient
import org.eclipse.paho.mqttv5.client.MqttCallback
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence
import org.eclipse.paho.mqttv5.common.MqttException
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.eclipse.paho.mqttv5.common.packet.MqttProperties
import org.json.JSONArray
import org.json.JSONObject

class MqttService : Service() {

    val TAG = "MqttService"
    val HOST = "tcp://47.120.71.177:1883"
    val USERNAME = "android"
    val PASSWORD = "ts1331_c64c"
    val PUBLISH_TOPIC = "AndroidPublish"
    val SUBSCRIVE_TOPIC = "data"
    val CLIENTID = "android"
    val action = "com.example.prophet.MQTT.MqttService"

    private lateinit var mqttAndroidClient: MqttAsyncClient
    private lateinit var mMqttConnectionOptions: MqttConnectionOptions
    private val handler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? {
        throw UnsupportedOperationException("Not yet implemented");
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        init()
        val filter = IntentFilter()
        filter.addAction(action)
        registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        return super.onStartCommand(intent, flags, startId)
    }

    fun subscribe(mqttClient: MqttAsyncClient, topic: String, qos: Int) {
        if (!mqttClient.isConnected) {
            doClientConnection()
        }
        try {
            mqttClient.subscribe(topic, qos)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun unsubscribe(topic: String) {
        try {
            mqttAndroidClient.unsubscribe(topic)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun publish(topic: String, msg: String, qos: Int) {
        val retained = false
        try {
            mqttAndroidClient.publish(topic, msg.toByteArray(), qos, retained)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun init() {
        Log.d(TAG, "mqtt service init")
        val serverURI = HOST
        try {
            mqttAndroidClient = MqttAsyncClient(serverURI, CLIENTID, MemoryPersistence())
        } catch (e: MqttException) {
            e.printStackTrace()
        }
        mqttAndroidClient.setCallback(mqttCallback)
        mMqttConnectionOptions = MqttConnectionOptions()
        mMqttConnectionOptions.isCleanStart = true
        mMqttConnectionOptions.connectionTimeout = 10
        mMqttConnectionOptions.keepAliveInterval = 60
        mMqttConnectionOptions.userName = USERNAME
        mMqttConnectionOptions.password = PASSWORD.toByteArray()

        var doconnect = true
        val message = "last will message"
        val topic = PUBLISH_TOPIC

        val mqttMessage = MqttMessage(message.toByteArray())

        //最后的遗嘱
        try {
            mMqttConnectionOptions.setWill(topic, mqttMessage)
        } catch (e: MqttException) {
            e.printStackTrace()
            doconnect = false
            mMqttActionListener.onFailure(null, e)
        }
        if (doconnect) {
            doClientConnection()
        }

    }

    private val mqttCallback = object : MqttCallback {
        override fun disconnected(disconnectResponse: MqttDisconnectResponse?) {
        }

        override fun mqttErrorOccurred(exception: MqttException?) {
        }

        override fun messageArrived(topic: String?, message: MqttMessage?) {
            if (message != null) {
                handler.post {
//                    if (topic == "points") {
//                        val data = JSONObject(String(message.payload))
//                        val y = data.getDouble("value").toFloat()
//                        mqttUtil.addNewPoint(0f, y)
//                    }
//                    if (topic == "patientState") {
//                        val data = JSONObject(String(message.payload))
//                        val state = data.getInt("state")
//                        mqttUtil.setPatientState(state)
//                    }
                    if (topic == "data") {
                        Log.d("MQTT", message.payload.toString())
                        val data = JSONObject(String(message.payload))
                        Log.d("MQTT", data.toString())
                        val y = data.getDouble("value").toFloat()
                        val state = data.getInt("state")
                        mqttUtil.addNewPoint(0f, y)
                        mqttUtil.setPatientState(state)
                    }
                }
            }
            val intent = Intent(action)
            val bundle = Bundle()
            bundle.putString("MQTT_RevMsg", message?.payload.toString())
            intent.putExtras(bundle)
            sendBroadcast(intent)
        }

        override fun deliveryComplete(token: IMqttToken?) {
        }

        override fun connectComplete(reconnect: Boolean, serverURI: String?) {
        }

        override fun authPacketArrived(reasonCode: Int, properties: MqttProperties?) {
        }

    }

    private val mMqttActionListener = object : MqttActionListener {
        override fun onSuccess(asyncActionToken: IMqttToken?) {
            Log.d(TAG, "CONNECT SUCCESS")
            try {
                Log.d("TEST", "TEST")
                Log.d("SUBSCRIBE TEST", mqttAndroidClient.subscribe(SUBSCRIVE_TOPIC, 2).toString())
            } catch (e: MqttException) {
                Log.d("TEST1", "TEST1")
                e.printStackTrace()
            }
        }

        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
            exception?.printStackTrace()
            Log.d(TAG, "CONNECT FAILURE")

        }

    }

    private fun doClientConnection() {
        if (!mqttAndroidClient.isConnected && isConnected()) {
            try {
                Log.d(TAG, mMqttConnectionOptions.userName + " " + mMqttConnectionOptions.password)
                mqttAndroidClient.connect(mMqttConnectionOptions, null, mMqttActionListener)
//                Log.d(TAG, "CONNECTED")
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }
    }

    private fun isConnected(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivityManager.activeNetworkInfo
        if (info != null && info.isAvailable) {
            val name = info.typeName
            Log.d(TAG, "连接到网络: $name")
            return true
        } else {
            Log.d(TAG, "没有网络")
            /*没有可用网络的时候，延迟3秒再尝试重连*/
            Handler().postDelayed({ doClientConnection() }, 3000)
            return false
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "onReceive")
            if (intent.action == "com.example.prophet.MQTT.MqttService") {
                val topic = intent.getStringExtra("topic")
                if (topic != null) {
                    subscribe(mqttAndroidClient, topic, 2)
                    handler.post {
                        Toast.makeText(context, "subscribe $topic", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mqttAndroidClient.disconnect()
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }


}