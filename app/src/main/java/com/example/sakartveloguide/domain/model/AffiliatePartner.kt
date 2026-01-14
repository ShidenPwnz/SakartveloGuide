package com.example.sakartveloguide.domain.model

enum class PartnerPlatform {
    BOLT, BOOKING_COM, LOCAL_RENTAL
}

data class AffiliateLink(
    val platform: PartnerPlatform,
    val title: String,
    val description: String,
    val deepLinkUrl: String
)
