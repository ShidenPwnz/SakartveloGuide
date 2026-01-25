package com.example.sakartveloguide.presentation.home

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.sakartveloguide.R
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.presentation.home.components.PathCard
import com.example.sakartveloguide.presentation.theme.MatteCharcoal
import com.example.sakartveloguide.presentation.theme.SakartveloRed
import com.example.sakartveloguide.presentation.theme.SnowWhite
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onPathClick: (String) -> Unit,
    onPassportClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val session by viewModel.userSession.collectAsState(initial = UserSession())
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current

    if (currentUser == null) {
        AuthGatekeeper(
            onSignIn = { viewModel.signIn(context) },
            onGuestSignIn = { viewModel.onGuestSignIn() }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {

            // LAYER 1: Global Map Background (Blurred)
            AsyncImage(
                model = "https://images.pexels.com/photos/459225/pexels-photo-459225.jpeg", // Placeholder for Topographic map
                contentDescription = null,
                modifier = Modifier.fillMaxSize().blur(30.dp),
                contentScale = ContentScale.Crop
            )
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)))

            // LAYER 2: Authenticated Content
            val categories = uiState.groupedPaths.keys.toList()

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SakartveloRed)
                }
            } else if (categories.isEmpty()) {
                DataLoadErrorFallback(onSettingsClick)
            } else {
                val hPagerState = rememberPagerState(pageCount = { categories.size })

                Column(modifier = Modifier.fillMaxSize()) {
                    HeaderSection(categories[hPagerState.currentPage], onPassportClick, onSettingsClick)

                    HorizontalPager(
                        state = hPagerState,
                        modifier = Modifier.weight(1f),
                        pageSpacing = 16.dp,
                        contentPadding = PaddingValues(horizontal = 25.dp)
                    ) { hPage ->
                        val category = categories[hPage]
                        val paths = uiState.groupedPaths[category] ?: emptyList()

                        key(session.hasSeenTutorial, session.language, paths.size) {
                            val vPagerState = rememberPagerState(pageCount = { paths.size })

                            LaunchedEffect(vPagerState.currentPage) {
                                viewModel.triggerHapticTick()
                            }

                            VerticalPager(
                                state = vPagerState,
                                modifier = Modifier.fillMaxSize(),
                                pageSpacing = (-350).dp
                            ) { vPage ->
                                val path = paths[vPage]
                                val pageOffset = (vPagerState.currentPage - vPage).toFloat() + vPagerState.currentPageOffsetFraction
                                val absOffset = pageOffset.absoluteValue.coerceIn(0f, 1f)
                                val dynamicZIndex = 1f - absOffset

                                Box(modifier = Modifier
                                    .fillMaxWidth()
                                    .height(520.dp)
                                    .zIndex(dynamicZIndex)
                                    .graphicsLayer {
                                        val scale = lerp(0.85f, 1f, 1f - absOffset)
                                        scaleX = scale
                                        scaleY = scale
                                        alpha = lerp(0.4f, 1f, 1f - absOffset)

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            val blurAmount = absOffset * 30f
                                            if (blurAmount > 0f) {
                                                renderEffect = android.graphics.RenderEffect.createBlurEffect(
                                                    blurAmount, blurAmount, android.graphics.Shader.TileMode.DECAL
                                                ).asComposeRenderEffect()
                                            }
                                        }
                                    }
                                ) {
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
    }
}

@Composable
private fun AuthGatekeeper(onSignIn: () -> Unit, onGuestSignIn: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(MatteCharcoal), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(R.string.auth_init_title), style = MaterialTheme.typography.labelSmall, color = SakartveloRed, letterSpacing = 4.sp)
            Spacer(Modifier.height(16.dp))
            Text(text = stringResource(R.string.auth_welcome), style = MaterialTheme.typography.headlineSmall, color = SnowWhite, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Spacer(Modifier.height(48.dp))
            Button(onClick = onSignIn, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.AutoMirrored.Filled.Login, null)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.auth_google_btn), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onGuestSignIn, modifier = Modifier.fillMaxWidth().height(60.dp), border = BorderStroke(1.dp, Color.White.copy(0.3f)), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = SnowWhite)) {
                Text(stringResource(R.string.auth_guest_btn), fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun HeaderSection(category: Category, onPassportClick: () -> Unit, onSettingsClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(R.string.discover), style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Black, letterSpacing = (-1).sp)
            Row {
                IconButton(onClick = onSettingsClick, modifier = Modifier.background(Color.White.copy(0.1f), CircleShape)) {
                    Icon(Icons.Default.Settings, null, tint = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onPassportClick, modifier = Modifier.background(Color.White.copy(0.1f), CircleShape)) {
                    Icon(Icons.Default.Badge, null, tint = Color.White)
                }
            }
        }
        val categoryEnum = try { RouteCategory.valueOf(category.name) } catch(e: Exception) { RouteCategory.CULTURE }
        Text(text = stringResource(categoryEnum.getLabelRes()), color = SakartveloRed, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
    }
}

@Composable
private fun DataLoadErrorFallback(onSettingsClick: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "MISSION DATA LOST", color = SakartveloRed, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onSettingsClick, colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed)) {
                Text("GO TO SETTINGS")
            }
        }
    }
}