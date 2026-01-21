package com.example.sakartveloguide.data.local

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.sakartveloguide.domain.model.GeoPoint
import com.example.sakartveloguide.domain.model.MissionState
import com.example.sakartveloguide.domain.model.UserJourneyState
import com.example.sakartveloguide.domain.model.UserSession
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val KEY_STATE = stringPreferencesKey("journey_state")
        private val KEY_ACTIVE_TRIP = stringPreferencesKey("active_trip_id")
        private val KEY_IS_PRO = booleanPreferencesKey("is_pro_user")
        private val KEY_HAS_SEEN_TUTORIAL = booleanPreferencesKey("has_seen_tutorial")
        private val KEY_LANGUAGE = stringPreferencesKey("user_language")

        private val KEY_FOB_LAT = doublePreferencesKey("mission_fob_lat")
        private val KEY_FOB_LNG = doublePreferencesKey("mission_fob_lng")
        private val KEY_HAS_FOB = booleanPreferencesKey("mission_has_fob")

        private val KEY_COMPLETED_NODES = stringSetPreferencesKey("mission_completed_nodes")
        private val KEY_ACTIVE_TARGET_IDX = intPreferencesKey("mission_active_target_idx")
        private val KEY_ACTIVE_LOADOUT = stringPreferencesKey("mission_active_loadout")
    }

    val userSession: Flow<UserSession> = dataStore.data.map { prefs ->
        UserSession(
            activePathId = prefs[KEY_ACTIVE_TRIP],
            state = UserJourneyState.valueOf(prefs[KEY_STATE] ?: UserJourneyState.BROWSING.name),
            isProUser = prefs[KEY_IS_PRO] ?: false,
            activeStepIndex = 0,
            hasSeenTutorial = prefs[KEY_HAS_SEEN_TUTORIAL] ?: false,
            language = prefs[KEY_LANGUAGE] ?: "en"
        )
    }

    val missionState: Flow<MissionState> = dataStore.data.map { prefs ->
        val tripId = prefs[KEY_ACTIVE_TRIP] ?: ""
        val hasFob = prefs[KEY_HAS_FOB] ?: false
        val fob = if (hasFob) {
            GeoPoint(prefs[KEY_FOB_LAT] ?: 0.0, prefs[KEY_FOB_LNG] ?: 0.0)
        } else null

        val completedSet = prefs[KEY_COMPLETED_NODES]?.map { it.toInt() }?.toSet() ?: emptySet()
        val activeIdx = prefs[KEY_ACTIVE_TARGET_IDX]?.let { if (it == -1) null else it }

        MissionState(
            tripId = tripId,
            fobLocation = fob,
            completedNodeIndices = completedSet,
            activeNodeIndex = activeIdx
        )
    }.distinctUntilChanged()

    val activeLoadout: Flow<List<Int>> = dataStore.data.map { prefs ->
        prefs[KEY_ACTIVE_LOADOUT]?.split(",")?.filter { it.isNotEmpty() }?.map { it.toInt() } ?: emptyList()
    }

    // --- ACTIONS ---

    suspend fun saveActiveLoadout(ids: List<Int>) {
        withContext(NonCancellable) {
            dataStore.edit { it[KEY_ACTIVE_LOADOUT] = ids.joinToString(",") }
        }
    }

    suspend fun setFobLocation(location: GeoPoint) {
        withContext(NonCancellable) {
            dataStore.edit { prefs ->
                prefs[KEY_HAS_FOB] = true
                prefs[KEY_FOB_LAT] = location.latitude
                prefs[KEY_FOB_LNG] = location.longitude
                Log.d("TACTICAL_PERSIST", "FOB Saved: ${location.latitude}")
            }
        }
    }

    suspend fun setActiveTarget(index: Int?) {
        withContext(NonCancellable) {
            dataStore.edit { prefs -> prefs[KEY_ACTIVE_TARGET_IDX] = index ?: -1 }
        }
    }

    suspend fun markTargetComplete(index: Int) {
        withContext(NonCancellable) {
            dataStore.edit { prefs ->
                val currentSet = prefs[KEY_COMPLETED_NODES] ?: emptySet()
                prefs[KEY_COMPLETED_NODES] = currentSet + index.toString()
                prefs[KEY_ACTIVE_TARGET_IDX] = -1
            }
        }
    }

    suspend fun updateLanguage(langCode: String) {
        withContext(NonCancellable) { dataStore.edit { it[KEY_LANGUAGE] = langCode } }
    }

    // ARCHITECT'S FIX: Separated state update from data wiping
    suspend fun updateState(state: UserJourneyState, pathId: String? = null) {
        withContext(NonCancellable) {
            dataStore.edit { prefs ->
                prefs[KEY_STATE] = state.name
                pathId?.let { prefs[KEY_ACTIVE_TRIP] = it }
            }
        }
    }

    // NEW: Explicit function to clear mission data when aborting
    suspend fun clearCurrentMissionData() {
        withContext(NonCancellable) {
            dataStore.edit { prefs ->
                prefs[KEY_HAS_FOB] = false
                prefs.remove(KEY_FOB_LAT)
                prefs.remove(KEY_FOB_LNG)
                prefs[KEY_COMPLETED_NODES] = emptySet()
                prefs[KEY_ACTIVE_TARGET_IDX] = -1
                // We do NOT clear the loadout immediately in case they want to retry
            }
        }
    }

    suspend fun setHasSeenTutorial(seen: Boolean) {
        dataStore.edit { it[KEY_HAS_SEEN_TUTORIAL] = seen }
    }

    suspend fun updateStepIndex(index: Int) { }
}