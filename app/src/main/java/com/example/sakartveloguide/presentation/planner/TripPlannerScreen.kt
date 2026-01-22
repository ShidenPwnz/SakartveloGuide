package com.example.sakartveloguide.presentation.planner

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sakartveloguide.data.local.entity.LocationEntity
import com.example.sakartveloguide.domain.model.GeoPoint
import com.example.sakartveloguide.presentation.planner.components.*
import com.example.sakartveloguide.presentation.theme.SakartveloRed
import androidx.compose.ui.graphics.Brush as GradientBrush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripPlannerScreen(
    viewModel: AdventureViewModel,
    onBack: () -> Unit,
    onNavigateToFobMap: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    // Capture local variable for Smart Casting
    val currentBaseLoc = state.baseLocation

    // Single Expansion State for Edit Mode
    var expandedEditId by remember { mutableStateOf<Int?>(null) }

    // 1. LOCK BACK BUTTON IN LIVE MODE
    BackHandler(enabled = state.mode == TripMode.LIVE) { }

    // 2. ROBUST EXIT STRATEGY
    fun handleBack() {
        if (state.mode == TripMode.EDITING) {
            viewModel.onBackCleanup()
            onBack()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(state.title.uppercase(), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    // Hide Back button visually in Live Mode
                    if (state.mode == TripMode.EDITING) {
                        IconButton(onClick = { handleBack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        }
                    }
                },
                actions = {
                    if (state.mode == TripMode.EDITING) {
                        IconButton(onClick = { viewModel.optimizeRoute() }) { Icon(Icons.Default.AutoFixHigh, null, tint = SakartveloRed) }
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp) {
                Button(
                    onClick = { viewModel.toggleMode() },
                    modifier = Modifier.fillMaxWidth().padding(24.dp).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (state.mode == TripMode.EDITING) SakartveloRed else Color(0xFF333333))
                ) {
                    Text(if (state.mode == TripMode.EDITING) "START JOURNEY" else "PAUSE & EDIT", fontWeight = FontWeight.Black)
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 100.dp)) {

                // 1. LOGISTICS HEADER
                item {
                    AnimatedVisibility(visible = state.mode == TripMode.EDITING) {
                        LogisticsHeader(
                            hasBase = currentBaseLoc != null,
                            hasFlights = !state.profile.needsFlight,
                            onBaseSetup = onNavigateToFobMap,
                            onBaseLink = { viewModel.onStayAction(it) },
                            onFlightAction = { viewModel.onFlightAction(it) },
                            onTransportAction = { viewModel.onTransportAction(it) }
                        )
                    }
                }

                // 2. SANDWICH: START HOME
                if (currentBaseLoc != null) {
                    item {
                        // -1 = Start
                        val isActive = state.activeNodeId == -1
                        val isExpanded = if (state.mode == TripMode.LIVE) isActive else expandedEditId == -1

                        ItineraryCard(
                            node = createDisplayHomeNode(currentBaseLoc, -1, "HOME (START)"),
                            distFromPrev = null,
                            mode = state.mode,
                            isActive = isActive,
                            isExpanded = isExpanded,
                            isCompleted = true,
                            onMapClick = {},
                            onTaxiClick = {},
                            onCheckIn = {},
                            onRemove = {},
                            onCardClick = {
                                if (state.mode == TripMode.LIVE) viewModel.onCardClicked(-1)
                                else expandedEditId = if(expandedEditId == -1) null else -1
                            }
                        )
                    }
                }

                // 3. THE ROUTE
                itemsIndexed(state.route) { index, node ->
                    val isActive = state.activeNodeId == node.id
                    val isExpanded = if (state.mode == TripMode.LIVE) isActive else expandedEditId == node.id

                    // ARCHITECT'S FIX: Removed redundant Box wrapper, logic now inside onCardClick
                    ItineraryCard(
                        node = node,
                        distFromPrev = state.distances[node.id],
                        mode = state.mode,
                        isActive = isActive,
                        isExpanded = isExpanded,
                        isCompleted = state.completedIds.contains(node.id),
                        onMapClick = { viewModel.launchNavigation(GeoPoint(node.latitude, node.longitude), "driving") },
                        onTaxiClick = { viewModel.onTransportAction("bolt") },
                        onCheckIn = { viewModel.markCheckIn(node.id) },
                        onRemove = { viewModel.removeStop(node.id) },
                        onCardClick = {
                            if (state.mode == TripMode.LIVE) viewModel.onCardClicked(node.id)
                            else expandedEditId = if(expandedEditId == node.id) null else node.id
                        }
                    )
                }

                // 4. ADD BUTTON
                if (state.mode == TripMode.EDITING && !state.isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(24.dp), Alignment.Center) {
                            IconButton(onClick = { showAddSheet = true }, modifier = Modifier.size(56.dp).background(SakartveloRed, CircleShape)) { Icon(Icons.Default.Add, null, tint = Color.White) }
                        }
                    }
                }

                // 5. RETURN HOME
                if (currentBaseLoc != null && state.route.isNotEmpty()) {
                    item {
                        // -2 = End
                        val isActive = state.activeNodeId == -2
                        val isExpanded = if (state.mode == TripMode.LIVE) isActive else expandedEditId == -2

                        ItineraryCard(
                            node = createDisplayHomeNode(currentBaseLoc, -2, "RETURN HOME"),
                            distFromPrev = state.distances[-2],
                            mode = state.mode,
                            isActive = isActive,
                            isExpanded = isExpanded,
                            isCompleted = false,
                            onMapClick = { viewModel.launchNavigation(currentBaseLoc, "driving") },
                            onTaxiClick = { viewModel.onTransportAction("bolt") },
                            onCheckIn = { viewModel.toggleMode() }, // Finish Trip
                            onRemove = {},
                            onCardClick = {
                                if (state.mode == TripMode.LIVE) viewModel.onCardClicked(-2)
                                else expandedEditId = if(expandedEditId == -2) null else -2
                            }
                        )
                    }
                }

                // EMPTY STATE
                if (state.route.isEmpty() && currentBaseLoc == null && !state.isLoading) {
                    item {
                        Text("Set Home Location or Add a Stop to begin.", modifier = Modifier.fillMaxWidth().padding(32.dp), color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        if (showAddSheet) {
            ModalBottomSheet(onDismissRequest = { showAddSheet = false }, containerColor = MaterialTheme.colorScheme.surface) {
                AddStopSheet(
                    query = state.searchQuery,
                    nearbyRecs = state.nearbyRecs,
                    regionalRecs = state.regionalRecs,
                    results = state.searchResults,
                    activeCat = state.activeCategory,
                    onQueryChange = { viewModel.onSearchQuery(it) },
                    onCategorySelect = { viewModel.onCategorySelect(it) },
                    onAdd = { viewModel.addStop(it); showAddSheet = false }
                )
            }
        }
    }
}

// Helper for UI-only Home Node creation
fun createDisplayHomeNode(loc: GeoPoint, id: Int, title: String): LocationEntity {
    return LocationEntity(
        id = id, type = "HOME", region = "HQ", latitude = loc.latitude, longitude = loc.longitude,
        imageUrl = "https://images.pexels.com/photos/271624/pexels-photo-271624.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
        nameEn = title, nameKa = title, nameRu = title, nameTr = title, nameHy = title, nameIw = title, nameAr = title,
        descEn = "Your secured base of operations.", descKa = "", descRu = "", descTr = "", descHy = "", descIw = "", descAr = ""
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStopSheet(
    query: String,
    nearbyRecs: List<LocationEntity>,
    regionalRecs: List<LocationEntity>,
    results: List<LocationEntity>,
    activeCat: String?,
    onQueryChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onAdd: (LocationEntity) -> Unit
) {
    val categories = listOf("Nature", "History", "Caves", "Relax", "Hiking")
    var detailNode by remember { mutableStateOf<LocationEntity?>(null) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var isSearchFocused by remember { mutableStateOf(false) }

    if (detailNode != null) {
        AlertDialog(
            onDismissRequest = { detailNode = null },
            confirmButton = { Button(onClick = { onAdd(detailNode!!); detailNode = null }, colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed)) { Text("ADD TO TRIP") } },
            dismissButton = { TextButton(onClick = { detailNode = null }) { Text("CLOSE") } },
            title = { Text(detailNode!!.nameEn) },
            text = { Column { AsyncImage(model = detailNode!!.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop); Spacer(Modifier.height(16.dp)); Text(detailNode!!.descEn, style = MaterialTheme.typography.bodyMedium) } }
        )
    }

    Column(modifier = Modifier.padding(bottom = 24.dp).fillMaxHeight(0.9f)) {

        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { isSearchFocused = it.isFocused },
                placeholder = { Text("Search places or regions...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (!isSearchFocused && query.isEmpty()) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            isSearchFocused = true
                        }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { cat ->
                val isSelected = activeCat == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelect(cat) },
                    label = { Text(cat) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SakartveloRed, selectedLabelColor = Color.White),
                    border = FilterChipDefaults.filterChipBorder(enabled = true, selected = isSelected, borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        if (query.isEmpty() && !isSearchFocused) {

            if (nearbyRecs.isNotEmpty()) {
                Text("NEARBY GEMS", modifier = Modifier.padding(horizontal = 24.dp), fontWeight = FontWeight.Black, color = SakartveloRed, fontSize = 12.sp)
                Spacer(Modifier.height(12.dp))
                LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(nearbyRecs) { loc -> RecommendationCard(loc, onInfo = { detailNode = loc }, onAdd = { onAdd(loc) }) }
                }
                Spacer(Modifier.height(24.dp))
            }

            if (regionalRecs.isNotEmpty()) {
                Text("REGIONAL HIGHLIGHTS", modifier = Modifier.padding(horizontal = 24.dp), fontWeight = FontWeight.Black, color = SakartveloRed, fontSize = 12.sp)
                Spacer(Modifier.height(12.dp))
                LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(regionalRecs) { loc -> RecommendationCard(loc, onInfo = { detailNode = loc }, onAdd = { onAdd(loc) }) }
                }
            }

        } else {
            val headerText = if (query.isEmpty()) "RECOMMENDED FOR YOU" else "SEARCH RESULTS"
            Text(headerText, modifier = Modifier.padding(horizontal = 24.dp), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface.copy(0.5f), fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            LazyColumn(contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(results) { location -> DiscoveryCard(location) { onAdd(location) } }
            }
        }
    }
}

@Composable
fun DiscoveryCard(location: LocationEntity, onAdd: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f), shape = RoundedCornerShape(12.dp), onClick = { isExpanded = !isExpanded }, modifier = Modifier.animateContentSize()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(model = location.imageUrl, contentDescription = null, modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) { Text(location.nameEn, fontWeight = FontWeight.Bold, maxLines = 1); Text(location.region.uppercase(), fontSize = 10.sp, color = SakartveloRed) }
                IconButton(onClick = onAdd) { Icon(Icons.Default.AddCircle, null, tint = SakartveloRed) }
            }
            if (isExpanded) { Spacer(Modifier.height(8.dp)); Text(text = location.descEn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.8f)) }
        }
    }
}

@Composable
fun RecommendationCard(location: LocationEntity, onInfo: () -> Unit, onAdd: () -> Unit) {
    Card(modifier = Modifier.size(160.dp, 200.dp), shape = RoundedCornerShape(16.dp), onClick = onInfo) {
        Box {
            AsyncImage(model = location.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(GradientBrush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.8f)))))
            Column(Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                Text(location.region.uppercase(), color = SakartveloRed, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Text(location.nameEn, color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp, maxLines = 2, lineHeight = 18.sp)
            }
            IconButton(onClick = onAdd, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) { Icon(Icons.Default.AddCircle, null, tint = Color.White) }
        }
    }
}