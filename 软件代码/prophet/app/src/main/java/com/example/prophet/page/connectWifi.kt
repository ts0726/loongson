package com.example.prophet.page

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.prophet.bluetoothUtil.loopRead
import com.example.prophet.bluetoothUtil.write
import com.example.prophet.wifiUtil.getWifiName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ConnectWifi(
    navigateUp: () -> Unit
) {

    val context = LocalContext.current

    Scaffold (
        topBar = {
            ConnectWIFIAppBar(navigateUp)
        }
    ) {innerpadding ->
        PasswardInput(innerpadding, context)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectWIFIAppBar(
    navigateUp: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        title = {
            Text(
                color = MaterialTheme.colorScheme.primary,
                text = "建立连接"
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswardInput(innerPadding: PaddingValues, context: Context) {
    Column (
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        val wifiName = getWifiName()
        val showDialog = remember { mutableStateOf(false) }
        Text(
            modifier = Modifier.padding(16.dp, 50.dp, 16.dp, 8.dp),
            color = MaterialTheme.colorScheme.primary,
            text = "输入密码以连接到\n$wifiName",
            fontSize = 30.sp,
            style = TextStyle(fontWeight = FontWeight.Bold)
        )


        Card (
            modifier = Modifier
                .padding(16.dp, 50.dp, 16.dp, 8.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(100.dp)
        ) {

            var password by remember { mutableStateOf("") }

            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = password,
                textStyle = TextStyle(fontWeight = FontWeight.Bold),
                onValueChange = {
                    password = it
                },
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                placeholder = {
                    Text(
                        text = "密码"
                    )
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = {
                        showDialog.value = true
                        CoroutineScope(Dispatchers.IO).launch {
                            sendData(wifiName, password, context, showDialog)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = "back_button"
                        )
                    }
                }
            )
        }

        if (showDialog.value) {
            LoadnetDialog(showDialog)
        }
    }
}

suspend fun sendData(name: String, password: String, context: Context, showDialog: MutableState<Boolean>) {
    val job = CoroutineScope(Dispatchers.IO).launch {
        val data = "#N:$name;#P:$password".toByteArray()
        var i = 0
        while (i < 3) {
            Log.d("sendData", data.toString(Charsets.UTF_8))
            if (write(data)) {
                loopRead().collect {
                    if (it.toString(Charsets.UTF_8) == "success") {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "设备联网成功", Toast.LENGTH_SHORT)
                                .show()
                        }
                        showDialog.value = false
                        return@collect
                    }
                }
            }
            i++
        }
        withContext(Dispatchers.Main) {
            showDialog.value = false
            Toast.makeText(context, "设备联网失败", Toast.LENGTH_SHORT).show()
        }
    }
    try {
        withTimeout(5000) {
            job.join()
        }
    } catch (e: TimeoutCancellationException) {
        // 处理超时的情况
        withContext(Dispatchers.Main) {
            showDialog.value = false
            Toast.makeText(context, "接收数据超时", Toast.LENGTH_SHORT).show()
        }
    }
}


@Preview
@Composable
fun PreviewconnectWifi() {
//    passwardInput(innerPadding = PaddingValues(0.dp))
    ConnectWifi(navigateUp = {})
}


@Composable
fun LoadnetDialog(showDialog: MutableState<Boolean>) {
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