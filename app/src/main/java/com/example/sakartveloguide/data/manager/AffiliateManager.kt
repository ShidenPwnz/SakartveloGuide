package com.example.sakartveloguide.data.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.sakartveloguide.domain.model.RouteCategory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AffiliateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // --- SMART ROUTING ---

    fun getTaxiLink(category: RouteCategory): String {
        return when (category) {
            RouteCategory.CAPITAL, RouteCategory.URBAN, RouteCategory.COASTAL -> "https://bolt.eu"
            else -> "https://gotrip.ge/en"
        }
    }

    fun getRentalLink(): String = "https://localrent.com/en/georgia/"

    fun getWineTourLink(): String = "https://eat-this.ge"

    fun getBookingLink(title: String): String {
        val city = title.split(":").firstOrNull()?.trim() ?: "Georgia"
        return "https://www.booking.com/searchresults.html?ss=$city"
    }

    fun getEsimLink(): String = "https://www.magticom.ge/en/esim"

    // --- EXECUTION ---

    fun openLink(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("AFFILIATE", "Failed to open $url", e)
        }
    }
}