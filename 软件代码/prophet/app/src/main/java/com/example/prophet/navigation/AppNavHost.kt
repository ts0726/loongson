package com.example.prophet.navigation

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.prophet.page.BLE
import com.example.prophet.page.ConnectWifi
import com.example.prophet.page.Home
import com.example.prophet.page.My
import com.example.prophet.page.WIFI
import com.example.prophet.page.addUser
import com.example.prophet.viewModel.PointViewModel

@SuppressLint("UnrememberedMutableState")
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppNavHost(
    appState: AppState,
    innerPadding: PaddingValues,
    viewModel: PointViewModel,
    onNavigationToTopPage: () -> Unit,
    onNavigationToSecond: () -> Unit
) {
    val navController = appState.navController
    NavHost(
        navController = navController,
        startDestination = "Home"
    ) {


        composable(route = "Home") {
            onNavigationToTopPage()
            Home(
                innerPadding = innerPadding,
                viewModel = viewModel,
                onAddDeviceButtonClisked = { navController.navigate("AddDevice") }
            )
        }

        composable(route = "My") {
            onNavigationToTopPage()
            My(
                innerPadding = innerPadding
            )
        }

        composable(route = "AddDevice") {
            onNavigationToSecond()
            BLE (
                navigateUp = {navController.navigateUp()},
                connect = {navController.navigate("ConnectDevice")},
                connectSuccess = {
                    navController.popBackStack("Home", false)
                    navController.navigate("addPatient")
                }
            )
        }

//        composable(
//            route = "ConnectDevice"
//        ) {
//            onNavigationToSecond()
//            BLEConnect(navigateUp = {navController.navigateUp()})
//        }

        composable(
            route = "WIFI"
        ) {
            onNavigationToSecond()
            WIFI(
                navigateUp = {navController.navigateUp()},
                connectWifi = {
                    navController.popBackStack("Home", false)
                    navController.navigate("connectWIFI")
                }
            )
        }

        composable(
            route = "connectWIFI"
        ) {
            onNavigationToSecond()
            ConnectWifi(
                navigateUp = {navController.navigateUp()}
            )
        }

        composable(
            route = "addPatient"
        ) {
            onNavigationToSecond()
            addUser(
                navigateUp = {navController.navigateUp()}
            )
        }

    }
}

