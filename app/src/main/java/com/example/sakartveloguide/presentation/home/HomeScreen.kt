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
import androidx.compose.runtime.getValue // ARCHITECT'S FIX: Crucial
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sakartveloguide.R
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.presentation.home.components.PathCard
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onPathClick: (String) -> Unit,
    onPassportClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    // ARCHITECT'S FIX: Using explicit collection with lifecycle awareness
    val uiState by viewModel.uiState.collectAsState()
    val session by viewModel.userSession.collectAsState(initial = UserSession())
    val categories = uiState.groupedPaths.keys.toList()

    if (categories.isNotEmpty()) {
        val hPagerState = rememberPagerState(pageCount = { categories.size })
        LaunchedEffect(hPagerState.currentPage) { viewModel.triggerHapticTick() }

        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            HeaderSection(categories[hPagerState.currentPage], onPassportClick, onSettingsClick)

            HorizontalPager(
                state = hPagerState,
                modifier = Modifier.weight(1f),
                pageSpacing = 16.dp,
                contentPadding = PaddingValues(horizontal = 25.dp)
            ) { hPage ->
                val category = categories[hPage]
                val paths = uiState.groupedPaths[category] ?: emptyList()

                key(session.hasSeenTutorial, session.language) {
                    val initialTripIndex = if (category.name == "GUIDE" && session.hasSeenTutorial) 1 else 0
                    val vPagerState = rememberPagerState(initialPage = initialTripIndex, pageCount = { paths.size })

                    LaunchedEffect(vPagerState.currentPage) { viewModel.triggerHapticTick() }

                    VerticalPager(state = vPagerState, modifier = Modifier.fillMaxSize(), pageSpacing = (-350).dp) { vPage ->
                        val path = paths[vPage]
                        val pageOffset = (vPagerState.currentPage - vPage).toFloat() + vPagerState.currentPageOffsetFraction
                        Box(modifier = Modifier.fillMaxWidth().height(520.dp).graphicsLayer {
                            val scale = lerp(0.85f, 1f, 1f - pageOffset.absoluteValue.coerceIn(0f, 1f))
                            scaleX = scale; scaleY = scale
                        }) {
                            PathCard(
                                trip = path,
                                languageCode = session.language,
                                onCardClick = onPathClick,
                                onHideTutorial = { viewModel.onHideTutorialPermanent() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(category: Category, onPassportClick: () -> Unit, onSettingsClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.discover),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f),
                letterSpacing = (-1).sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onSettingsClick, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f), CircleShape)) {
                    Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onPassportClick, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f), CircleShape)) {
                    Icon(Icons.Default.Badge, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        val categoryEnum = try { RouteCategory.valueOf(category.name) } catch(e: Exception) { RouteCategory.CULTURE }
        Text(
            text = stringResource(categoryEnum.getLabelRes()),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}