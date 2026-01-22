package com.example.sakartveloguide.presentation.builder

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.sakartveloguide.domain.model.GeoPoint
import com.example.sakartveloguide.presentation.mission.components.MapViewContainer
import com.example.sakartveloguide.presentation.theme.SakartveloRed
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style

@Composable
fun FobSetupView(
    initialCenter: GeoPoint,
    onSetBase: (GeoPoint) -> Unit
) {
    val context = LocalContext.current
    var mapCenter by remember { mutableStateOf(initialCenter) }
    var mapRef by remember { mutableStateOf<MapLibreMap?>(null) }

    // ARCHITECT'S FIX: Safe Permission Launcher with correct Options Builder
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                mapRef?.getStyle { style ->
                    if (style.isFullyLoaded) {
                        val options = LocationComponentActivationOptions.builder(context, style).build()
                        mapRef?.locationComponent?.activateLocationComponent(options)
                        mapRef?.locationComponent?.isLocationComponentEnabled = true
                    }
                }
            } catch (e: Exception) {
                Log.e("MAP_ERROR", "Failed to activate location component: ${e.message}")
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        MapViewContainer(
            modifier = Modifier.fillMaxSize(),
            onMapReady = { map ->
                mapRef = map
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(initialCenter.latitude, initialCenter.longitude),
                    13.0
                ))

                map.addOnCameraIdleListener {
                    map.cameraPosition.target?.let { pos ->
                        mapCenter = GeoPoint(pos.latitude, pos.longitude)
                    }
                }

                // Check permission on load
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    map.getStyle { style ->
                        val options = LocationComponentActivationOptions.builder(context, style).build()
                        map.locationComponent.activateLocationComponent(options)
                        map.locationComponent.isLocationComponentEnabled = true
                    }
                }
            }
        )

        // Center Indicator
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = SakartveloRed,
            modifier = Modifier.size(40.dp).align(Alignment.Center)
        )

        // GPS FAB
        FloatingActionButton(
            onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                } else {
                    try {
                        val lastLoc = mapRef?.locationComponent?.lastKnownLocation
                        lastLoc?.let {
                            mapRef?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15.0))
                        }
                    } catch (e: Exception) {
                        Log.e("MAP_GPS", "GPS Error", e)
                    }
                }
            },
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
            containerColor = Color.White,
            contentColor = SakartveloRed,
            shape = CircleShape
        ) {
            Icon(Icons.Default.MyLocation, null)
        }

        // Bottom Action
        Button(
            onClick = { onSetBase(mapCenter) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("CONFIRM HOME", fontWeight = FontWeight.Black)
        }
    }
}