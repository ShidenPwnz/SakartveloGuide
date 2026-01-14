package com.example.sakartveloguide.presentation.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // ARCHITECT'S FIX: Added missing import
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import com.example.sakartveloguide.presentation.home.components.PathCard
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onPathClick: (String) -> Unit,
    onPaywallClick: () -> Unit,
    onPassportClick: () -> Unit
) {
    val state: HomeUiState by viewModel.uiState.collectAsState()
    val categories: List<Category> = state.groupedPaths.keys.toList()

    if (categories.isNotEmpty()) {
        val hPagerState = rememberPagerState(pageCount = { categories.size })

        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

            HeaderSection(
                title = categories[hPagerState.currentPage].name,
                onPassportClick = onPassportClick
            )

            HorizontalPager(
                state = hPagerState,
                modifier = Modifier.weight(1f),
                pageSpacing = 16.dp,
                contentPadding = PaddingValues(horizontal = 25.dp)
            ) { hPage ->
                val category = categories[hPage]
                val paths = state.groupedPaths[category] ?: emptyList()
                val vPagerState = rememberPagerState(pageCount = { paths.size })

                VerticalPager(
                    state = vPagerState,
                    modifier = Modifier.fillMaxSize(),
                    pageSpacing = (-350).dp
                ) { vPage ->
                    val path = paths[vPage]
                    val pageOffset = (vPagerState.currentPage - vPage).toFloat() + vPagerState.currentPageOffsetFraction

                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(520.dp)
                        .zIndex(1f - pageOffset.absoluteValue)
                        .graphicsLayer {
                            val scale = lerp(0.85f, 1f, 1f - pageOffset.absoluteValue.coerceIn(0f, 1f))
                            scaleX = scale
                            scaleY = scale
                        }
                    ) {
                        PathCard(
                            trip = path,
                            showButton = false,
                            onCardClick = { onPathClick(path.id) },
                            onLockClick = {},
                            onPaywallClick = onPaywallClick
                        )
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
private fun HeaderSection(title: String, onPassportClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "DISCOVER",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        IconButton(
            onClick = onPassportClick,
            modifier = Modifier.background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = CircleShape
            )
        ) {
            Icon(
                imageVector = Icons.Default.Badge,
                contentDescription = "Passport",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}