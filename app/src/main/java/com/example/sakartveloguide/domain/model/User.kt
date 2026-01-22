package com.example.sakartveloguide.domain.model

data class SakartveloUser(
    val id: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val idToken: String?
)