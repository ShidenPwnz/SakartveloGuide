package com.example.sakartveloguide.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.sakartveloguide.domain.repository.TripRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class OfflineFortressWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: TripRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val tripId = inputData.getString("TRIP_ID") ?: return@withContext Result.failure()

        try {
            Log.d("OFFLINE_FORTRESS", "Acquiring tactical assets for mission: $tripId")
            val trip = repository.getTripById(tripId) ?: return@withContext Result.failure()

            val urls = mutableListOf<String>()
            if (trip.imageUrl.isNotEmpty()) urls.add(trip.imageUrl)
            trip.itinerary.forEach { node ->
                node.imageUrl?.let { if (it.isNotEmpty()) urls.add(it) }
            }

            val distinctUrls = urls.distinct().filter { it.startsWith("http") }
            val imageLoader = ImageLoader(applicationContext)

            distinctUrls.forEachIndexed { index, url ->
                val request = ImageRequest.Builder(applicationContext)
                    .data(url)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build()

                // Force synchronous download into disk cache
                imageLoader.execute(request)
                Log.d("OFFLINE_FORTRESS", "Cached asset ${index + 1}/${distinctUrls.size}")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("OFFLINE_FORTRESS", "Cache operation failed: ${e.message}")
            Result.retry()
        }
    }
}