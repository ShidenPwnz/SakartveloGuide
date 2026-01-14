package com.example.sakartveloguide.domain.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun formatNodeDate(startMillis: Long?, nodeIndex: Int, timeLabel: String): String {
        if (startMillis == null) return timeLabel
        
        val calendar = Calendar.getInstance().apply {
            timeInMillis = startMillis
            // Simple heuristic: If it's a multi-day trip, we increment the day
            // You can refine this logic based on your BattleNode 'day' property
            add(Calendar.DAY_OF_YEAR, nodeIndex / 3) // Assume 3 nodes per day for now
        }
        
        val sdf = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
        return "${sdf.format(calendar.time)} • $timeLabel"
    }
}
