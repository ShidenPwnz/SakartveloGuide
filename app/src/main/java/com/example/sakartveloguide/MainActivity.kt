package com.example.sakartveloguide

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.sakartveloguide.domain.model.UserSession
import com.example.sakartveloguide.presentation.home.HomeViewModel
import com.example.sakartveloguide.presentation.navigation.SakartveloNavGraph
import com.example.sakartveloguide.presentation.theme.SakartveloTheme
import com.example.sakartveloguide.presentation.passport.components.PassportSlamOverlay
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { !viewModel.isSplashReady.value }

        setContent {
            val session by viewModel.userSession.collectAsState(initial = UserSession())
            val stampingTrip by viewModel.stampingTrip.collectAsState()

            LaunchedEffect(session.language) {
                if (session.language.isNotEmpty()) {
                    val appLocale = LocaleListCompat.forLanguageTags(session.language)
                    if (AppCompatDelegate.getApplicationLocales() != appLocale) {
                        AppCompatDelegate.setApplicationLocales(appLocale)
                    }
                }
            }

            SakartveloTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    SakartveloNavGraph(
                        homeViewModel = viewModel,
                        // RESOLVED: Lambda correctly calls restored method
                        onCompleteTrip = { trip -> viewModel.onCompleteTrip(trip) }
                    )

                    stampingTrip?.let { trip ->
                        PassportSlamOverlay(
                            trip = trip,
                            onAnimationFinished = { viewModel.onSlamAnimationFinished() }
                        )
                    }
                }
            }
        }
    }
}