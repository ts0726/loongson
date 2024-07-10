package com.example.prophet

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.prophet.MQTT.MqttUtil
import com.example.prophet.page.Home
import com.example.prophet.page.My
import com.example.prophet.page.ScaffoldExample
import com.example.prophet.sqlite.PatientDB
import com.example.prophet.ui.theme.ProphetTheme
import com.example.prophet.viewModel.PointViewModel
import dagger.hilt.android.AndroidEntryPoint

lateinit var dataBase: PatientDB
val mqttUtil = MqttUtil()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val context = this
        setContent {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                window.isNavigationBarContrastEnforced = false
            }
            dataBase = PatientDB.getDB(context)
            ProphetTheme {
                ScaffoldExample()
            }
        }
    }

}






