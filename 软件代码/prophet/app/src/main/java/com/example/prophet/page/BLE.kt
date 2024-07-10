package com.example.prophet.page

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.prophet.bluetoothUtil.pairDevice
import com.example.prophet.bluetoothUtil.setBluetooth
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


val deviceList = mutableStateListOf<BluetoothDevice>()

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
@Preview
fun Preview() {
    BLE({}, {}, {})
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BLE(
    navigateUp: () -> Unit,
    connect: () -> Unit,
    connectSuccess: () -> Unit
) {



    val context = LocalContext.current

    Scaffold (
        topBar = {
            BLEAppBar(navigateUp)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    deviceList.clear()
                    setBluetooth(context)
                },
                modifier = Modifier
                    .padding(20.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
    ) {innerPadding ->
        deviceList.clear()
        setBluetooth(context = LocalContext.current)
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        LocalContext.current.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        LazyColumn(
            modifier = Modifier.padding(innerPadding)
        ) {
            items(deviceList.size) {
                bleItem(deviceList[it], connectSuccess)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BLEAppBar(
    navigateUp: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        title = {
            Text(
                color = MaterialTheme.colorScheme.primary,
                text = "添加设备"
            )
        },
        navigationIcon = {
            IconButton(onClick = { navigateUp() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "back_button"
                )
            }
        },
    )
}

@Composable
fun TestText(innerPadding: PaddingValues, str: String) {
    Column {
        Text(
            text = str,
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .size(50.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}


@SuppressLint("MissingPermission", "UnrememberedMutableState")
@Composable
fun bleItem(
    device: BluetoothDevice,
    connectSuccess: () -> Unit
){
    val showDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                showDialog.value = true
                CoroutineScope(Dispatchers.IO).launch {
                    if (pairDevice(device, context)) {
                        withContext(Dispatchers.Main) {
                            showDialog.value = false
                            connectSuccess()
                        }
                    }
                }
            }
    ) {
        if (device.name == null) {
            Text(
                fontSize = 13.sp,
                text = "Unknown Device",
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Text(
                fontSize = 13.sp,
                text = device.name,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "go",
            tint = MaterialTheme.colorScheme.primary
        )

        if (showDialog.value) {
            LoadDialog(showDialog)
        }
    }
}

private val receiver = object : BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        val action : String? = intent?.action
        when(action){
            BluetoothDevice.ACTION_FOUND -> {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                val deviceName = device?.name
                val deviceAddress = device?.address
                if (device != null && !deviceList.contains(device)) {
                    device?.let { deviceList.add(it) }
                }
                Log.d("Device Found", "Name: $deviceName, Address: $deviceAddress")
            }
        }
    }
}

@Composable
fun LoadDialog(showDialog: MutableState<Boolean>) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
        Dialog(
            onDismissRequest = { showDialog.value = false },
            content = {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.wrapContentSize(Alignment.Center),
                            text = "正在连接设备",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 15.sp,
                            style = TextStyle(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        CircularProgressIndicator(
                            modifier = Modifier.size(50.dp),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                }

            },
        )
    }
}

@Preview
@Composable
fun LoadDialogPreview() {
    LoadDialog(remember { mutableStateOf(true) })
}

