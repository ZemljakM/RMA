@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.rmaprojekt

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.rmaprojekt.ui.theme.FirstColor
import com.example.rmaprojekt.ui.theme.RmaProjektTheme
import com.example.rmaprojekt.ui.theme.SecondColor
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions


class MainActivity : ComponentActivity() {

    var textResult = mutableStateOf("")

    private val barCodeLauncher = registerForActivityResult(ScanContract()){
        result ->
        if( result.contents == null){ }
        else{
            textResult.value = result.contents
        }
    }

    private fun showCamera(){
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("Scan a QR code")
        options.setCameraId(0)
        options.setBeepEnabled(false)
        options.setOrientationLocked(false)
        barCodeLauncher.launch(options)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){
        isGranted ->
        if(isGranted){
            showCamera()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)


        setContent {
            val navController = rememberNavController()

            RmaProjektTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = FirstColor
                ) {
                    NavHost(navController = navController, startDestination = if (isLoggedIn) "list_screen" else "home_screen") {
                        composable("login_screen") {
                            LoginScreen(navController = navController, context = LocalContext.current)
                        }
                        composable("register_screen") {
                            RegisterScreen(navController = navController, context = LocalContext.current)
                        }
                        composable("home_screen") {
                            HomeScreen(navController = navController)
                        }
                        composable("add_friend_screen") {
                            AddFriendScreen(navController = navController)
                        }
                        composable("list_screen") {
                            ListScreen(navController = navController)
                        }
                        composable("map_screen") {
                            MapScreen(navController = navController)
                        }
                        composable("profile_screen") {
                            ProfileScreen(navController = navController, context = LocalContext.current)
                        }
                        composable("create_list_screen") {
                            CreateListScreen(navController = navController, context = LocalContext.current)
                        }
                        composable("list_details_screen/{listName}") { backStackEntry ->
                            val listName = backStackEntry.arguments?.getString("listName") ?: ""
                            ListDetailsScreen(navController = navController,
                                listName = listName,
                                showCamera = { checkCameraPermission(this@MainActivity)},
                                textResult = textResult,
                                context = LocalContext.current)
                        }
                    }
                }
            }
        }
    }




    private fun checkCameraPermission(context: Context) {
        if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            showCamera()
        }
        else if(shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)){
            Toast.makeText(this@MainActivity, "Camera required", Toast.LENGTH_SHORT ).show()
        }
        else{
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }
}


@Composable
fun BottomNavigationBar(navController: NavHostController) {
    BackHandler {
        (navController.context as MainActivity).finish()
    }
    val items = listOf(
        BottomNavItem("Add Friend", Icons.Default.PersonAdd, "add_friend_screen"),
        BottomNavItem("Lists", Icons.Default.List, "list_screen"),
        BottomNavItem("Map", Icons.Default.Map, "map_screen"),
        BottomNavItem("Profile", Icons.Default.Person, "profile_screen")
    )

    NavigationBar(
        containerColor = FirstColor
    ) {
        items.forEach { item ->
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            var isSelected = currentDestination?.route == item.route
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title, tint = if(isSelected) Color.White else SecondColor) },
                label = { Text(item.title, fontWeight = if(isSelected) FontWeight.ExtraBold else FontWeight.Normal, color = if(isSelected) Color.White else SecondColor) },
                selected = false,
                onClick = { navController.navigate(item.route) }
            )
        }
    }
}


data class BottomNavItem(val title: String, val icon: ImageVector, val route: String)









