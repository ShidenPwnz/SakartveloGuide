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
        // ARCHITECT'S FIX: New persistence key
        private val KEY_ACTIVE_STEP = intPreferencesKey("active_step_index")
    }

    val userSession: Flow<UserSession> = dataStore.data.map { prefs ->
        UserSession(
            activePathId = prefs[KEY_ACTIVE_TRIP],
            state = UserJourneyState.valueOf(prefs[KEY_STATE] ?: UserJourneyState.BROWSING.name),
            isProUser = prefs[KEY_IS_PRO] ?: false,
            activeStepIndex = prefs[KEY_ACTIVE_STEP] ?: 0 // Load saved index
        )
    }

    suspend fun updateState(state: UserJourneyState, pathId: String? = null) {
        dataStore.edit { prefs ->
            prefs[KEY_STATE] = state.name
            pathId?.let { prefs[KEY_ACTIVE_TRIP] = it }
        }
    }

    // ARCHITECT'S FIX: Save progress instantly
    suspend fun updateStepIndex(index: Int) {
        dataStore.edit { prefs ->
            prefs[KEY_ACTIVE_STEP] = index
        }
    }

    suspend fun setProStatus(isPro: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_IS_PRO] = isPro
        }
    }
}