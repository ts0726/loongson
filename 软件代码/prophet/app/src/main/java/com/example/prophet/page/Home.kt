package com.example.prophet.page

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prophet.MQTT.MqttUtil
import com.example.prophet.R
import com.example.prophet.bluetoothUtil.bluetoothState
import com.example.prophet.bluetoothUtil.bluetoothStateFlow
import com.example.prophet.bluetoothUtil.disconnect
import com.example.prophet.bluetoothUtil.turnOnBluetooth
import com.example.prophet.bluetoothUtil.write
import com.example.prophet.mqttUtil
import com.example.prophet.networkUtil.NetWorkUtils
import com.example.prophet.viewModel.PointViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.paho.mqttv5.client.MqttAsyncClient

data class Point(var x: Float = 0f, var y: Float = 0f)
open class CardType {
    object Device : CardType()
    object LineChart : CardType()
    object PreviewTime : CardType()
}

private lateinit var mqttAndroidClient: MqttAsyncClient



@RequiresApi(Build.VERSION_CODES.TIRAMISU)

@Preview
@Composable
fun PreviewHome() {
//    val viewModel: PointViewModel = hiltViewModel()
//    Home(
//        innerPadding = PaddingValues(0.dp),
//        viewModel = viewModel,
//        onAddDeviceButtonClisked = {}
//    )
    PreviewTime()
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun Home(
    innerPadding: PaddingValues,
    viewModel: PointViewModel,
    onAddDeviceButtonClisked: () -> Unit
) {

    val context = LocalContext.current

//    CoroutineScope(Dispatchers.Main).launch {
//        loopRead().collect { data ->
//            Toast.makeText(context, data.toString(Charsets.UTF_8), Toast.LENGTH_SHORT).show()
//        }
//    }

    LazyColumn(
        modifier = Modifier
            .padding(innerPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        item {
            topDoker(onAddDeviceButtonClisked)
        }

        val cards = listOf(
            CardType.LineChart,
            CardType.PreviewTime,
            CardType.Device,
        )
        items(cards) { card ->
            when (card) {
                is CardType.LineChart -> LineChartCard(viewModel)
                is CardType.PreviewTime -> PreviewTime()
                is CardType.Device -> DeviceInfoCard()
            }
        }

//        receiveBluetoothData(context = context)
    }

}

@Composable
fun LineChartCard(viewModel: PointViewModel) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(10),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp, 15.dp, 15.dp, 15.dp),
    ) {
        LineChart(viewModel)
    }
}

