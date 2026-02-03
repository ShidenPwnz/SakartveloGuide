package com.example.sakartveloguide.presentation.planner

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
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
import com.example.sakartveloguide.domain.util.*
import com.example.sakartveloguide.presentation.planner.components.*
import com.example.sakartveloguide.presentation.passport.components.PassportSlamOverlay
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
    val state by viewModel.uiState.collectAsState()
    val session by viewModel.userSession.collectAsState(initial = UserSession())
    val currentLang = session.language.ifEmpty { "en" }
    val listState = rememberLazyListState()

    var showAddSheet by remember { mutableStateOf(false) }
    var expandedEditId by remember { mutableStateOf<Int?>(null) }
    var hasPerformedInitialFocus by remember { mutableStateOf(false) }
    var activeTargetCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    LaunchedEffect(state.bootcampStep) {
        activeTargetCoords = null
        if (state.bootcampStep != BootcampStep.NONE) {
            val targetIdx = when (state.bootcampStep) {
                BootcampStep.ESSENTIALS -> 0
                BootcampStep.SET_HOME -> 1
                BootcampStep.ADD_LOCATION -> if (state.route.isEmpty()) 2 else state.route.size + 1
                BootcampStep.START -> 99
                BootcampStep.LIVE_NAV -> 1
                BootcampStep.CHECK_IN -> 1
                BootcampStep.FINISH -> state.route.size + 1
                else -> null
            }
            targetIdx?.let {
                listState.animateScrollToItem(index = it.coerceAtMost(state.route.size + 5), scrollOffset = -250)
            }
        }
    }

    LaunchedEffect(state.mode) {
        if (state.mode == TripMode.LIVE && !hasPerformedInitialFocus) {
            listState.animateScrollToItem(0)
            hasPerformedInitialFocus = true
        } else if (state.mode == TripMode.EDITING) {
            hasPerformedInitialFocus = false
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest { route ->
            if (route == "passport") onNavigateToPassport()
        }
    }

    BackHandler(enabled = state.mode == TripMode.LIVE) { }

    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = state.route.firstOrNull()?.imageUrl ?: "https://images.pexels.com/photos/459225/pexels-photo-459225.jpeg",
            contentDescription = null,
            modifier = Modifier.fillMaxSize().blur(30.dp),
            contentScale = ContentScale.Crop
        )
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
                Surface(
                    tonalElevation = 8.dp,
                    color = Color.Black.copy(alpha = 0.4f),
                    modifier = Modifier.onGloballyPositioned {
                        if (state.bootcampStep == BootcampStep.START) activeTargetCoords = it
                    }
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(24.dp).height(56.dp)) {
                        Button(
                            onClick = {
                                viewModel.toggleMode()
                            },
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
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 120.dp)) {

                    if (state.mode == TripMode.EDITING) {
                        item {
                            Box(modifier = Modifier.onGloballyPositioned {
                                if (state.bootcampStep == BootcampStep.ESSENTIALS) activeTargetCoords = it
                            }) {
                                LogisticsHeader(
                                    imageUrl = "https://images.pexels.com/photos/459225/pexels-photo-459225.jpeg",
                                    hasBase = state.baseLocation != null,
                                    hasFlights = !state.profile.needsFlight,
                                    onBaseLink = { viewModel.onStayAction(it); if(state.bootcampStep == BootcampStep.ESSENTIALS) viewModel.nextTutorialStep() },
                                    onFlightAction = { viewModel.onFlightAction(it); if(state.bootcampStep == BootcampStep.ESSENTIALS) viewModel.nextTutorialStep() },
                                    onTransportAction = { viewModel.onTransportAction(it); if(state.bootcampStep == BootcampStep.ESSENTIALS) viewModel.nextTutorialStep() },
                                    onRentAction = { viewModel.onRentCarAction(); if(state.bootcampStep == BootcampStep.ESSENTIALS) viewModel.nextTutorialStep() }
                                )
                            }
                        }
                    }

                    item {
                        val node = createSyntheticNode(state.baseLocation ?: GeoPoint(41.7125, 44.7930), -1, stringResource(R.string.home_start_title), stringResource(R.string.home_desc))
                        ItineraryCard(
                            node = node, lang = currentLang, distFromPrev = null, mode = state.mode,
                            isActive = (state.activeNodeId == -1),
                            isExpanded = if (state.mode == TripMode.LIVE) (state.activeNodeId == -1) else (expandedEditId == -1),
                            isCompleted = (state.baseLocation != null), isSmall = true,
                            onMapClick = { viewModel.launchNavigation(state.baseLocation ?: GeoPoint(41.7125, 44.7930), "driving") },
                            onTaxiClick = { viewModel.onTransportAction("bolt") }, onRentClick = { viewModel.onRentCarAction() },
                            onMoreInfo = { viewModel.launchRecon(node, currentLang) }, onCheckIn = { viewModel.markCheckIn(-1) }, onRemove = {},
                            onCardClick = {
                                if (state.bootcampStep == BootcampStep.SET_HOME) viewModel.nextTutorialStep()
                                if (state.mode == TripMode.LIVE) viewModel.onCardClicked(-1)
                                else if (state.baseLocation == null) onNavigateToFobMap()
                                else expandedEditId = if(expandedEditId == -1) null else -1
                            },
                            modifier = Modifier.onGloballyPositioned {
                                if (state.bootcampStep == BootcampStep.SET_HOME) activeTargetCoords = it
                            }
                        )
                    }

                    itemsIndexed(state.route) { index, node ->
                        val isActive = (state.activeNodeId == node.id)
                        ItineraryCard(
                            node = node, lang = currentLang, distFromPrev = state.distances[node.id], mode = state.mode,
                            isActive = isActive,
                            isExpanded = if (state.bootcampStep == BootcampStep.CHECK_IN || state.bootcampStep == BootcampStep.LIVE_NAV) true
                            else if (state.mode == TripMode.LIVE) isActive
                            else (expandedEditId == node.id),
                            isCompleted = state.completedIds.contains(node.id),
                            onMapClick = { viewModel.launchNavigation(GeoPoint(node.latitude, node.longitude), "driving") },
                            onTaxiClick = { viewModel.onTransportAction("bolt") }, onRentClick = { viewModel.onRentCarAction() },
                            onMoreInfo = { viewModel.launchRecon(node, currentLang) }, onCheckIn = { viewModel.markCheckIn(node.id) },
                            onRemove = { viewModel.removeStop(node.id) },
                            onCardClick = {
                                if (state.mode == TripMode.LIVE) viewModel.onCardClicked(node.id)
                                else expandedEditId = if(expandedEditId == -1) null else node.id
                            },
                            navModifier = Modifier.onGloballyPositioned {
                                if (index == 0 && state.bootcampStep == BootcampStep.LIVE_NAV) activeTargetCoords = it
                            },
                            actionButtonModifier = Modifier.onGloballyPositioned {
                                if (index == 0 && state.bootcampStep == BootcampStep.CHECK_IN) activeTargetCoords = it
                            }
                        )
                    }

                    if (state.mode == TripMode.EDITING) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(24.dp)
                                    .onGloballyPositioned { if (state.bootcampStep == BootcampStep.ADD_LOCATION) activeTargetCoords = it },
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(onClick = {
                                    if(state.bootcampStep == BootcampStep.ADD_LOCATION) viewModel.nextTutorialStep()
                                    else showAddSheet = true
                                }, modifier = Modifier.size(56.dp).background(SakartveloRed, CircleShape)) {
                                    Icon(Icons.Default.Add, null, tint = Color.White)
                                }
                            }
                        }
                    }

                    if (state.route.isNotEmpty()) {
                        item {
                            val isActive = (state.activeNodeId == -2)
                            val node = createSyntheticNode(state.baseLocation ?: GeoPoint(41.7125, 44.7930), -2, stringResource(R.string.home_return_title), stringResource(R.string.home_desc))
                            ItineraryCard(
                                node = node, lang = currentLang, distFromPrev = state.distances[-2], mode = state.mode,
                                isActive = isActive,
                                isExpanded = if (state.bootcampStep == BootcampStep.FINISH) true else if (state.mode == TripMode.LIVE) isActive else (expandedEditId == -2),
                                isCompleted = false,
                                onMapClick = { viewModel.launchNavigation(state.baseLocation ?: GeoPoint(41.7125, 44.7930), "driving") },
                                onTaxiClick = { viewModel.onTransportAction("bolt") }, onRentClick = { viewModel.onRentCarAction() },
                                onMoreInfo = { viewModel.launchRecon(node, currentLang) }, onCheckIn = {
                                    if(state.bootcampStep == BootcampStep.FINISH) viewModel.dismissTutorial()
                                    viewModel.completeMission()
                                }, onRemove = {},
                                onCardClick = {
                                    if (state.mode == TripMode.LIVE) viewModel.onCardClicked(-2)
                                    else expandedEditId = if(expandedEditId == -2) null else -2
                                },
                                actionButtonModifier = Modifier.onGloballyPositioned {
                                    if (state.bootcampStep == BootcampStep.FINISH) activeTargetCoords = it
                                }
                            )
                        }
                    }
                }
            }

            if (showAddSheet) {
                ModalBottomSheet(onDismissRequest = { showAddSheet = false }, containerColor = Color(0xFF121212)) {
                    AddStopSheet(
                        query = state.searchQuery, lang = currentLang, nearby = state.nearbyRecs, results = state.searchResults,
                        bootcampStep = state.bootcampStep, onQuery = { viewModel.onSearchQuery(it) }, onAdd = { location -> viewModel.addStop(location); showAddSheet = false }, onCaptureCoords = { }
                    )
                }
            }
        }

        if (state.showSlamAnimation) {
            PassportSlamOverlay(regionName = state.title, onAnimationFinished = { viewModel.onSlamAnimationComplete() })
        }

        if (state.bootcampStep != BootcampStep.NONE) {
            val isInteractive = when(state.bootcampStep) {
                BootcampStep.ESSENTIALS, BootcampStep.ADD_LOCATION -> false
                else -> true
            }
            BootcampSpotlight(
                step = state.bootcampStep,
                targetCoords = activeTargetCoords,
                isInteractive = isInteractive,
                onNext = { viewModel.nextTutorialStep() },
                onDismiss = { viewModel.dismissTutorial() }
            )
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
fun AddStopSheet(
    query: String, lang: String, nearby: List<LocationEntity>, results: List<LocationEntity>,
    bootcampStep: BootcampStep, onQuery: (String) -> Unit, onAdd: (LocationEntity) -> Unit, onCaptureCoords: (LayoutCoordinates) -> Unit
) {
    var detailNode by remember { mutableStateOf<LocationEntity?>(null) }
    var isExplMode by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest { interaction ->
            if (interaction is PressInteraction.Release) {
                if (!isExplMode) { isExplMode = true } else { focusRequester.requestFocus() }
            }
        }
    }

    if (detailNode != null) {
        AlertDialog(
            onDismissRequest = { detailNode = null },
            confirmButton = { Button(onClick = { onAdd(detailNode!!); detailNode = null }, colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed)) { Text("ADD TO TRIP") } },
            dismissButton = { TextButton(onClick = { detailNode = null }) { Text("CLOSE") } },
            title = { Text(detailNode!!.getDisplayName(lang), fontWeight = FontWeight.Black) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    AsyncImage(model = detailNode!!.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                    Spacer(Modifier.height(16.dp))
                    Text(detailNode!!.getDisplayDesc(lang), style = MaterialTheme.typography.bodyMedium)
                }
            }
        )
    }

    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxHeight(0.85f)) {
        OutlinedTextField(
            value = query, onValueChange = { onQuery(it) }, modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            placeholder = { Text("Search places...") },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = SakartveloRed) },
            singleLine = true, interactionSource = interactionSource
        )
        Spacer(Modifier.height(24.dp))
        if (!isExplMode) {
            Text("NEARBY GEMS", fontWeight = FontWeight.Black, color = SakartveloRed, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                itemsIndexed(nearby) { index, item ->
                    RecommendationCard(item, lang, { detailNode = item }, { onAdd(item) })
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(results) { DiscoveryCard(it, lang, { detailNode = it }, { onAdd(it) }) }
            }
        }
    }
}

@Composable
fun RecommendationCard(location: LocationEntity, lang: String, onInfo: () -> Unit, onAdd: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.size(160.dp, 220.dp).clickable { onInfo() }, shape = RoundedCornerShape(16.dp)) {
        Box {
            AsyncImage(model = location.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.8f)))))
            Column(Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                if (location.isLandmark) {
                    Text("MUST SEE", color = Color(0xFFFFD700), fontSize = 9.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(4.dp))
                }
                Text(location.getDisplayName(lang), color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp, maxLines = 2)
            }
            IconButton(onClick = onAdd, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) { Icon(Icons.Default.AddCircle, null, tint = Color.White) }
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