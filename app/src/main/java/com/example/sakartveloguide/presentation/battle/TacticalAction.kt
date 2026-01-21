package com.example.sakartveloguide.presentation.battle

import androidx.compose.ui.graphics.vector.ImageVector

sealed class TacticalAction {
    object Idle : TacticalAction()
    data class Execute(
        val label: String,
        val icon: ImageVector,
        val colorHex: Long,
        val intent: android.content.Intent? = null
    ) : TacticalAction()
}