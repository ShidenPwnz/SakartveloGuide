package com.example.sakartveloguide.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.sakartveloguide.domain.model.GeoPoint
import com.example.sakartveloguide.domain.model.MissionState
import com.example.sakartveloguide.domain.model.UserJourneyState
import com.example.sakartveloguide.domain.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        // --- SESSION KEYS ---
        private val KEY_STATE = stringPreferencesKey("journey_state")
        private val KEY_ACTIVE_TRIP = stringPreferencesKey("active_trip_id")
        private val KEY_IS_PRO = booleanPreferencesKey("is_pro_user")
        private val KEY_HAS_SEEN_TUTORIAL = booleanPreferencesKey("has_seen_tutorial")
        private val KEY_LANGUAGE = stringPreferencesKey("user_language")

        // --- MISSION STATE KEYS (NEW) ---
        // FOB (Forward Operating Base)
        private val KEY_FOB_LAT = doublePreferencesKey("mission_fob_lat")
        private val KEY_FOB_LNG = doublePreferencesKey("mission_fob_lng")
        private val KEY_HAS_FOB = booleanPreferencesKey("mission_has_fob")

        // Tactical Progress
        private val KEY_COMPLETED_NODES = stringSetPreferencesKey("mission_completed_nodes")
        private val KEY_ACTIVE_TARGET_IDX = intPreferencesKey("mission_active_target_idx") // -1 = Idle
    }

    // 1. GLOBAL USER SESSION
    val userSession: Flow<UserSession> = dataStore.data.map { prefs ->
        UserSession(
            activePathId = prefs[KEY_ACTIVE_TRIP],
            state = UserJourneyState.valueOf(prefs[KEY_STATE] ?: UserJourneyState.BROWSING.name),
            isProUser = prefs[KEY_IS_PRO] ?: false,
            activeStepIndex = 0, // Deprecated in favor of MissionState
            hasSeenTutorial = prefs[KEY_HAS_SEEN_TUTORIAL] ?: false,
            language = prefs[KEY_LANGUAGE] ?: "en"
        )
    }

    // 2. MISSION STATE (The Tactical Save File)
    val missionState: Flow<MissionState> = dataStore.data.map { prefs ->
        val tripId = prefs[KEY_ACTIVE_TRIP] ?: ""

        // Reconstruct FOB
        val hasFob = prefs[KEY_HAS_FOB] ?: false
        val fob = if (hasFob) {
            GeoPoint(
                prefs[KEY_FOB_LAT] ?: 0.0,
                prefs[KEY_FOB_LNG] ?: 0.0
            )
        } else null

        // Reconstruct Progress
        val completedSet = prefs[KEY_COMPLETED_NODES]?.map { it.toInt() }?.toSet() ?: emptySet()
        val activeIdx = prefs[KEY_ACTIVE_TARGET_IDX]?.let { if (it == -1) null else it }

        MissionState(
            tripId = tripId,
            fobLocation = fob,
            completedNodeIndices = completedSet,
            activeNodeIndex = activeIdx
        )
    }

    // --- WRITE OPERATIONS ---

    suspend fun setFobLocation(location: GeoPoint) {
        dataStore.edit { prefs ->
            prefs[KEY_HAS_FOB] = true
            prefs[KEY_FOB_LAT] = location.latitude
            prefs[KEY_FOB_LNG] = location.longitude
        }
    }

    suspend fun setActiveTarget(index: Int?) {
        dataStore.edit { prefs ->
            prefs[KEY_ACTIVE_TARGET_IDX] = index ?: -1
        }
    }

    suspend fun markTargetComplete(index: Int) {
        dataStore.edit { prefs ->
            val currentSet = prefs[KEY_COMPLETED_NODES] ?: emptySet()
            prefs[KEY_COMPLETED_NODES] = currentSet + index.toString()
            // Auto-disengage active target upon completion
            prefs[KEY_ACTIVE_TARGET_IDX] = -1
        }
    }

    suspend fun updateLanguage(langCode: String) {
        dataStore.edit { it[KEY_LANGUAGE] = langCode }
    }

    suspend fun updateState(state: UserJourneyState, pathId: String? = null) {
        dataStore.edit { prefs ->
            prefs[KEY_STATE] = state.name
            pathId?.let {
                // If starting a NEW trip, wipe old mission data
                if (it != prefs[KEY_ACTIVE_TRIP]) {
                    prefs[KEY_HAS_FOB] = false
                    prefs.remove(KEY_FOB_LAT)
                    prefs.remove(KEY_FOB_LNG)
                    prefs[KEY_COMPLETED_NODES] = emptySet()
                    prefs[KEY_ACTIVE_TARGET_IDX] = -1
                }
                prefs[KEY_ACTIVE_TRIP] = it
            }
        }
    }

    suspend fun setHasSeenTutorial(seen: Boolean) {
        dataStore.edit { it[KEY_HAS_FOB] = seen }
    }

    // Legacy support (Deprecated)
    suspend fun updateStepIndex(index: Int) { /* No-op in new architecture */ }
}