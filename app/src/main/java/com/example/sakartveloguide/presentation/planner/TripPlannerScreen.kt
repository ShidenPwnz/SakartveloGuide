package com.example.sakartveloguide.presentation.planner

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sakartveloguide.R
import com.example.sakartveloguide.data.local.entity.LocationEntity
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.domain.util.getDisplayName
import com.example.sakartveloguide.domain.util.getDisplayDesc
import com.example.sakartveloguide.presentation.passport.components.PassportSlamOverlay
import com.example.sakartveloguide.presentation.planner.components.*
import com.example.sakartveloguide.presentation.theme.SakartveloRed
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripPlannerScreen(
    viewModel: AdventureViewModel,
    onBack: () -> Unit,
    onNavigateToFobMap: () -> Unit,
    onNavigateToPassport: () -> Unit
) {
    val state: TripPlannerState by viewModel.uiState.collectAsState()
    val session: UserSession by viewModel.userSession.collectAsState(initial = UserSession())
    val currentLang = session.language.ifEmpty { "en" }
    val context = LocalContext.current

    val persistentBg = state.route.firstOrNull()?.imageUrl ?: "https://images.pexels.com/photos/459225/pexels-photo-459225.jpeg"
    val baseLoc = state.baseLocation
    val homeStartTitle = stringResource(R.string.home_start_title)
    val homeReturnTitle = stringResource(R.string.home_return_title)
    val homeDescription = stringResource(R.string.home_desc)
    val missionEndTitle = stringResource(R.string.approved_protocol)
    val missionEndDesc = "Objectives Secured. Proceed to extraction."

    var showAddSheet by remember { mutableStateOf(false) }
    var expandedEditId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest { route ->
            if (route == "passport") onNavigateToPassport()
        }
    }

    fun launchMoreInfo(location: LocationEntity) {
        val label = Uri.encode(location.nameEn)
        val u = Uri.parse("geo:0,0?q=${location.latitude},${location.longitude}($label)")
        val fallback = Uri.parse("https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}")
        val i = Intent(Intent.ACTION_VIEW, u).apply { setPackage("com.google.android.apps.maps") }
        try { context.startActivity(i) } catch (e: Exception) { context.startActivity(Intent(Intent.ACTION_VIEW, fallback)) }
    }

    BackHandler(enabled = state.mode == TripMode.LIVE) { }

    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(model = persistentBg, contentDescription = null, modifier = Modifier.fillMaxSize().blur(30.dp), contentScale = ContentScale.Crop)
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.75f)))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(state.title.uppercase(), fontWeight = FontWeight.Black, color = Color.White) },
                    navigationIcon = {
                        if (state.mode == TripMode.EDITING) {
                            IconButton(onClick = { viewModel.onBackCleanup(); onBack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = {
                Surface(tonalElevation = 8.dp, color = Color.Black.copy(alpha = 0.4f)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(24.dp).height(56.dp)) {
                        Button(
                            onClick = { viewModel.toggleMode() },
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (state.mode == TripMode.EDITING) SakartveloRed else Color(0xFF333333))
                        ) {
                            val labelId = if (state.mode == TripMode.EDITING) R.string.btn_start_journey else R.string.btn_pause_edit
                            Text(stringResource(labelId), fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 120.dp)) {
                    item {
                        if (state.mode == TripMode.EDITING) {
                            LogisticsHeader(persistentBg, baseLoc != null, !state.profile.needsFlight, onNavigateToFobMap, { viewModel.onStayAction(it) }, { viewModel.onFlightAction(it) }, { viewModel.onTransportAction(it) }, { viewModel.onRentCarAction() })
                        }
                    }

                    if (baseLoc != null) {
                        item {
                            val isActive = state.activeNodeId == -1
                            val isExpanded = if (state.mode == TripMode.LIVE) isActive else (expandedEditId == -1)
                            ItineraryCard(
                                node = createSyntheticNode(baseLoc, -1, homeStartTitle, homeDescription),
                                lang = currentLang,
                                distFromPrev = state.distances[-1],
                                mode = state.mode,
                                isActive = isActive,
                                isExpanded = isExpanded,
                                isCompleted = state.completedIds.contains(-1),
                                onMapClick = { viewModel.launchNavigation(baseLoc, "driving") },
                                onTaxiClick = { viewModel.onTransportAction("bolt") },
                                onRentClick = { viewModel.onRentCarAction() },
                                onMoreInfo = { launchMoreInfo(createSyntheticNode(baseLoc, -1, homeStartTitle, homeDescription)) },
                                onCheckIn = { viewModel.markCheckIn(-1) },
                                onRemove = {},
                                onCardClick = { if (state.mode == TripMode.LIVE) viewModel.onCardClicked(-1) else expandedEditId = if(expandedEditId == -1) null else -1 }
                            )
                        }
                    }

                    itemsIndexed(state.route) { _, node ->
                        val isActive = state.activeNodeId == node.id
                        val isExpanded = if (state.mode == TripMode.LIVE) isActive else (expandedEditId == node.id)
                        ItineraryCard(
                            node = node,
                            lang = currentLang,
                            distFromPrev = state.distances[node.id],
                            mode = state.mode,
                            isActive = isActive,
                            isExpanded = isExpanded,
                            isCompleted = state.completedIds.contains(node.id),
                            onMapClick = { viewModel.launchNavigation(GeoPoint(node.latitude, node.longitude), "driving") },
                            onTaxiClick = { viewModel.onTransportAction("bolt") },
                            onRentClick = { viewModel.onRentCarAction() },
                            onMoreInfo = { launchMoreInfo(node) },
                            onCheckIn = { viewModel.markCheckIn(node.id) },
                            onRemove = { viewModel.removeStop(node.id) },
                            onCardClick = { if (state.mode == TripMode.LIVE) viewModel.onCardClicked(node.id) else expandedEditId = if(expandedEditId == node.id) null else node.id }
                        )
                    }

                    if (state.mode == TripMode.EDITING) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(24.dp), Alignment.Center) {
                                IconButton(onClick = { showAddSheet = true }, modifier = Modifier.size(56.dp).background(SakartveloRed, CircleShape)) { Icon(Icons.Default.Add, null, tint = Color.White) }
                            }
                        }
                    }

                    if (state.route.isNotEmpty()) {
                        if (baseLoc != null) {
                            item {
                                val isActive = state.activeNodeId == -2
                                val isExpanded = if (state.mode == TripMode.LIVE) isActive else (expandedEditId == -2)
                                ItineraryCard(
                                    node = createSyntheticNode(baseLoc, -2, homeReturnTitle, homeDescription),
                                    lang = currentLang, distFromPrev = state.distances[-2], mode = state.mode, isActive = isActive,
                                    isExpanded = isExpanded,
                                    isCompleted = false,
                                    onMapClick = { viewModel.launchNavigation(baseLoc, "driving") },
                                    onTaxiClick = { viewModel.onTransportAction("bolt") },
                                    onRentClick = { viewModel.onRentCarAction() },
                                    onMoreInfo = { launchMoreInfo(createSyntheticNode(baseLoc, -2, homeReturnTitle, homeDescription)) },
                                    onCheckIn = { viewModel.completeMission() }, onRemove = {},
                                    onCardClick = { if (state.mode == TripMode.LIVE) viewModel.onCardClicked(-2) else expandedEditId = if(expandedEditId == -2) null else -2 }
                                )
                            }
                        } else {
                            val lastStopLoc = state.route.last().let { GeoPoint(it.latitude, it.longitude) }
                            item {
                                val isActive = state.activeNodeId == -3
                                ItineraryCard(createSyntheticNode(lastStopLoc, -3, missionEndTitle, missionEndDesc), currentLang, 0.0, state.mode, isActive, true, false, { }, { }, { viewModel.onRentCarAction() }, { launchMoreInfo(createSyntheticNode(lastStopLoc, -3, missionEndTitle, missionEndDesc)) }, { viewModel.completeMission() }, {}, { if (state.mode == TripMode.LIVE) viewModel.onCardClicked(-3) })
                            }
                        }
                    }

                    // --- PHASE 28.1: TACTICAL SPLIT ACTION CARD ---
                    val objectivesCount = state.route.size + (if(baseLoc != null) 1 else 0)
                    if (objectivesCount >= 2) {
                        item {
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .height(80.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                    onClick = { viewModel.launchFullTripIntent() }
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                        Icon(Icons.Default.Map, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.height(4.dp))
                                        Text(stringResource(R.string.btn_preview_trip), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    }
                                }
                                Surface(
                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                    onClick = { viewModel.optimizeRoute() }
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                        Icon(Icons.Default.AutoFixHigh, null, tint = SakartveloRed, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.height(4.dp))
                                        Text(stringResource(R.string.btn_optimize_short), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showAddSheet) {
                ModalBottomSheet(onDismissRequest = { showAddSheet = false }, containerColor = Color(0xFF121212)) {
                    AddStopSheet(state.searchQuery, currentLang, state.nearbyRecs, state.searchResults, { viewModel.onSearchQuery(it) }, { location -> viewModel.addStop(location); showAddSheet = false })
                }
            }
        }

        if (state.showSlamAnimation) {
            PassportSlamOverlay(regionName = state.title, onAnimationFinished = { viewModel.onSlamAnimationComplete() })
        }
    }
}

fun createSyntheticNode(loc: GeoPoint, id: Int, title: String, desc: String) = LocationEntity(
    id = id, type = "HOME", region = "HQ", latitude = loc.latitude, longitude = loc.longitude,
    imageUrl = "https://images.pexels.com/photos/271624/pexels-photo-271624.jpeg",
    nameEn = title, nameKa = title, nameRu = title, nameTr = title, nameHy = title, nameIw = title, nameAr = title,
    descEn = desc, descKa = desc, descRu = desc, descTr = desc, descHy = desc, descIw = desc, descAr = desc
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStopSheet(query: String, lang: String, nearby: List<LocationEntity>, results: List<LocationEntity>, onQuery: (String) -> Unit, onAdd: (LocationEntity) -> Unit) {
    var detailNode by remember { mutableStateOf<LocationEntity?>(null) }
    var isExplMode by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest { interaction ->
            if (interaction is PressInteraction.Release) {
                if (!isExplMode) { isExplMode = true } else { focusRequester.requestFocus() }
            }
        }
    }

    if (detailNode != null) {
        AlertDialog(onDismissRequest = { detailNode = null }, confirmButton = { Button(onClick = { onAdd(detailNode!!); detailNode = null }, colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed)) { Text(stringResource(R.string.btn_add_to_trip)) } }, dismissButton = { TextButton(onClick = { detailNode = null }) { Text(stringResource(R.string.btn_close)) } }, title = { Text(detailNode!!.getDisplayName(lang), fontWeight = FontWeight.Black) }, text = { Column(modifier = Modifier.verticalScroll(rememberScrollState())) { AsyncImage(model = detailNode!!.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop); Spacer(Modifier.height(16.dp)); Text(detailNode!!.getDisplayDesc(lang), style = MaterialTheme.typography.bodyMedium); Spacer(Modifier.height(12.dp)); Button(onClick = { val u = Uri.parse("geo:${detailNode!!.latitude},${detailNode!!.longitude}?q=${Uri.encode(detailNode!!.nameEn)}"); val i = Intent(Intent.ACTION_VIEW, u).apply { setPackage("com.google.android.apps.maps") }; try { context.startActivity(i) } catch (e: Exception) { context.startActivity(Intent(Intent.ACTION_VIEW, u)) } }, modifier = Modifier.fillMaxWidth().height(40.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)) { Text(stringResource(R.string.btn_more_info), fontSize = 12.sp, fontWeight = FontWeight.Bold) } } })
    }

    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxHeight(0.85f)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = query, onValueChange = onQuery, modifier = Modifier.fillMaxWidth().focusRequester(focusRequester), placeholder = { Text(stringResource(R.string.search_hint)) }, leadingIcon = { Icon(Icons.Default.Search, null, tint = SakartveloRed) }, trailingIcon = { if (isExplMode || query.isNotEmpty()) IconButton(onClick = { onQuery(""); isExplMode = false }) { Icon(Icons.Default.Close, null) } }, shape = RoundedCornerShape(12.dp), singleLine = true, interactionSource = interactionSource)
        }
        Spacer(Modifier.height(24.dp))
        if (!isExplMode) {
            Text(stringResource(R.string.nearby_gems), fontWeight = FontWeight.Black, color = SakartveloRed, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) { items(nearby) { RecommendationCard(it, lang, { detailNode = it }, { onAdd(it) }) } }
        } else {
            Text(text = if(query.isEmpty()) stringResource(R.string.recommended_for_you) else stringResource(R.string.search_results), fontWeight = FontWeight.Black, color = Color.Gray, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) { items(results) { DiscoveryCard(it, lang, { detailNode = it }, { onAdd(it) }) } }
        }
    }
}

@Composable
fun DiscoveryCard(location: LocationEntity, lang: String, onInfo: () -> Unit, onAdd: () -> Unit) {
    Surface(onClick = onInfo, color = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = location.imageUrl, contentDescription = null, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(location.getDisplayName(lang), fontWeight = FontWeight.Bold, maxLines = 1, fontSize = 14.sp)
                Text(location.region.uppercase(), fontSize = 9.sp, color = SakartveloRed, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onAdd) { Icon(Icons.Default.AddCircle, null, tint = SakartveloRed) }
        }
    }
}

@Composable
fun RecommendationCard(location: LocationEntity, lang: String, onInfo: () -> Unit, onAdd: () -> Unit) {
    Card(modifier = Modifier.size(160.dp, 220.dp).clickable { onInfo() }, shape = RoundedCornerShape(16.dp)) {
        Box {
            AsyncImage(model = location.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.8f)))))
            Column(Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                Text(location.region.uppercase(), color = SakartveloRed, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Text(location.getDisplayName(lang), color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp, maxLines = 2, lineHeight = 18.sp)
            }
            IconButton(onClick = onAdd, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) { Icon(Icons.Default.AddCircle, null, tint = Color.White) }
        }
    }
}