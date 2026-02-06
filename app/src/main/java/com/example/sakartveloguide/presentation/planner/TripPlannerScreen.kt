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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import kotlinx.coroutines.delay
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
    var activeTargetCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    LaunchedEffect(state.bootcampStep) {
        activeTargetCoords = null
        if (state.bootcampStep != BootcampStep.NONE) {
            val targetIdx = when (state.bootcampStep) {
                BootcampStep.ESSENTIALS -> 0
                BootcampStep.SET_HOME -> 1
                BootcampStep.ADD_LOCATION -> if (state.route.isEmpty()) 2 else state.route.size + 1
                else -> null
            }
            targetIdx?.let { listState.animateScrollToItem(index = it, scrollOffset = -200) }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest { if (it == "passport") onNavigateToPassport() }
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
                    title = { Text(state.title.uppercase(), fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 2.sp) },
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
                    modifier = Modifier.onGloballyPositioned { if (state.bootcampStep == BootcampStep.START) activeTargetCoords = it }
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).height(56.dp)) {
                        Button(
                            onClick = { viewModel.toggleMode() },
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (state.mode == TripMode.EDITING) SakartveloRed else Color(0xFF333333))
                        ) {
                            val label = if (state.mode == TripMode.EDITING) R.string.btn_start_journey else R.string.btn_pause_edit
                            Text(stringResource(label), fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 120.dp)) {

                    if (state.mode == TripMode.EDITING) {
                        item {
                            Box(modifier = Modifier.onGloballyPositioned { if (state.bootcampStep == BootcampStep.ESSENTIALS) activeTargetCoords = it }) {
                                LogisticsHeader(
                                    imageUrl = "https://images.pexels.com/photos/459225/pexels-photo-459225.jpeg",
                                    hasBase = state.baseLocation != null,
                                    hasFlights = !state.profile.needsFlight,
                                    onBaseLink = { viewModel.onStayAction(it) },
                                    onFlightAction = { viewModel.onFlightAction(it) },
                                    onTransportAction = { viewModel.onTransportAction(it) },
                                    onRentAction = { viewModel.onRentCarAction() }
                                )
                            }
                        }

                        item {
                            AnimatedVisibility(visible = state.route.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    UtilityActionButton(stringResource(R.string.btn_optimize_short), Icons.Default.AutoFixHigh, Modifier.weight(1f)) { viewModel.optimizeRoute() }
                                    UtilityActionButton(stringResource(R.string.btn_preview_trip), Icons.Default.Map, Modifier.weight(1f)) { viewModel.launchFullTripIntent() }
                                }
                            }
                        }
                    }

                    item {
                        val hasBase = state.baseLocation != null
                        val homeTitle = if (hasBase) stringResource(R.string.home_start_title) else stringResource(R.string.home_unset_title)
                        val homeDesc = if (hasBase) stringResource(R.string.home_desc) else stringResource(R.string.home_unset_desc)

                        val node = createSyntheticNode(
                            loc = state.baseLocation ?: GeoPoint(41.7125, 44.7930),
                            id = -1,
                            title = homeTitle,
                            desc = homeDesc
                        )

                        ItineraryCard(
                            node = node, lang = currentLang, distFromPrev = null, mode = state.mode,
                            isActive = (state.activeNodeId == -1),
                            isExpanded = if (state.mode == TripMode.LIVE) (state.activeNodeId == -1) else (expandedEditId == -1),
                            isCompleted = hasBase,
                            isSmall = true,
                            onMapClick = { viewModel.launchNavigation(state.baseLocation ?: GeoPoint(41.7125, 44.7930), "driving") },
                            onTaxiClick = { viewModel.onTransportAction("bolt") }, onRentClick = { viewModel.onRentCarAction() },
                            onMoreInfo = { viewModel.launchRecon(node, currentLang) }, onCheckIn = { viewModel.markCheckIn(-1) }, onRemove = {},
                            onCardClick = {
                                if (state.bootcampStep == BootcampStep.SET_HOME) viewModel.nextTutorialStep()
                                if (state.mode == TripMode.LIVE) viewModel.onCardClicked(-1)
                                else if (state.baseLocation == null) onNavigateToFobMap()
                                else expandedEditId = if(expandedEditId == -1) null else -1
                            },
                            modifier = Modifier.onGloballyPositioned { if (state.bootcampStep == BootcampStep.SET_HOME) activeTargetCoords = it }
                        )
                    }

                    itemsIndexed(state.route) { _, node ->
                        val isActive = (state.activeNodeId == node.id)
                        ItineraryCard(
                            node = node, lang = currentLang, distFromPrev = state.distances[node.id], mode = state.mode,
                            isActive = isActive,
                            isExpanded = if (state.mode == TripMode.LIVE) isActive else (expandedEditId == node.id),
                            isCompleted = state.completedIds.contains(node.id),
                            onMapClick = { viewModel.launchNavigation(GeoPoint(node.latitude, node.longitude), "driving") },
                            onTaxiClick = { viewModel.onTransportAction("bolt") }, onRentClick = { viewModel.onRentCarAction() },
                            onMoreInfo = { viewModel.launchRecon(node, currentLang) }, onCheckIn = { viewModel.markCheckIn(node.id) },
                            onRemove = { viewModel.removeStop(node.id) },
                            onCardClick = {
                                if (state.mode == TripMode.LIVE) viewModel.onCardClicked(node.id)
                                else expandedEditId = if(expandedEditId == node.id) null else node.id
                            }
                        )
                    }

                    if (state.mode == TripMode.EDITING) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(24.dp).onGloballyPositioned { if (state.bootcampStep == BootcampStep.ADD_LOCATION) activeTargetCoords = it }, contentAlignment = Alignment.Center) {
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
                            val node = createSyntheticNode(
                                loc = state.baseLocation ?: GeoPoint(41.7125, 44.7930),
                                id = -2,
                                title = stringResource(R.string.home_return_title),
                                desc = stringResource(R.string.home_desc)
                            )
                            ItineraryCard(
                                node = node, lang = currentLang, distFromPrev = state.distances[-2], mode = state.mode,
                                isActive = isActive,
                                isExpanded = if (state.mode == TripMode.LIVE) isActive else (expandedEditId == -2),
                                isCompleted = false,
                                onMapClick = { viewModel.launchNavigation(state.baseLocation ?: GeoPoint(41.7125, 44.7930), "driving") },
                                onTaxiClick = { viewModel.onTransportAction("bolt") }, onRentClick = { viewModel.onRentCarAction() },
                                onMoreInfo = { viewModel.launchRecon(node, currentLang) }, onCheckIn = { viewModel.completeMission() }, onRemove = {},
                                onCardClick = {
                                    if (state.mode == TripMode.LIVE) viewModel.onCardClicked(-2)
                                    else expandedEditId = if(expandedEditId == -2) null else -2
                                }
                            )
                        }
                    }
                }
            }

            if (showAddSheet) {
                ModalBottomSheet(onDismissRequest = { showAddSheet = false }, containerColor = Color(0xFF121212)) {
                    AddStopSheet(
                        query = state.searchQuery, lang = currentLang,
                        nearby = state.nearbyRecs, nearbyFood = state.nearbyFood, results = state.searchResults,
                        onQuery = { viewModel.onSearchQuery(it) },
                        onAdd = { location -> viewModel.addStop(location); showAddSheet = false }
                    )
                }
            }
        }

        if (state.showSlamAnimation) {
            PassportSlamOverlay(regionName = state.title, onAnimationFinished = { viewModel.onSlamAnimationComplete() })
        }

        if (state.bootcampStep != BootcampStep.NONE) {
            BootcampSpotlight(
                step = state.bootcampStep,
                targetCoords = activeTargetCoords,
                isInteractive = (state.bootcampStep != BootcampStep.ESSENTIALS && state.bootcampStep != BootcampStep.ADD_LOCATION),
                onNext = { viewModel.nextTutorialStep() },
                onDismiss = { viewModel.dismissTutorial() }
            )
        }
    }
}

