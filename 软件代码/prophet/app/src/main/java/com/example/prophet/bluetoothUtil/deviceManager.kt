package com.example.prophet.bluetoothUtil

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat.startActivityForResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

private lateinit var bluetoothManager: BluetoothManager
private lateinit var bluetoothAdapter: BluetoothAdapter
private  var mmSocket: BluetoothSocket? = null
private var isReading = false
val bluetoothStateFlow = MutableStateFlow(false)

/**
 * 蓝夜初始化，开始发现周围设备
 * 调用该方法时，需要确保设备已获取发现周围设备权限、位置信息权限，并打开蓝牙
 * @param context 上下文
 */
@SuppressLint("MissingPermission")
fun setBluetooth(context: Context) {
    init(context)
    Log.d("Bluetooth", "Bluetooth supported")
    Log.d("Bluetooth", "Bluetooth enabled: ${bluetoothAdapter.isEnabled}")
    if (bluetoothAdapter.startDiscovery()) {
        Log.d("Bluetooth", "Discovery started")
    } else {
        Log.d("Bluetooth", "Discovery failed to start")
    }
}

/**
 * 启动蓝牙
 * @param context 上下文
 */
fun turnOnBluetooth(context: Context): Boolean {
    init(context)
    if (!bluetoothAdapter.isEnabled) {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(context as Activity, enableBtIntent, 12, null)
    }
    return bluetoothAdapter.isEnabled
}

/**
 * 连接蓝牙设备
 * 在调用该方法前，需确保蓝牙设备已与本机进行配对，优先执行pairDevice方法
 * @param context 上下文
 * @param device 蓝牙设备
 */
suspend fun connectDevice(context: Context, device: BluetoothDevice): Boolean {
    init(context)
    context.registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED))
    mmSocket = device.createRfcommSocketToServiceRecord(device.uuids.last().uuid)
    if (mmSocket?.isConnected == true) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Already connected to device: ${device.name}", Toast.LENGTH_SHORT).show()
        }
        return true
    }
    if (bluetoothAdapter.isDiscovering) {
        bluetoothAdapter.cancelDiscovery()
    }
    mmSocket.let { socket ->
        return withContext(Dispatchers.IO) {
            try {
                socket?.connect()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Connected to device: ${device.name}", Toast.LENGTH_SHORT).show()
                }
                bluetoothStateFlow.value = socket?.isConnected == true
                Log.d("bluetoothState0", bluetoothStateFlow.value.toString())
                isReading = true
                true
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to connect to device: ${device.name}", Toast.LENGTH_SHORT).show()
                }
//                socket?.close()
                false
            }
        }
    }
}


/**
 * 配对蓝牙设备
 * @param device 蓝牙设备
 * @param context 上下文
 */
suspend fun pairDevice(device: BluetoothDevice, context: Context): Boolean {
    if (device.bondState != BluetoothDevice.BOND_BONDED) {
        try {
            device.createBond()
            context.registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Pairing with device: ${device.name}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    } else {
        return connectDevice(context, device)
    }
    return false
}

/**
 * 初始化蓝牙
 * @param context 上下文
 */
private fun init(context: Context) {
    bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    bluetoothAdapter = bluetoothManager.adapter
}

/**
 * 断开蓝牙连接
 */
fun disconnect(context: Context) {
    try {
        context.unregisterReceiver(receiver)
        isReading = false
        mmSocket?.close()
        bluetoothStateFlow.value = mmSocket?.isConnected == true
        Log.d("bluetoothState1", bluetoothStateFlow.value.toString())
    } catch (e: IOException) {
        e.printStackTrace()
    }
}


/**
 * 获取蓝牙连接状态
 * @param context 上下文
 */

fun bluetoothState(context: Context) {
    init(context)
    CoroutineScope(Dispatchers.IO).launch {
        while (bluetoothAdapter.isEnabled) {
            bluetoothStateFlow.value = mmSocket?.isConnected == true
        }
        delay(1000)
    }
}


/**
 * 蓝牙广播接收器
 */
val receiver = object: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == intent.action) {
                val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                when (device.bondState) {
                    BluetoothDevice.BOND_BONDED -> {
                        Log.d("Bluetooth", "Paired with device: ${device.name}")
                        CoroutineScope(Dispatchers.IO).launch {
                            connectDevice(context!!, device)
                        }
                    }
                    BluetoothDevice.BOND_BONDING -> {
                        Log.d("Bluetooth", "Pairing with device: ${device.name}")
                    }
                    BluetoothDevice.BOND_NONE -> {
                        Log.d("Bluetooth", "Unpaired with device: ${device.name}")
                    }
                }
            }
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED == intent.action) {
                Log.d("Bluetooth", "Device disconnected")
                Toast.makeText(context, "设备离线", Toast.LENGTH_SHORT).show()
                disconnect(context!!)
            }
            if (BluetoothDevice.ACTION_ACL_CONNECTED == intent.action) {
                Log.d("Bluetooth", "Device connected")
                Toast.makeText(context, "设备已连接", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

/**
 * 写入数据
 * @param data 数据
 */
suspend fun write(data: ByteArray):Boolean {
    return withContext(Dispatchers.IO) {
        try {
            mmSocket?.outputStream?.write(data)
            Log.d("BluetoothData", "Data sent: ${data.toString(Charsets.UTF_8)}")
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
}

/**
 * 读取数据
 */
fun loopRead(): Flow<ByteArray> = flow {
    while (isReading) {
        if (mmSocket == null || mmSocket?.inputStream == null || mmSocket?.isConnected == false) {
            return@flow
        }
        val buffer = ByteArray(1024)
        var bytes: Int? = null
        try {
            while (bytes == null || bytes == 0) {
                bytes = mmSocket?.inputStream?.read(buffer)
            }
            emit(buffer.copyOfRange(0, bytes))
            //结束读取
            isReading = false
        } catch (e: IOException) {
            e.printStackTrace()
            return@flow
        }
    }
}.flowOn(Dispatchers.IO)



