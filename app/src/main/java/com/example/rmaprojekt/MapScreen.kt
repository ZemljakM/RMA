package com.example.rmaprojekt

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.UiSettings
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun MapScreen(navController: NavHostController) {


        val context = LocalContext.current
        var marker: Marker?
        val markers = mutableListOf<Marker?>()

        var hasLocationPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        val locationPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted ->
                hasLocationPermission = granted
            }
        )

        LaunchedEffect(key1 = true) {
            if (!hasLocationPermission) {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        Scaffold(
            bottomBar = { BottomNavigationBar(navController) }
        ) { paddingValues ->
            Surface(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            ) {
                if (hasLocationPermission) {
                    AndroidView(
                        factory = { context ->
                            MapView(context).apply {
                                onCreate(null)
                                onResume()
                                getMapAsync { googleMap ->
                                    googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                                    googleMap.moveCamera(
                                        CameraUpdateFactory.newCameraPosition(
                                            CameraBounds.getCameraPosition()
                                        )
                                    )
                                    googleMap.setOnCameraMoveListener {
                                        CameraBounds.setCameraPosition(
                                            googleMap.cameraPosition
                                        )
                                    }

                                    googleMap.isMyLocationEnabled = true

                                    val uiSettings: UiSettings = googleMap.uiSettings
                                    uiSettings.isZoomControlsEnabled = true
                                    val locations = mutableListOf<MapMarker>()

                                    FirebaseFirestore.getInstance().collection("stores").get().addOnSuccessListener { documents ->
                                        for (document in documents.documents) {
                                            val name = document.getString("name") ?: ""
                                            val latitude = document.getDouble("latitude") ?: 0.0
                                            val longitude = document.getDouble("longitude") ?: 0.0
                                            val coordinates = LatLng(latitude, longitude)
                                            locations.add(
                                                MapMarker(document.id, name, coordinates)
                                            )
                                        }
                                    }.addOnCompleteListener {
                                        for (location in locations) {
                                            val myMarker = googleMap.addMarker(
                                                MarkerOptions().position(location.cordinates).title(location.name)
                                            )
                                            myMarker!!.tag = location.id
                                            markers.add(myMarker)
                                        }

                                        if (CameraBounds.showSpecifiedLocationOnMap) {
                                            marker = googleMap.addMarker(
                                                MarkerOptions().position(
                                                    LatLng(
                                                        CameraBounds.latitude,
                                                        CameraBounds.longitude
                                                    )
                                                ).icon(
                                                    BitmapDescriptorFactory.defaultMarker(
                                                        BitmapDescriptorFactory.HUE_AZURE
                                                    )
                                                ).title("It's here!")
                                            )

                                            for (mark in markers) {
                                                if (marker!!.position == mark?.position) marker!!.tag = mark.tag
                                            }
                                            googleMap.setOnMapClickListener {
                                                marker!!.remove()
                                            }

                                            CameraBounds.showSpecifiedLocationOnMap = false
                                            marker?.showInfoWindow()
                                        }
                                    }
                                }
                            }
                        }, modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = "Permission not granted for accessing location.",
                        modifier = Modifier.fillMaxSize(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
}


data class MapMarker(
    var id: String,
    var name: String,
    var cordinates: LatLng
)

