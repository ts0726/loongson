package com.example.prophet.page

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prophet.wifiUtil.setWifiName
import com.example.prophet.wifiUtil.startScan

val wifiList = mutableStateListOf<String>()

@Composable
@Preview
fun PreviewWIFI() {
//    WIFI({})
    wifiItem(wifi = "WIFI", {})
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WIFI(
    navigateUp: () -> Unit,
    connectWifi: () -> Unit
) {

    val context = LocalContext.current

    Scaffold(
        topBar = {
            WIFIAppBar(navigateUp)
        },
    ) {innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding)
        ) {
            wifiList.clear()
            startScan(context)
            items(wifiList.size) { index ->
                wifiItem(wifiList[index], connectWifi)
            }
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WIFIAppBar(
    navigateUp: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        title = {
            Text(
                color = MaterialTheme.colorScheme.primary,
                text = "网络接入"
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
fun wifiItem(
    wifi: String,
    connectWifi: () -> Unit
) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .clickable {
                setWifiName(wifi)
                connectWifi()
            }
    ) {
        Text(
            text = wifi,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "go",
            tint = MaterialTheme.colorScheme.primary
        )
    }

}