// --- CORE HELPERS ---

@Composable
fun UtilityActionButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

fun createSyntheticNode(loc: GeoPoint, id: Int, title: String, desc: String) = LocationEntity(
    id = id, type = "HOME", region = "HQ", latitude = loc.latitude, longitude = loc.longitude,
    imageUrl = "https://images.pexels.com/photos/271624/pexels-photo-271624.jpeg",
    priority = 1, popularity = 0, isLandmark = true, tags = emptyList(),
    nameEn = title, nameKa = title, nameRu = title, nameTr = title, nameHy = title, nameIw = title, nameAr = title,
    descEn = desc, descKa = desc, descRu = desc, descTr = desc, descHy = desc, descIw = desc, descAr = desc
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStopSheet(
    query: String, lang: String,
    nearby: List<LocationEntity>, nearbyFood: List<LocationEntity>, results: List<LocationEntity>,
    onQuery: (String) -> Unit, onAdd: (LocationEntity) -> Unit
) {
    var detailNode by remember { mutableStateOf<LocationEntity?>(null) }
    var isExplMode by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }

    val gemsScrollState = rememberLazyListState()
    val foodScrollState = rememberLazyListState()

    LaunchedEffect(isExplMode) {
        if (!isExplMode) {
            while (true) {
                if (!gemsScrollState.isScrollInProgress) gemsScrollState.dispatchRawDelta(0.7f)
                if (!foodScrollState.isScrollInProgress) foodScrollState.dispatchRawDelta(-0.7f)
                delay(16)
            }
        }
    }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest { if (it is PressInteraction.Release) { if (!isExplMode) isExplMode = true else focusRequester.requestFocus() } }
    }

    if (detailNode != null) {
        AlertDialog(
            onDismissRequest = { detailNode = null },
            confirmButton = { Button(onClick = { detailNode?.let { onAdd(it) }; detailNode = null }, colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed)) { Text("ADD TO TRIP") } },
            dismissButton = { TextButton(onClick = { detailNode = null }) { Text("CLOSE") } },
            title = { Text(detailNode?.getDisplayName(lang) ?: "", fontWeight = FontWeight.Black) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    AsyncImage(model = detailNode?.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                    Spacer(Modifier.height(16.dp))
                    Text(detailNode?.getDisplayDesc(lang) ?: "", style = MaterialTheme.typography.bodyMedium)
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
            Text("NEARBY GEMS", fontWeight = FontWeight.Black, color = SakartveloRed, style = MaterialTheme.typography.labelMedium, letterSpacing = 2.sp)
            Spacer(Modifier.height(12.dp))
            LazyRow(state = gemsScrollState, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val loop = if (nearby.isNotEmpty()) List(10) { nearby }.flatten() else emptyList()
                items(loop) { item -> RecommendationCard(item, lang, { detailNode = item }, { onAdd(item) }) }
            }
            Spacer(Modifier.height(32.dp))

            Text("LOCAL FLAVORS", fontWeight = FontWeight.Black, color = SakartveloRed, style = MaterialTheme.typography.labelMedium, letterSpacing = 2.sp)
            Spacer(Modifier.height(12.dp))
            LazyRow(state = foodScrollState, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val loopFood = if (nearbyFood.isNotEmpty()) List(10) { nearbyFood }.flatten() else emptyList()
                items(loopFood) { item -> RecommendationCard(item, lang, { detailNode = item }, { onAdd(item) }) }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(results) { DiscoveryCard(it, lang, { detailNode = it }, { onAdd(it) }) }
            }
        }
    }
}

@Composable
fun RecommendationCard(location: LocationEntity, lang: String, onInfo: () -> Unit, onAdd: () -> Unit) {
    Card(modifier = Modifier.size(160.dp, 220.dp).clickable { onInfo() }, shape = RoundedCornerShape(16.dp)) {
        Box {
            AsyncImage(model = location.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.8f)))))
            Column(Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                if (location.isLandmark) { Text("MUST SEE", color = Color(0xFFFFD700), fontSize = 9.sp, fontWeight = FontWeight.Black); Spacer(Modifier.height(4.dp)) }
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