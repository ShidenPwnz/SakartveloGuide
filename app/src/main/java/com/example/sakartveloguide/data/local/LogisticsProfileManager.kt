package com.example.sakartveloguide.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.sakartveloguide.domain.model.EntryPoint
import com.example.sakartveloguide.domain.model.LogisticsProfile
import com.example.sakartveloguide.domain.model.TransportType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogisticsProfileManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val KEY_ENTRY_POINT = stringPreferencesKey("logistics_entry_point")
        private val KEY_TRANSPORT_TYPE = stringPreferencesKey("logistics_transport_type")
        private val KEY_NEEDS_ACCOMMODATION = booleanPreferencesKey("logistics_needs_accommodation")
        private val KEY_NEEDS_ESIM = booleanPreferencesKey("logistics_needs_esim")
        private val KEY_NEEDS_FLIGHT = booleanPreferencesKey("logistics_needs_flight")
        private val KEY_NEEDS_TRANSPORT = booleanPreferencesKey("logistics_needs_transport")
        // ARCHITECT'S FIX
        private val KEY_START_DATE = longPreferencesKey("logistics_start_date")
        private val KEY_END_DATE = longPreferencesKey("logistics_end_date")
        private val KEY_IS_BY_AIR = booleanPreferencesKey("logistics_is_by_air")
    }

    val logisticsProfile: Flow<LogisticsProfile> = dataStore.data.map { prefs ->
        LogisticsProfile(
            isByAir = prefs[KEY_IS_BY_AIR] ?: true,
            entryPoint = EntryPoint.valueOf(prefs[KEY_ENTRY_POINT] ?: EntryPoint.AIRPORT_TBS.name),
            transportType = TransportType.valueOf(prefs[KEY_TRANSPORT_TYPE] ?: TransportType.RENTAL_4X4.name),
            needsAccommodation = prefs[KEY_NEEDS_ACCOMMODATION] ?: false,
            needsEsim = prefs[KEY_NEEDS_ESIM] ?: false,
            needsFlight = prefs[KEY_NEEDS_FLIGHT] ?: false,
            needsTransport = prefs[KEY_NEEDS_TRANSPORT] ?: false, // ARCHITECT'S FIX
            startDate = prefs[KEY_START_DATE],
            endDate = prefs[KEY_END_DATE]
        )
    }

    suspend fun saveFullProfile(profile: LogisticsProfile) {
        dataStore.edit { prefs ->
            prefs[KEY_IS_BY_AIR] = profile.isByAir
            prefs[KEY_ENTRY_POINT] = profile.entryPoint.name
            prefs[KEY_TRANSPORT_TYPE] = profile.transportType.name
            prefs[KEY_NEEDS_ACCOMMODATION] = profile.needsAccommodation
            prefs[KEY_NEEDS_ESIM] = profile.needsEsim
            prefs[KEY_NEEDS_FLIGHT] = profile.needsFlight
            prefs[KEY_NEEDS_TRANSPORT] = profile.needsTransport // ARCHITECT'S FIX

            profile.startDate?.let { prefs[KEY_START_DATE] = it }
            profile.endDate?.let { prefs[KEY_END_DATE] = it }
        }
    }
}