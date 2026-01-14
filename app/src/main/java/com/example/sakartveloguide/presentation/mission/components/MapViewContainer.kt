package com.example.sakartveloguide.presentation.mission.components

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@Composable
fun MapViewContainer(
    modifier: Modifier = Modifier,
    onMapReady: (MapLibreMap) -> Unit
) {
    val context = LocalContext.current
    val mapTilerKey = "qkMaulJ2NlsVPfbF8xwp"
    val styleUrl = "https://api.maptiler.com/maps/outdoor-v2/style.json?key=$mapTilerKey"

    val mapView = remember { MapView(context) }

    AndroidView(factory = { mapView }, modifier = modifier)

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(mapView) {
        mapView.getMapAsync { map ->
            map.setStyle(styleUrl) { style ->
                // ARCHITECT'S FIX: Sequence validation
                if (ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED) {

                    try {
                        val locationComponent = map.locationComponent
                        // 1. ACTIVATE FIRST
                        locationComponent.activateLocationComponent(
                            LocationComponentActivationOptions
                                .builder(context, style).build()
                        )
                        // 2. ENABLE SECOND
                        locationComponent.isLocationComponentEnabled = true
                        // 3. SET MODES LAST
                        locationComponent.cameraMode = CameraMode.TRACKING
                    } catch (e: Exception) {
                        Log.e("SAKARTVELO", "Location activation failed: ${e.message}")
                    }
                }

                onMapReady(map)
            }
        }
    }
}