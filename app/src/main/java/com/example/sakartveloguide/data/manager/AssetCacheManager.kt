package com.example.sakartveloguide.data.manager

import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.sakartveloguide.domain.model.TripPath
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetCacheManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // We access the global Coil loader which manages the Disk Cache
    private val imageLoader = ImageLoader(context)

    suspend fun cacheMissionAssets(trip: TripPath) = withContext(Dispatchers.IO) {
        val urlsToCache = mutableListOf<String>()

        // 1. Capture Cover Image
        if (trip.imageUrl.isNotEmpty()) {
            urlsToCache.add(trip.imageUrl)
        }

        // 2. Capture All Node Images
        trip.itinerary.forEach { node ->
            node.imageUrl?.let {
                if (it.isNotEmpty()) urlsToCache.add(it)
            }
        }

        Log.d("OFFLINE_FORTRESS", "Starting tactical cache for ${urlsToCache.size} assets.")

        // 3. Aggressive Pre-fetch
        urlsToCache.distinct().forEach { url ->
            val request = ImageRequest.Builder(context)
                .data(url)
                // FORCE network to disk
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()

            // Enqueue allows this to happen in parallel/background without blocking UI
            imageLoader.enqueue(request)
        }
    }
}