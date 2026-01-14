package com.example.sakartveloguide.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
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
        private val KEY_STATE = stringPreferencesKey("journey_state")
        private val KEY_ACTIVE_TRIP = stringPreferencesKey("active_trip_id")
        private val KEY_IS_PRO = booleanPreferencesKey("is_pro_user")
        private val KEY_ACTIVE_STEP = intPreferencesKey("active_step_index")
        private val KEY_HAS_SEEN_TUTORIAL = booleanPreferencesKey("has_seen_tutorial") // NEW
    }

    val userSession: Flow<UserSession> = dataStore.data.map { prefs ->
        UserSession(
            activePathId = prefs[KEY_ACTIVE_TRIP],
            state = UserJourneyState.valueOf(prefs[KEY_STATE] ?: UserJourneyState.BROWSING.name),
            isProUser = prefs[KEY_IS_PRO] ?: false,
            activeStepIndex = prefs[KEY_ACTIVE_STEP] ?: 0,
            hasSeenTutorial = prefs[KEY_HAS_SEEN_TUTORIAL] ?: false
        )
    }

    suspend fun updateState(state: UserJourneyState, pathId: String? = null) {
        dataStore.edit { prefs ->
            prefs[KEY_STATE] = state.name
            pathId?.let { prefs[KEY_ACTIVE_TRIP] = it }
        }
    }

    suspend fun updateStepIndex(index: Int) {
        dataStore.edit { it[KEY_ACTIVE_STEP] = index }
    }

    suspend fun setHasSeenTutorial(seen: Boolean) {
        dataStore.edit { it[KEY_HAS_SEEN_TUTORIAL] = seen }
    }

    suspend fun setProStatus(isPro: Boolean) {
        dataStore.edit { it[KEY_IS_PRO] = isPro }
    }
}