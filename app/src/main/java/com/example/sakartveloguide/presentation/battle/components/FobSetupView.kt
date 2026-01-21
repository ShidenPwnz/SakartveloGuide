package com.example.sakartveloguide.presentation.battle.components

import android.Manifest
import android.content.Context
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakartveloguide.domain.model.GeoPoint
import com.example.sakartveloguide.presentation.battle.BattleViewModel
import com.example.sakartveloguide.presentation.mission.components.MapViewContainer
import com.example.sakartveloguide.presentation.theme.MatteCharcoal
import com.example.sakartveloguide.presentation.theme.SakartveloRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import java.util.*

@Composable
fun FobSetupView(
    viewModel: BattleViewModel,
    onSetBase: (GeoPoint) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    // ARCHITECT'S FIX: Initialize map at the mission theatre location
    val initialCenter = remember { viewModel.getInitialMapCenter() }
    var mapCenter by remember { mutableStateOf(initialCenter) }

    var searchQuery by remember { mutableStateOf("") }
    var mapRef by remember { mutableStateOf<MapLibreMap?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    Box(modifier = Modifier.fillMaxSize().background(MatteCharcoal)) {
        MapViewContainer(
            modifier = Modifier.fillMaxSize(),
            onMapReady = { map ->
                mapRef = map
                // Fly to initial center on first load
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(initialCenter.latitude, initialCenter.longitude), 13.0
                ))

                map.addOnCameraIdleListener {
                    map.cameraPosition.target?.let { pos ->
                        mapCenter = GeoPoint(pos.latitude, pos.longitude)
                    }
                }
            }
        )
// ... rest of the file

        Icon(Icons.Default.Add, null, tint = SakartveloRed, modifier = Modifier.size(40.dp).align(Alignment.Center))

        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            // SEARCH BAR
            Surface(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                color = MatteCharcoal.copy(0.9f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SakartveloRed.copy(0.5f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                    Icon(Icons.Default.Search, null, tint = SakartveloRed)
                    Spacer(Modifier.width(12.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            focusManager.clearFocus()
                            scope.launch {
                                val result = performGeocoding(context, searchQuery)
                                result?.let {
                                    mapRef?.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 15.0), 2000)
                                }
                            }
                        }),
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                        cursorBrush = SolidColor(SakartveloRed),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) Text("Search location...", color = Color.White.copy(0.4f))
                            innerTextField()
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // FIND ME BUTTON
            FloatingActionButton(
                onClick = {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    scope.launch {
                        val freshLoc = viewModel.getFreshLocation()
                        if (freshLoc != null) {
                            mapRef?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(freshLoc.latitude, freshLoc.longitude), 15.0), 2000)
                        }
                    }
                },
                containerColor = MatteCharcoal.copy(0.9f),
                contentColor = SakartveloRed,
                shape = CircleShape,
                modifier = Modifier.align(Alignment.End).size(48.dp)
            ) { Icon(Icons.Default.MyLocation, null) }

            Spacer(Modifier.weight(1f))

            // CONFIRM CARD
            Surface(
                color = MatteCharcoal.copy(0.95f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SakartveloRed)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("RECONNAISSANCE MODE", color = SakartveloRed, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    Text("Establish Base Coordinates", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            isSaving = true
                            onSetBase(mapCenter)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("CONFIRM FOB", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

// ARCHITECT'S FIX: This function was missing in your last file
suspend fun performGeocoding(context: Context, query: String): LatLng? = withContext(Dispatchers.IO) {
    try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocationName(query, 1)
        if (!addresses.isNullOrEmpty()) {
            val addr = addresses[0]
            LatLng(addr.latitude, addr.longitude)
        } else null
    } catch (e: Exception) {
        null
    }
}