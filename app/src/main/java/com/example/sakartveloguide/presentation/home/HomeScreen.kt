package com.example.sakartveloguide.presentation.home

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import com.example.sakartveloguide.presentation.settings.LanguageChip
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

    Box(modifier = Modifier.fillMaxSize()) {
        // Shared Background
        AsyncImage(
            model = "https://images.pexels.com/photos/459225/pexels-photo-459225.jpeg",
            contentDescription = null,
            modifier = Modifier.fillMaxSize().blur(30.dp),
            contentScale = ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)))

        if (currentUser == null) {
            AuthGatekeeper(
                currentLang = session.language,
                onSignIn = { viewModel.signIn(context) },
                onGuestSignIn = { viewModel.onGuestSignIn() },
                onLangChange = { viewModel.onLanguageChange(it) }
            )
        } else {
            val categories = uiState.groupedPaths.keys.toList()

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = SakartveloRed)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "INITIALIZING ADVENTURE ENGINE...",
                            color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            } else if (categories.isEmpty()) {
                DataLoadErrorFallback(onSettingsClick)
            } else {
                // THE MATRIX (Horizontal Categories)
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

                        // Vertical Stack (Trips)
                        key(session.hasSeenTutorial, session.language, paths.size) {
                            val vPagerState = rememberPagerState(pageCount = { paths.size })

                            LaunchedEffect(vPagerState.currentPage) {
                                viewModel.triggerHapticTick()
                            }

                            Box(modifier = Modifier.fillMaxSize()) {
                                VerticalPager(
                                    state = vPagerState,
                                    modifier = Modifier.fillMaxSize(),
                                    // ARCHITECT'S FIX: Use standard spacing, achieve overlap via TranslationY
                                    pageSpacing = 0.dp,
                                    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                                    beyondViewportPageCount = 3
                                ) { vPage ->
                                    val path = paths[vPage]

                                    // Parallax & Scale Logic
                                    val pageOffset = (vPagerState.currentPage - vPage).toFloat() + vPagerState.currentPageOffsetFraction
                                    val absOffset = pageOffset.absoluteValue.coerceIn(0f, 1f)
                                    val dynamicZIndex = if (vPagerState.currentPage == vPage) 10f else 5f - absOffset

                                    Box(modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(0.85f) // Taller cards
                                        .zIndex(dynamicZIndex)
                                        .graphicsLayer {
                                            val scale = lerp(0.90f, 1f, 1f - absOffset)
                                            scaleX = scale
                                            scaleY = scale
                                            alpha = lerp(0.5f, 1f, 1f - absOffset)

                                            // ARCHITECT'S FIX: Visual Overlap via Translation
                                            // Pushes non-active cards down/up visually without affecting touch target
                                            translationY = pageOffset * 100f

                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                val blurAmount = absOffset * 20f
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

                                // Bouncing Arrow (Only show if more items below)
                                if (paths.size > 1 && vPagerState.currentPage < paths.size - 1) {
                                    BouncingArrow(
                                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
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

// ... (AuthGatekeeper, HeaderSection, DataLoadErrorFallback, BouncingArrow remain same) ...
@Composable
private fun AuthGatekeeper(currentLang: String, onSignIn: () -> Unit, onGuestSignIn: () -> Unit, onLangChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(32.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(text = stringResource(R.string.auth_init_title), style = MaterialTheme.typography.labelSmall, color = SakartveloRed, letterSpacing = 4.sp)
        Spacer(Modifier.height(16.dp))
        Text(text = stringResource(R.string.auth_welcome), style = MaterialTheme.typography.headlineSmall, color = SnowWhite, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        Spacer(Modifier.height(48.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LanguageChip("EN", "English", currentLang == "en") { onLangChange("en") }
            LanguageChip("GE", "ქართული", currentLang == "ka") { onLangChange("ka") }
            LanguageChip("RU", "Русский", currentLang == "ru") { onLangChange("ru") }
            LanguageChip("AR", "العربية", currentLang == "ar") { onLangChange("ar") }
            LanguageChip("HE", "עברית", currentLang == "iw") { onLangChange("iw") }
        }
        Spacer(Modifier.height(48.dp))
        Button(onClick = onSignIn, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed), shape = RoundedCornerShape(12.dp)) { Icon(Icons.AutoMirrored.Filled.Login, null); Spacer(Modifier.width(12.dp)); Text(stringResource(R.string.auth_google_btn), fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onGuestSignIn, modifier = Modifier.fillMaxWidth().height(60.dp), border = BorderStroke(1.dp, Color.White.copy(0.3f)), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = SnowWhite)) { Text(stringResource(R.string.auth_guest_btn), fontWeight = FontWeight.Medium) }
        Spacer(Modifier.height(24.dp))
        Text(text = stringResource(R.string.auth_footer), color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))
    }
}

@Composable
private fun HeaderSection(category: Category, onPassportClick: () -> Unit, onSettingsClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(R.string.discover), style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Black, letterSpacing = (-1).sp)
            Row {
                IconButton(onClick = onSettingsClick, modifier = Modifier.background(Color.White.copy(0.1f), CircleShape)) { Icon(Icons.Default.Settings, null, tint = Color.White) }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onPassportClick, modifier = Modifier.background(Color.White.copy(0.1f), CircleShape)) { Icon(Icons.Default.Badge, null, tint = Color.White) }
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
            Text(text = "MISSION DATA LOST", color = SakartveloRed, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, fontSize = 24.sp)
            Spacer(Modifier.height(16.dp))
            Text(text = "The Tactical Engine failed to ingest local mission parameters. This usually occurs after a major schema update.", color = Color.White.copy(0.6f), textAlign = TextAlign.Center, fontSize = 12.sp)
            Spacer(Modifier.height(32.dp))
            Button(onClick = onSettingsClick, colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed), shape = RoundedCornerShape(12.dp)) { Text("REINITIALIZE SYSTEM", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun BouncingArrow(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "Bounce")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "OffsetY"
    )

    Icon(
        imageVector = Icons.Default.KeyboardArrowDown,
        contentDescription = null,
        tint = SakartveloRed,
        modifier = modifier.size(32.dp).graphicsLayer { translationY = offsetY }
    )
}