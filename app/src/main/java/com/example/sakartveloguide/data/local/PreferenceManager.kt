package com.example.sakartveloguide.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.domain.repository.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val authRepository: AuthRepository
) {
    // Helper to ensure all keys are isolated by User ID
    private fun scopedKey(key: String): String {
        val userId = authRepository.currentUser.value?.id ?: "anonymous"
        return "${userId}_$key"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val userSession: Flow<UserSession> = authRepository.currentUser.flatMapLatest { user ->
        val prefix = user?.id ?: "anonymous"
        dataStore.data.map { prefs ->
            UserSession(
                activePathId = prefs[stringPreferencesKey("${prefix}_active_trip_id")],
                state = UserJourneyState.valueOf(prefs[stringPreferencesKey("${prefix}_journey_state")] ?: UserJourneyState.BROWSING.name),
                isProUser = prefs[booleanPreferencesKey("${prefix}_is_pro_user")] ?: false,
                hasSeenTutorial = prefs[booleanPreferencesKey("${prefix}_has_seen_tutorial")] ?: false,
                language = prefs[stringPreferencesKey("${prefix}_user_language")] ?: "en"
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val missionState: Flow<MissionState> = authRepository.currentUser.flatMapLatest { user ->
        val prefix = user?.id ?: "anonymous"
        dataStore.data.map { prefs ->
            val hasFob = prefs[booleanPreferencesKey("${prefix}_mission_has_fob")] ?: false
            MissionState(
                tripId = prefs[stringPreferencesKey("${prefix}_active_trip_id")] ?: "",
                fobLocation = if (hasFob) GeoPoint(
                    prefs[doublePreferencesKey("${prefix}_mission_fob_lat")] ?: 0.0,
                    prefs[doublePreferencesKey("${prefix}_mission_fob_lng")] ?: 0.0
                ) else null,
                completedNodeIndices = prefs[stringSetPreferencesKey("${prefix}_mission_completed_nodes")]
                    ?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet(),
                activeNodeIndex = prefs[intPreferencesKey("${prefix}_mission_active_target_idx")].let { if (it == -1 || it == null) null else it }
            )
        }
    }

    suspend fun updateLanguage(langCode: String) {
        dataStore.edit { it[stringPreferencesKey(scopedKey("user_language"))] = langCode }
    }

    // ARCHITECT'S RESTORATION: Restores the missing tutorial setter
    suspend fun setHasSeenTutorial(seen: Boolean) {
        dataStore.edit { it[booleanPreferencesKey(scopedKey("has_seen_tutorial"))] = seen }
    }

    suspend fun setFobLocation(location: GeoPoint) {
        dataStore.edit { prefs ->
            prefs[booleanPreferencesKey(scopedKey("mission_has_fob"))] = true
            prefs[doublePreferencesKey(scopedKey("mission_fob_lat"))] = location.latitude
            prefs[doublePreferencesKey(scopedKey("mission_fob_lng"))] = location.longitude
        }
    }

    suspend fun updateState(state: UserJourneyState, pathId: String? = null) {
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey(scopedKey("journey_state"))] = state.name
            pathId?.let { prefs[stringPreferencesKey(scopedKey("active_trip_id"))] = it }
        }
    }

    suspend fun setActiveTarget(index: Int?) {
        dataStore.edit { it[intPreferencesKey(scopedKey("mission_active_target_idx"))] = index ?: -1 }
    }

    suspend fun markTargetComplete(locationId: Int) {
        dataStore.edit { prefs ->
            val key = stringSetPreferencesKey(scopedKey("mission_completed_nodes"))
            val currentSet = prefs[key] ?: emptySet()
            prefs[key] = currentSet + locationId.toString()
        }
    }

    suspend fun saveActiveLoadout(tripId: String, ids: List<Int>) {
        dataStore.edit { it[stringPreferencesKey(scopedKey("loadout_$tripId"))] = ids.joinToString(",") }
    }

    fun getActiveLoadout(tripId: String): Flow<List<Int>> = dataStore.data.map { prefs ->
        prefs[stringPreferencesKey(scopedKey("loadout_$tripId"))]
            ?.split(",")
            ?.filter { it.isNotEmpty() }
            ?.mapNotNull { it.toIntOrNull() } ?: emptyList()
    }

    suspend fun clearCurrentMissionData() {
        dataStore.edit { prefs ->
            val tid = prefs[stringPreferencesKey(scopedKey("active_trip_id"))]
            prefs.remove(booleanPreferencesKey(scopedKey("mission_has_fob")))
            prefs.remove(doublePreferencesKey(scopedKey("mission_fob_lat")))
            prefs.remove(doublePreferencesKey(scopedKey("mission_fob_lng")))
            prefs.remove(stringSetPreferencesKey(scopedKey("mission_completed_nodes")))
            prefs.remove(intPreferencesKey(scopedKey("mission_active_target_idx")))
            tid?.let { prefs.remove(stringPreferencesKey(scopedKey("loadout_$it"))) }
        }
    }
}