package com.example.sakartveloguide.data.manager

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.sakartveloguide.domain.model.GeoPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AffiliateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // --- TAXI PROTOCOLS ---
    fun launchBolt(destination: GeoPoint?) {
        val uri = if (destination != null) {
            "bolt://ride?destination_lat=${destination.latitude}&destination_lng=${destination.longitude}"
        } else "bolt://ride"

        launchOrMarket(uri, "ee.mtakso.client", "https://bolt.eu")
    }

    fun launchYandexGo(destination: GeoPoint?) {
        val uri = if (destination != null) {
            "yandextaxi://route?end-lat=${destination.latitude}&end-lon=${destination.longitude}&level=econom"
        } else "yandextaxi://route"

        launchOrMarket(uri, "com.yandex.taxi", "https://go.yandex.com")
    }

    // --- ACCOMMODATION PROTOCOLS ---
    fun launchBooking() {
        // Deep linking to "Georgia" search
        launchOrMarket("booking://search?query=Georgia", "com.booking", "https://www.booking.com/searchresults.html?ss=Georgia")
    }

    fun launchAirbnb() {
        launchOrMarket("airbnb://", "com.airbnb.android", "https://www.airbnb.com/s/Georgia")
    }

    // --- FLIGHT PROTOCOLS ---
    fun launchSkyscanner() {
        launchOrMarket("skyscanner://", "net.skyscanner.android.main", "https://www.skyscanner.net")
    }

    fun launchWizzAir() {
        launchOrMarket("wizzair://", "com.wizzair.WizzAirApp", "https://wizzair.com")
    }

    // --- CORE LOGIC ---
    private fun launchOrMarket(deepLink: String, packageName: String, webFallback: String) {
        try {
            // 1. Try Native App Deep Link
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            try {
                // 2. Try Opening App in Play Store
                val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                storeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(storeIntent)
            } catch (e2: ActivityNotFoundException) {
                // 3. Fallback to Web Browser
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webFallback))
                webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(webIntent)
            }
        } catch (e: Exception) {
            Log.e("AFFILIATE", "Launch failed", e)
        }
    }
}