package com.example.prophet.wifiUtil

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.util.Log
import android.widget.Toast
import com.example.prophet.page.wifiList

private lateinit var wifiManager: WifiManager
private var wifiName = "WIFI"

fun startScan(context: Context) {
    init(context)
    Log.d("wifiAvailable", wifiManager.isWifiEnabled.toString())
    context.registerReceiver(wifiScanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
    scanResults()
}

fun init(context: Context) {
    wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
}

val wifiScanReceiver = object: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
        if (success) {
            val results = wifiManager.scanResults
            wifiList.clear()
            for (result in results) {
                wifiList.add(result.wifiSsid.toString())
            }
        } else {
            Toast.makeText(context, "Scan failed", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun scanResults() {
    val results = wifiManager.scanResults
    wifiList.clear()
    for (result in results) {
        if (!wifiList.contains(result.SSID.toString())) {
            wifiList.add(result.SSID.toString())
        }
    }
}

fun setWifiName(name: String) {
    wifiName = name
}

fun getWifiName(): String {
    return wifiName
}