package com.example.sakartveloguide.presentation.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import com.example.sakartveloguide.domain.model.UserSession
import com.example.sakartveloguide.presentation.home.components.PathCard
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onPathClick: (String) -> Unit,
    onPaywallClick: () -> Unit,
    onPassportClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val state: HomeUiState by viewModel.uiState.collectAsState()
    val session by viewModel.userSession.collectAsState(initial = UserSession())
    val categories = state.groupedPaths.keys.toList()

    if (categories.isNotEmpty()) {
        val hPagerState = rememberPagerState(pageCount = { categories.size })

        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            HeaderSection(categories[hPagerState.currentPage].name, onPassportClick, onSettingsClick)

            HorizontalPager(
                state = hPagerState,
                modifier = Modifier.weight(1f),
                pageSpacing = 16.dp,
                contentPadding = PaddingValues(horizontal = 25.dp)
            ) { hPage ->
                val category = categories[hPage]
                val paths = state.groupedPaths[category] ?: emptyList()

                // ARCHITECT'S FIX: Use the 'key' block to force the pager to re-initialize
                // when the tutorial is dismissed. This solves the "Dismiss does nothing" bug.
                key(session.hasSeenTutorial) {
                    val initialTripIndex = if (category.name == "GUIDE" && session.hasSeenTutorial) 1 else 0
                    val vPagerState = rememberPagerState(
                        initialPage = initialTripIndex,
                        pageCount = { paths.size }
                    )

                    VerticalPager(
                        state = vPagerState,
                        modifier = Modifier.fillMaxSize(),
                        pageSpacing = (-350).dp
                    ) { vPage ->
                        val path = paths[vPage]
                        val pageOffset = (vPagerState.currentPage - vPage).toFloat() + vPagerState.currentPageOffsetFraction

                        Box(modifier = Modifier
                            .fillMaxWidth().height(520.dp).zIndex(1f - pageOffset.absoluteValue)
                            .graphicsLayer {
                                val scale = lerp(0.85f, 1f, 1f - pageOffset.absoluteValue.coerceIn(0f, 1f))
                                scaleX = scale; scaleY = scale
                            }
                        ) {
                            PathCard(
                                trip = path,
                                showButton = true,
                                onCardClick = { onPathClick(path.id) },
                                onLockClick = {},
                                onPaywallClick = onPaywallClick,
                                onHideTutorial = { viewModel.onHideTutorialPermanent() }
                            )
                        }
                    }
                }
            }
        }
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun HeaderSection(title: String, onPassportClick: () -> Unit, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("DISCOVER", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium, letterSpacing = 2.sp)
            Text(title, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onSettingsClick, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)) {
                Icon(Icons.Default.Settings, "Settings", tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onPassportClick, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)) {
                Icon(Icons.Default.Badge, "Passport", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}