package com.example.sakartveloguide.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.domain.repository.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi // ADDED
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val authRepository: AuthRepository
) {
    // ... (Keys remain the same) ...
    // Helper to generate scoped keys (Same as before)
    private fun scopedStringKey(key: String): Preferences.Key<String> {
        val userId = authRepository.currentUser.value?.id ?: "anonymous"
        return stringPreferencesKey("${userId}_$key")
    }
    private fun scopedBoolKey(key: String): Preferences.Key<Boolean> {
        val userId = authRepository.currentUser.value?.id ?: "anonymous"
        return booleanPreferencesKey("${userId}_$key")
    }
    private fun scopedIntKey(key: String): Preferences.Key<Int> {
        val userId = authRepository.currentUser.value?.id ?: "anonymous"
        return intPreferencesKey("${userId}_$key")
    }
    private fun scopedDoubleKey(key: String): Preferences.Key<Double> {
        val userId = authRepository.currentUser.value?.id ?: "anonymous"
        return doublePreferencesKey("${userId}_$key")
    }

    @OptIn(ExperimentalCoroutinesApi::class) // FIX: Opt-in annotation
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

    @OptIn(ExperimentalCoroutinesApi::class) // FIX: Opt-in annotation
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

    // ... (rest of methods updateLanguage, etc. remain the same) ...

    suspend fun updateLanguage(langCode: String) {
        dataStore.edit { it[scopedStringKey("user_language")] = langCode }
    }
    suspend fun setHasSeenTutorial(seen: Boolean) {
        dataStore.edit { it[scopedBoolKey("has_seen_tutorial")] = seen }
    }
    suspend fun setFobLocation(location: GeoPoint) {
        dataStore.edit { prefs ->
            prefs[scopedBoolKey("mission_has_fob")] = true
            prefs[scopedDoubleKey("mission_fob_lat")] = location.latitude
            prefs[scopedDoubleKey("mission_fob_lng")] = location.longitude
        }
    }
    suspend fun updateState(state: UserJourneyState, pathId: String? = null) {
        dataStore.edit { prefs ->
            prefs[scopedStringKey("journey_state")] = state.name
            pathId?.let { prefs[scopedStringKey("active_trip_id")] = it }
        }
    }
    suspend fun setActiveTarget(index: Int?) {
        dataStore.edit { prefs -> prefs[scopedIntKey("mission_active_target_idx")] = index ?: -1 }
    }
    suspend fun markTargetComplete(locationId: Int) {
        dataStore.edit { prefs ->
            val key = stringSetPreferencesKey("${authRepository.currentUser.value?.id ?: "anonymous"}_mission_completed_nodes")
            val currentSet = prefs[key] ?: emptySet()
            prefs[key] = currentSet + locationId.toString()
        }
    }
    suspend fun saveActiveLoadout(ids: List<Int>) {
        dataStore.edit { it[scopedStringKey("active_loadout")] = ids.joinToString(",") }
    }

    @OptIn(ExperimentalCoroutinesApi::class) // FIX: Opt-in annotation
    val activeLoadout: Flow<List<Int>> = authRepository.currentUser.flatMapLatest { user ->
        val prefix = user?.id ?: "anonymous"
        dataStore.data.map { prefs ->
            prefs[stringPreferencesKey("${prefix}_active_loadout")]?.split(",")?.filter { it.isNotEmpty() }?.map { it.toInt() } ?: emptyList()
        }
    }

    suspend fun clearCurrentMissionData() {
        dataStore.edit { prefs ->
            prefs.remove(scopedBoolKey("mission_has_fob"))
            prefs.remove(scopedDoubleKey("mission_fob_lat"))
            prefs.remove(scopedDoubleKey("mission_fob_lng"))
            val completedKey = stringSetPreferencesKey("${authRepository.currentUser.value?.id ?: "anonymous"}_mission_completed_nodes")
            prefs.remove(completedKey)
            prefs.remove(scopedIntKey("mission_active_target_idx"))
        }
    }
}