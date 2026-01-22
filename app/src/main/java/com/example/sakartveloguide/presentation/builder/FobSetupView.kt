package com.example.sakartveloguide.presentation.builder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.sakartveloguide.domain.model.GeoPoint
import com.example.sakartveloguide.presentation.mission.components.MapViewContainer
import com.example.sakartveloguide.presentation.theme.SakartveloRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.maps.MapLibreMap
import java.util.Locale

@Composable
fun FobSetupView(
    initialCenter: GeoPoint,
    onSetBase: (GeoPoint) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var mapCenter by remember { mutableStateOf(initialCenter) }
    var mapRef by remember { mutableStateOf<MapLibreMap?>(null) }
    var searchQuery by remember { mutableStateOf("") }

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

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    map.getStyle { style ->
                        val options = LocationComponentActivationOptions.builder(context, style).build()
                        map.locationComponent.activateLocationComponent(options)
                        map.locationComponent.isLocationComponentEnabled = true
                    }
                }
            }
        )

        // --- SEARCH BAR OVERLAY ---
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search address...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = SakartveloRed) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    scope.launch {
                        val result = performGeocoding(context, searchQuery)
                        result?.let { latLng ->
                            mapRef?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0))
                        }
                    }
                })
            )
        }

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

// Simple Geocoder Helper
suspend fun performGeocoding(context: Context, query: String): LatLng? = withContext(Dispatchers.IO) {
    try {
        val geocoder = Geocoder(context, Locale.getDefault())
        @Suppress("DEPRECATION")
        val addresses = geocoder.getFromLocationName(query, 1)
        if (!addresses.isNullOrEmpty()) {
            LatLng(addresses[0].latitude, addresses[0].longitude)
        } else null
    } catch (e: Exception) {
        null
    }
}