@Composable
fun LineChart(viewModel: PointViewModel) {
    val data = viewModel.points
    val color1 = MaterialTheme.colorScheme.inversePrimary
    val color2 = MaterialTheme.colorScheme.surface
    val color3 = MaterialTheme.colorScheme.primary
    val context = LocalContext.current
    val path1 = Path()
    val path2 = Path()
//    val mqttUtil = MqttUtil()
    val filter = IntentFilter("com.example.prophet.MQTT.MqttService")

    mqttUtil.initViewModelMqttService(viewModel)
    generatePoints(viewModel, data, context)
    mqttUtil.startMqttService(context)
    context.applicationContext.registerReceiver(
        mqttUtil.broadcastReceiver,
        filter,
        Context.RECEIVER_NOT_EXPORTED
    )
//    mqttUtil.addTopic(context, "points")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Text(
            text = "半小时膀胱充盈度",
            modifier = Modifier.padding(15.dp, 5.dp, 0.dp, 5.dp),
            style = TextStyle(fontWeight = FontWeight.Bold)
        )
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                //在LineChart里对点进行更改，模拟数据采集后的变化
                .clickable {
//                    data.value = data.value.map { it.copy(y = it.y + 10) }
//                    Log.d("LineChart", "click")
                }
        ) {

            //获取画布的宽高
            val canvasWidth = size.width

            //适配不同屏幕尺寸
            val points = data.value.map { it.copy(x = it.x / 2950 * canvasWidth) }

            path1.reset()
            path2.reset()

            path1.moveTo(points[0].x, points[0].y)
            path2.moveTo(points[0].x, points[0].y)

            val max = points.maxOf { it.y }
            val min = points.minOf { it.y }
            for (i in 1 until points.size - 1) {
                Log.d("LineChart", "i: $i, x: ${points[i].x}, y: ${300-points[i].y}")
                val controlPoint1 =
                    Offset(points[i].x + (points[i + 1].x - points[i].x) / 2F, scaleValue(points[i].y, min, max))
                val controlPoint2 =
                    Offset(points[i].x + (points[i + 1].x - points[i].x) / 2F, scaleValue(points[i].y, min, max))
                val endPoint = points[i + 1].copy(y = scaleValue(points[i + 1].y, min, max))
                path1.cubicTo(
                    controlPoint1.x,
                    controlPoint1.y,
                    controlPoint2.x,
                    controlPoint2.y,
                    endPoint.x,
                    endPoint.y
                )
                path2.cubicTo(
                    controlPoint1.x,
                    controlPoint1.y,
                    controlPoint2.x,
                    controlPoint2.y,
                    endPoint.x,
                    endPoint.y
                )
            }
            path2.lineTo(points.last().x, 300f)
            path2.lineTo(points[0].x, 300f)
            path2.close()

            val gradient = Brush.verticalGradient(
                colors = listOf(color1, color2),
                startY = 0f,
                endY = 300f
            )

            drawPath(
                path = path2,
                brush = gradient,
                style = Fill,
            )

            drawPath(
                path = path1,
                color = color3,
                style = Stroke(width = 5f)
            )

            drawLine(
                start = Offset(10f, 100f),
                end = Offset(canvasWidth - 10, 100f),
                color = Color.Red,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f), 0f),
                strokeWidth = 5f
            )
        }
    }
}


