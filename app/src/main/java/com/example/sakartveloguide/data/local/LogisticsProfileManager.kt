package com.example.sakartveloguide.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.sakartveloguide.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogisticsProfileManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        // Core
        private val KEY_ENTRY_POINT = stringPreferencesKey("logistics_entry_point")
        private val KEY_EXIT_POINT = stringPreferencesKey("logistics_exit_point")
        private val KEY_START_DATE = longPreferencesKey("logistics_start_date")
        private val KEY_END_DATE = longPreferencesKey("logistics_end_date")

        // New Strategy
        private val KEY_STRATEGY = stringPreferencesKey("logistics_strategy")
        private val KEY_VEHICLE_STATUS = stringPreferencesKey("logistics_vehicle_status")

        // Legacy / Toggles
        private val KEY_IS_BY_AIR = booleanPreferencesKey("logistics_is_by_air")
        private val KEY_NEEDS_FLIGHT = booleanPreferencesKey("logistics_needs_flight")
        private val KEY_NEEDS_TRANSPORT = booleanPreferencesKey("logistics_needs_transport")
        private val KEY_NEEDS_ACCOMMODATION = booleanPreferencesKey("logistics_needs_accommodation")
        private val KEY_NEEDS_ESIM = booleanPreferencesKey("logistics_needs_esim")
        private val KEY_TRANSPORT_TYPE = stringPreferencesKey("logistics_transport_type")
    }

    val logisticsProfile: Flow<LogisticsProfile> = dataStore.data.map { prefs ->
        LogisticsProfile(
            // New
            transportStrategy = try {
                TransportStrategy.valueOf(prefs[KEY_STRATEGY] ?: TransportStrategy.PASSENGER_URBAN.name)
            } catch(e: Exception) { TransportStrategy.PASSENGER_URBAN },

            vehicleStatus = try {
                VehicleStatus.valueOf(prefs[KEY_VEHICLE_STATUS] ?: VehicleStatus.NONE.name)
            } catch(e: Exception) { VehicleStatus.NONE },

            // Core
            entryPoint = try { EntryPoint.valueOf(prefs[KEY_ENTRY_POINT] ?: EntryPoint.AIRPORT_TBS.name) } catch(e: Exception) { EntryPoint.AIRPORT_TBS },
            exitPoint = try { EntryPoint.valueOf(prefs[KEY_EXIT_POINT] ?: EntryPoint.AIRPORT_TBS.name) } catch(e: Exception) { EntryPoint.AIRPORT_TBS },
            startDate = prefs[KEY_START_DATE],
            endDate = prefs[KEY_END_DATE],

            // Legacy
            isByAir = prefs[KEY_IS_BY_AIR] ?: true,
            needsFlight = prefs[KEY_NEEDS_FLIGHT] ?: false,
            needsTransport = prefs[KEY_NEEDS_TRANSPORT] ?: false,
            needsAccommodation = prefs[KEY_NEEDS_ACCOMMODATION] ?: false,
            needsEsim = prefs[KEY_NEEDS_ESIM] ?: false,
            transportType = try { TransportType.valueOf(prefs[KEY_TRANSPORT_TYPE] ?: TransportType.TAXI.name) } catch(e: Exception) { TransportType.TAXI }
        )
    }

    suspend fun saveFullProfile(profile: LogisticsProfile) {
        dataStore.edit { prefs ->
            // New
            prefs[KEY_STRATEGY] = profile.transportStrategy.name
            prefs[KEY_VEHICLE_STATUS] = profile.vehicleStatus.name

            // Core
            prefs[KEY_ENTRY_POINT] = profile.entryPoint.name
            prefs[KEY_EXIT_POINT] = profile.exitPoint.name
            profile.startDate?.let { prefs[KEY_START_DATE] = it }
            profile.endDate?.let { prefs[KEY_END_DATE] = it }

            // Legacy
            prefs[KEY_IS_BY_AIR] = profile.isByAir
            prefs[KEY_NEEDS_FLIGHT] = profile.needsFlight
            prefs[KEY_NEEDS_TRANSPORT] = profile.needsTransport
            prefs[KEY_NEEDS_ACCOMMODATION] = profile.needsAccommodation
            prefs[KEY_NEEDS_ESIM] = profile.needsEsim
            prefs[KEY_TRANSPORT_TYPE] = profile.transportType.name
        }
    }
}