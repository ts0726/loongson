package com.example.prophet.page

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.prophet.bluetoothUtil.pairDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


//private lateinit var BLEdevice: BluetoothDevice

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BLEConnect(
    navigateUp: () -> Unit
) {
    Scaffold(
        topBar = {
            connectAppBar(navigateUp)
        }
    ) {

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun connectAppBar(
    navigateUp: () -> Unit
) {

    val context = LocalContext.current

    TopAppBar(
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        title = {
            Text(
                color = MaterialTheme.colorScheme.primary,
                text = "连接设备"
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
