package com.example.sakartveloguide.data.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.sakartveloguide.domain.model.RouteCategory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.net.URLEncoder

@Singleton
class AffiliateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // --- PARTNER IDS (CONSTANTS) ---
    companion object {
        // Aviation
        private const val ID_TRAVELPAYOUTS = "sakartvelo_guide_app" // Placeholder marker

        // Ground
        private const val ID_GOTRIP = "sakartvelo_guide"
        private const val ID_LOCALRENT = "sakartvelo_guide"
        private const val ID_12GO = "sakartvelo_guide"

        // Accommodation
        private const val ID_BOOKING = "123456" // Placeholder AID

        // Experiential
        private const val ID_VIATOR = "sakartvelo_guide"
        private const val ID_GYG = "sakartvelo_guide"
        private const val ID_EAT_THIS = "SAKARTVELO_APP" // Coupon code

        // Essentials
        private const val ID_AIRALO = "sakartvelo_guide"
        private const val ID_SAFETYWING = "sakartvelo_guide"
    }

    // --- AVIATION ---
    fun getFlightLink(origin: String = "", destination: String = "TBS"): String {
        // Utilizing Travelpayouts / Aviasales deep link structure
        // https://www.aviasales.com/search/LON0101TBS08011
        return "https://www.aviasales.com/search?origin=$origin&destination=$destination&marker=$ID_TRAVELPAYOUTS"
    }

    // --- GROUND LOGISTICS ---
    fun getTaxiLink(category: RouteCategory, destination: String = ""): String {
        return when (category) {
            // Urban environments: Use Bolt/Yandex (Utility)
            RouteCategory.CAPITAL, RouteCategory.URBAN, RouteCategory.COASTAL -> {
                 "https://bolt.eu/ride?destination=$destination"
            }
            // Rural/Intercity: Use GoTrip (Monetized)
            else -> {
                // Construct dynamic deep link for GoTrip
                // Example: https://gotrip.ge/en/tbilisi-to-stepantsminda
                val routeSlug = if (destination.isNotEmpty()) "tbilisi-to-${destination.lowercase()}" else ""
                "https://gotrip.ge/en/$routeSlug?ref=$ID_GOTRIP"
            }
        }
    }

    fun getRentalLink(): String {
        // LocalRent deep link with affiliate parameter
        return "https://localrent.com/en/georgia/?r=$ID_LOCALRENT"
    }

    fun getTrainLink(): String {
        // 12Go Asia deep link for Georgia trains
        return "https://12go.asia/en/travel/tbilisi/batumi?z=$ID_12GO"
    }

    // --- ACCOMMODATION ---
    fun getBookingLink(title: String): String {
        val city = title.split(":").firstOrNull()?.trim() ?: "Georgia"
        val encodedCity = URLEncoder.encode(city, "UTF-8")
        return "https://www.booking.com/searchresults.html?ss=$encodedCity&aid=$ID_BOOKING"
    }

    // --- EXPERIENTIAL TOURISM ---
    fun getWineTourLink(): String {
        // Eat This! Tours (High Ticket)
        return "https://eat-this.ge/?coupon=$ID_EAT_THIS"
    }

    fun getTourLink(category: RouteCategory, keyword: String): String {
        val query = URLEncoder.encode(keyword, "UTF-8")
        return when (category) {
            RouteCategory.WINE_REGION, RouteCategory.WINE_CELLAR ->
                "https://www.viator.com/searchResults/all?text=$query&pid=$ID_VIATOR" // Viator has strong wine inventory
            RouteCategory.MOUNTAIN, RouteCategory.HIKING ->
                "https://www.getyourguide.com/s?q=$query&partner_id=$ID_GYG" // GYG strong on adventure
            else ->
                "https://www.viator.com/searchResults/all?text=$query&pid=$ID_VIATOR" // Default to Viator
        }
    }

    // --- TRAVEL ESSENTIALS ---
    fun getEsimLink(): String {
        // Airalo deep link
        return "https://www.airalo.com/georgia-esim?ref=$ID_AIRALO"
    }

    fun getInsuranceLink(): String {
        // SafetyWing Nomad Insurance
        return "https://safetywing.com/nomad-insurance?ref=$ID_SAFETYWING"
    }

    fun getLuggageStorageLink(): String {
        // Radical Storage
        return "https://radicalstorage.com?ref=sakartvelo"
    }

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
