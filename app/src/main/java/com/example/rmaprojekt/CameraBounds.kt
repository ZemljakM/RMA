package com.example.rmaprojekt

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

object CameraBounds {
    var camerapostion: CameraPosition = CameraPosition.fromLatLngZoom(LatLng(45.5539, 18.695), 11.5f)
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var showSpecifiedLocationOnMap = false


    fun setCameraPosition(position: CameraPosition) {
        camerapostion = position
    }

    fun getCameraPosition(): CameraPosition {
        return camerapostion
    }
}
