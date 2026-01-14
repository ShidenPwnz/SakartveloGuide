package com.example.sakartveloguide.presentation.home

import com.example.sakartveloguide.data.local.LogisticsProfileManager
import com.example.sakartveloguide.data.local.PreferenceManager
import com.example.sakartveloguide.data.manager.AffiliateManager
import com.example.sakartveloguide.data.manager.AssetCacheManager
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.domain.repository.TripRepository
import com.example.sakartveloguide.domain.usecase.AddPassportStampUseCase
import com.example.sakartveloguide.ui.manager.HapticManager
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel

    private val repository: TripRepository = mockk(relaxed = true)
    private val addPassportStampUseCase: AddPassportStampUseCase = mockk(relaxed = true)
    private val hapticManager: HapticManager = mockk(relaxed = true)
    private val preferenceManager: PreferenceManager = mockk(relaxed = true)
    private val logisticsProfileManager: LogisticsProfileManager = mockk(relaxed = true)
    private val assetCacheManager: AssetCacheManager = mockk(relaxed = true)
    private val affiliateManager: AffiliateManager = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        coEvery { repository.getAvailableTrips() } returns flowOf(emptyList())
        coEvery { logisticsProfileManager.logisticsProfile } returns flowOf(LogisticsProfile())
        coEvery { preferenceManager.userSession } returns flowOf(UserSession(UserJourneyState.BROWSING, null, false, 0))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads trips and updates state`() = runTest {
        // Given
        val trips = listOf(
            TripPath(
                id = "1", title = "Test Trip", description = "Desc", imageUrl = "",
                category = RouteCategory.CAPITAL, difficulty = Difficulty.NORMAL,
                totalRideTimeMinutes = 0, durationDays = 1
            )
        )
        coEvery { repository.getAvailableTrips() } returns flowOf(trips)

        // When
        viewModel = HomeViewModel(
            repository, addPassportStampUseCase, hapticManager,
            preferenceManager, logisticsProfileManager, assetCacheManager, affiliateManager
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.groupedPaths.values.flatten().size)
    }
}
