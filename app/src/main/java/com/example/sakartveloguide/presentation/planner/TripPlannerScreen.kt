package com.example.sakartveloguide.presentation.planner

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sakartveloguide.data.local.entity.LocationEntity
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

    BackHandler(enabled = state.mode == TripMode.LIVE) { }

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
                    if (state.mode == TripMode.EDITING) {
                        IconButton(onClick = { handleBack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
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
                            hasBase = state.baseLocation != null,
                            hasFlights = !state.profile.needsFlight,
                            onBaseSetup = onNavigateToFobMap,
                            onBaseLink = { viewModel.openBookingLink() },
                            onFlightAction = { viewModel.openFlightLink() },
                            onTransportAction = { /* Handle taxi */ }
                        )
                    }
                }

                // 2. LOADING OR EMPTY
                if (state.isLoading && state.route.isEmpty()) {
                    item { Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = SakartveloRed) } }
                } else if (state.route.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.AddLocationAlt, null, tint = SakartveloRed.copy(0.5f), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("YOUR ITINERARY IS EMPTY", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(0.5f))
                            Text("Tap + to add your first destination", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(0.3f))
                        }
                    }
                } else {
                    // 3. ITINERARY
                    itemsIndexed(state.route) { index, node ->
                        ItineraryCard(
                            node = node,
                            distFromPrev = state.distances[node.id],
                            mode = state.mode,
                            isActive = state.activeNodeId == node.id,
                            isCompleted = state.completedIds.contains(node.id),
                            onNavigateDrive = { viewModel.launchNavigation(com.example.sakartveloguide.domain.model.GeoPoint(node.latitude, node.longitude), "driving") },
                            onNavigateWalk = { viewModel.launchNavigation(com.example.sakartveloguide.domain.model.GeoPoint(node.latitude, node.longitude), "walking") },
                            onCheckIn = { viewModel.markCheckIn(node.id) },
                            onRemove = { viewModel.removeStop(node.id) },
                            onCardClick = { viewModel.onCardClicked(node.id) }
                        )
                    }
                }

                // 4. ADD BUTTON
                if (state.mode == TripMode.EDITING && !state.isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                            IconButton(onClick = { showAddSheet = true }, modifier = Modifier.size(56.dp).background(SakartveloRed, CircleShape)) { Icon(Icons.Default.Add, null, tint = Color.White) }
                        }
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

    // ARCHITECT'S FIX: Focus State Tracker
    var isSearchFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    if (detailNode != null) {
        AlertDialog(
            onDismissRequest = { detailNode = null },
            confirmButton = {
                Button(onClick = { onAdd(detailNode!!); detailNode = null }, colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed)) {
                    Text("ADD TO TRIP")
                }
            },
            dismissButton = { TextButton(onClick = { detailNode = null }) { Text("CLOSE") } },
            title = { Text(detailNode!!.nameEn) },
            text = {
                Column {
                    AsyncImage(
                        model = detailNode!!.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(detailNode!!.descEn, style = MaterialTheme.typography.bodyMedium)
                }
            }
        )
    }

    Column(modifier = Modifier.padding(bottom = 24.dp).fillMaxHeight(0.9f)) {

        // 1. SEARCH
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .onFocusChanged { isSearchFocused = it.isFocused }, // TRACK FOCUS
            placeholder = { Text("Search places or regions...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        // 2. VIBE CHIPS
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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

        // 3. LOGIC SWITCH
        // Show Carousels ONLY if Search is NOT focused AND Query is Empty
        if (query.isEmpty() && !isSearchFocused) {

            // NEARBY GEMS (0-10 closest)
            if (nearbyRecs.isNotEmpty()) {
                Text("NEARBY GEMS (Proximity)", modifier = Modifier.padding(horizontal = 24.dp), fontWeight = FontWeight.Black, color = SakartveloRed, fontSize = 12.sp)
                Spacer(Modifier.height(12.dp))
                LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(nearbyRecs) { loc ->
                        RecommendationCard(loc, onInfo = { detailNode = loc }, onAdd = { onAdd(loc) })
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // REGIONAL HIGHLIGHTS (11-20 closest)
            if (regionalRecs.isNotEmpty()) {
                Text("REGIONAL HIGHLIGHTS", modifier = Modifier.padding(horizontal = 24.dp), fontWeight = FontWeight.Black, color = SakartveloRed, fontSize = 12.sp)
                Spacer(Modifier.height(12.dp))
                LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(regionalRecs) { loc ->
                        RecommendationCard(loc, onInfo = { detailNode = loc }, onAdd = { onAdd(loc) })
                    }
                }
            }

        } else {
            // 4. VERTICAL SEARCH LIST (Active Search Mode)
            val headerText = if (query.isEmpty()) "RECOMMENDED FOR YOU" else "SEARCH RESULTS"

            Text(headerText, modifier = Modifier.padding(horizontal = 24.dp), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface.copy(0.5f), fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))

            LazyColumn(
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(results) { location ->
                    DiscoveryCard(location) { onAdd(location) }
                }
            }
        }
    }
}

@Composable
fun DiscoveryCard(location: LocationEntity, onAdd: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
        shape = RoundedCornerShape(12.dp),
        onClick = { isExpanded = !isExpanded },
        modifier = Modifier.animateContentSize()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = location.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(location.nameEn, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(location.region.uppercase(), fontSize = 10.sp, color = SakartveloRed)
                }
                IconButton(onClick = onAdd) { Icon(Icons.Default.AddCircle, null, tint = SakartveloRed) }
            }
            if (isExpanded) {
                Spacer(Modifier.height(8.dp))
                Text(text = location.descEn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.8f))
            }
        }
    }
}

@Composable
fun RecommendationCard(location: LocationEntity, onInfo: () -> Unit, onAdd: () -> Unit) {
    Card(
        modifier = Modifier.size(160.dp, 200.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = onInfo
    ) {
        Box {
            AsyncImage(model = location.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(GradientBrush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.8f)))))

            Column(Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                Text(location.region.uppercase(), color = SakartveloRed, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Text(location.nameEn, color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp, maxLines = 2, lineHeight = 18.sp)
            }

            IconButton(
                onClick = onAdd,
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
            ) {
                Icon(Icons.Default.AddCircle, null, tint = Color.White)
            }
        }
    }
}