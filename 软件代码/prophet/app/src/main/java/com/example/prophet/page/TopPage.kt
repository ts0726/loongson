package com.example.prophet.page

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.prophet.navigation.AppNavHost
import com.example.prophet.navigation.AppState
import com.example.prophet.viewModel.PointViewModel

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@SuppressLint("UnusedContentLambdaTargetStateParameter", "UnusedMaterial3ScaffoldPaddingParameter")
@Preview
@Composable
fun ScaffoldExample() {
    val items = listOf("Home", "My")
    val selectedIndex = remember { mutableIntStateOf(0) }
    val navController = rememberNavController()
    val viewModel: PointViewModel = hiltViewModel()
    val showBottomBar = rememberSaveable { mutableStateOf(true) }
    val appState = remember {
        AppState(
            navController = navController
        )
    }

    if (ContextCompat.checkSelfPermission(
            LocalContext.current,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(
            LocalContext.current as Activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxWidth(),
        bottomBar = {
            if (showBottomBar.value) {
                NavigationBar {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = {
                                when(index) {
                                    0 -> Icon(Icons.Default.Home, contentDescription = "Home")
                                    1 -> Icon(Icons.Default.Person, contentDescription = "My")
                                }
                            },
                            label = { Text(item) },
                            selected = index == selectedIndex.intValue,
                            onClick = {
                                selectedIndex.intValue = index
                                navController.navigate(item)
                            }
                        )
                    }
                }
            }
        }
    ) {innerPadding ->
        AppNavHost(
            appState = appState,
            innerPadding = innerPadding,
            viewModel = viewModel,
            onNavigationToTopPage = { showBottomBar.value = true },
            onNavigationToSecond = { showBottomBar.value = false }
        )
    }
}

