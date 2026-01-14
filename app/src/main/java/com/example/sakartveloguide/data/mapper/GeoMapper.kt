package com.example.sakartveloguide.data.mapper

import com.example.sakartveloguide.domain.model.GeoPoint
import org.maplibre.android.geometry.LatLng

// Extension function to convert Domain -> MapLibre
fun GeoPoint.toLatLng(): LatLng = LatLng(latitude, longitude)

// Extension function to convert MapLibre -> Domain
fun LatLng.toGeoPoint(): GeoPoint = GeoPoint(latitude, longitude)
