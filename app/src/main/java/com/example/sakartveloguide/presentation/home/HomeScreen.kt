package com.example.sakartveloguide.presentation.home

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
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

    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = "https://images.pexels.com/photos/459225/pexels-photo-459225.jpeg",
            contentDescription = null,
            modifier = Modifier.fillMaxSize().blur(40.dp),
            contentScale = ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.82f)))

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
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = SakartveloRed) }
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
                        contentPadding = PaddingValues(horizontal = 28.dp)
                    ) { hPage ->
                        val category = categories[hPage]
                        val paths = uiState.groupedPaths[category] ?: emptyList()
                        val vPagerState = rememberPagerState(pageCount = { paths.size })

                        LaunchedEffect(vPagerState.currentPage) { viewModel.triggerHapticTick() }

                        // ARCHITECT'S FIX: Structural gap compression
                        val isGuideCategory = category.name == "GUIDE"

                        Box(modifier = Modifier.fillMaxSize()) {
                            VerticalPager(
                                state = vPagerState,
                                modifier = Modifier.fillMaxSize(),
                                // If Guide, use fixed page size to snap them closer together
                                pageSize = if (isGuideCategory) PageSize.Fixed(340.dp) else PageSize.Fill,
                                pageSpacing = if (isGuideCategory) 12.dp else 0.dp,
                                contentPadding = if (isGuideCategory) PaddingValues(vertical = 24.dp) else PaddingValues(top = 12.dp, bottom = 80.dp),
                                beyondViewportPageCount = 3
                            ) { vPage ->
                                val path = paths[vPage]
                                val pageOffset = (vPagerState.currentPage - vPage).toFloat() + vPagerState.currentPageOffsetFraction
                                val absOffset = pageOffset.absoluteValue.coerceIn(0f, 1f)

                                val isGuide = path.category == RouteCategory.GUIDE
                                val heightFactor = if (isGuide) 1f else 0.88f // Guides now fill their restricted 340.dp container

                                Box(modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(heightFactor)
                                    .zIndex(if (vPagerState.currentPage == vPage) 10f else 5f)
                                    .graphicsLayer {
                                        val scale = lerp(0.92f, 1f, 1f - absOffset)
                                        scaleX = scale; scaleY = scale; alpha = lerp(0.6f, 1f, 1f - absOffset)
                                        // Smoother transition for tight stack
                                        translationY = if (isGuideCategory) 0f else pageOffset * 80f
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
private fun AuthGatekeeper(currentLang: String, onSignIn: () -> Unit, onGuestSignIn: () -> Unit, onLangChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(32.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(text = stringResource(R.string.auth_init_title), color = SakartveloRed, letterSpacing = 4.sp, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(16.dp))
        Text(text = stringResource(R.string.auth_welcome), style = MaterialTheme.typography.headlineSmall, color = SnowWhite, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        Spacer(Modifier.height(48.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LanguageChip("EN", "English", currentLang == "en") { onLangChange("en") }
            LanguageChip("GE", "ქართული", currentLang == "ka") { onLangChange("ka") }
            LanguageChip("RU", "Русский", currentLang == "ru") { onLangChange("ru") }
            LanguageChip("TR", "Türkçe", currentLang == "tr") { onLangChange("tr") }
            LanguageChip("HY", "Հայերեն", currentLang == "hy") { onLangChange("hy") }
            LanguageChip("IW", "עברית", currentLang == "iw") { onLangChange("iw") }
            LanguageChip("AR", "العربية", currentLang == "ar") { onLangChange("ar") }
        }
        Spacer(Modifier.height(48.dp))
        Button(onClick = onSignIn, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.AutoMirrored.Filled.Login, null); Spacer(Modifier.width(12.dp)); Text("CONTINUE WITH GOOGLE", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onGuestSignIn, modifier = Modifier.fillMaxWidth().height(60.dp), border = BorderStroke(1.dp, Color.White.copy(0.3f)), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = SnowWhite)) {
            Text("PROCEED AS GUEST", fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun HeaderSection(category: Category, onPassportClick: () -> Unit, onSettingsClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(R.string.discover), style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Black)
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
            Text(text = "MISSION DATA LOST", color = SakartveloRed, fontWeight = FontWeight.Black, fontSize = 24.sp)
            Spacer(Modifier.height(32.dp))
            Button(onClick = onSettingsClick, colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed)) { Text("RE-INITIALIZE ENGINE") }
        }
    }
}