@Composable
fun PreviewTime() {
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    var patientState = remember { mutableStateOf("----") }

    val filter = IntentFilter("com.example.prophet.MQTT.MqttService")

    mqttUtil.initPatientStateMqttService(patientState)
    mqttUtil.startMqttService(context)
    context.applicationContext.registerReceiver(
        mqttUtil.broadcastReceiver,
        filter,
        Context.RECEIVER_NOT_EXPORTED
    )
//    mqttUtil.addTopic(context, "patient")

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(10),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp, 0.dp, 15.dp, 15.dp)
            .clickable {
            },
    ) {
        Row {
            Text(
                text = "尿意感知",
                modifier = Modifier.padding(15.dp),
                style = TextStyle(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {
                showDialog.value = true
            }) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "info"
                )
            }
        }
        patientState = mqttUtil.getPatientState()
        Text(
            text = patientState.value,
            modifier = Modifier
                .padding(15.dp),
            fontSize = 30.sp,
            style = TextStyle(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        if (showDialog.value){
            infoDialog(
                onDissmissRequest = { showDialog.value = false },
                onConfirmation = { showDialog.value = false },
                dialogTitle = "尿意感知",
                dialogText = "尿意感知是指人体感知到膀胱内尿液的存在，产生排尿欲望的过程。尿意感知是膀胱功能的重要表现之一，是膀胱功能的重要指标之一。\n"+
                        "设备通过深度学习模型帮助患者重新感知尿意，提醒患者及时排尿。\n" +
                        "\n*注意：尿意感知功能仅具有参考作用，无法保证百分百准确，请及时留意患者身体状况。",
                icon = Icons.Default.Info
            )
        }


    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
@Preview
fun DeviceInfoCard() {
    val context = LocalContext.current
    bluetoothState(context)
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(10),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp, 0.dp, 15.dp, 15.dp)
            .clickable {
                Log.d("DeviceInfoCard", "click")
                val mqttUtil = MqttUtil()
                mqttUtil.startMqttService(context)
                val filter = IntentFilter("com.example.prophet.MQTT.MqttService")
                context.applicationContext.registerReceiver(
                    mqttUtil.broadcastReceiver,
                    filter,
                    Context.RECEIVER_NOT_EXPORTED
                )
                val data = "Hello, this is a message from Android"
                val byteData = data.toByteArray()
                CoroutineScope(Dispatchers.IO).launch {
                    write(byteData)
                }
//                mqttUtil.connect(context, clientId = "android")
            },
    ) {
        Row {
            Text(
                text = "设备状态",
                modifier = Modifier.padding(15.dp),
                style = TextStyle(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = deviceState(bluetoothStateFlow),
                modifier = Modifier.padding(15.dp),
                style = TextStyle(fontWeight = FontWeight.Bold)
            )
        }

    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun topDoker(
    onAddDeviceButtonClicked: () -> Unit
) {
    val bluetoothPermission = rememberPermissionState(android.Manifest.permission.BLUETOOTH_SCAN)
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp, 15.dp, 15.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Surface(
            onClick = {
//                disconnect(context)
            },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .size(40.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.avatar),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                contentDescription = "User Image",
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Crop,

            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                when {
                    bluetoothPermission.hasPermission -> {
                        if (turnOnBluetooth(context)) {
                            onAddDeviceButtonClicked()
                            Toast.makeText(context, bluetoothStateFlow.value.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                    else -> {
                        showDialog.value = true
                    }
                }
            },
            modifier = Modifier
                .height(40.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
        }
    }

    if (showDialog.value) {
        BluetoothAlertDialog(
            onDissmissRequest = { showDialog.value = false },
            onConfirmation = { intentSettingPage(context, showDialog) },
            dialogTitle = "需要“附近设备”权限",
            dialogText = "应用需要获取蓝牙权限才可正常搜索设备，请前往设置页面，并在权限管理中开启“附近设备”权限",
            icon = Icons.Default.Warning
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothAlertDialog(
    onDissmissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector
) {
    AlertDialog(
        icon = { Icon(icon, contentDescription = "icon") },
        title = { Text(dialogTitle) },
        text = { Text(dialogText) },
        onDismissRequest = { onDissmissRequest() },
        confirmButton = {
            TextButton(
                onClick = { onConfirmation() }
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDissmissRequest() }
            ) {
                Text("取消")
            }
        }
    )
}

@Composable
fun infoDialog(
    onDissmissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector
) {
    AlertDialog(
        icon = { Icon(icon, contentDescription = "icon") },
        title = { Text(dialogTitle) },
        text = { Text(dialogText) },
        onDismissRequest = { onDissmissRequest() },
        confirmButton = {
            TextButton(
                onClick = { onConfirmation() }
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDissmissRequest() }
            ) {
                Text("取消")
            }
        }
    )
}

private fun intentSettingPage(context: Context, showDialog: MutableState<Boolean>) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", context.packageName, null)
    intent.data = uri
    context.startActivity(intent)
    showDialog.value = false
}


/**
 * 通过网络请求获取数据生成绘制点位
 * @param pointViewModel 点位ViewModel
 * @param data 点位数据
 * @param context 上下文
 *
 * 使用协程进行网络请求，更新视图时需在主线程处理
 */
fun generatePoints(
    pointViewModel: PointViewModel,
    data: MutableState<List<Point>>,
    context: Context
) = CoroutineScope(Dispatchers.IO).launch {
    Log.d("Home", "generatePoints")
    val netWorkUtils = NetWorkUtils()
    val xValues = netWorkUtils.getValues()
    val points = mutableListOf<Point>()
    Log.d("Home", "NetCode: ${netWorkUtils.getNetCode()}")
    if (netWorkUtils.getNetCode() == 200) {
        for (i in 0 until xValues.size) {
            points.add(Point(50f * i, xValues[i]))
            data.value = points
        }

        withContext(Dispatchers.Main) {
            pointViewModel.updatePoint(points)
        }

    } else {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show()
        }
    }
}


@Composable
fun deviceState(bluetoothStateFlow: Flow<Boolean>): String {
    val isConnected = bluetoothStateFlow.collectAsState(initial = false)
//    if (isConnected.value) {
////        loopRead()
//    }
    return if (isConnected.value) "蓝牙已连接" else "蓝牙未连接"
}

fun scaleValue(value: Float, min: Float, max: Float): Float {
    return ((value - min) / (max - min)) * 300
}



