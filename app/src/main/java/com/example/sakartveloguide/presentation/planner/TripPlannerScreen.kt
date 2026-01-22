package com.example.sakartveloguide.presentation.planner

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakartveloguide.data.local.entity.LocationEntity
import com.example.sakartveloguide.presentation.planner.components.*
import com.example.sakartveloguide.presentation.theme.SakartveloRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripPlannerScreen(
    viewModel: AdventureViewModel,
    onBack: () -> Unit,
    onNavigateToFobMap: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 1. STATE TRAP: Prevent exiting Live Mode via Back Button
    BackHandler(enabled = state.mode == TripMode.LIVE) {
        // Optional: Show snackbar "Pause Journey to exit"
        // For now, simply consume the event so nothing happens
    }

    // 2. Normal Back Handler for Edit Mode
    fun handleBack() {
        if (state.mode == TripMode.EDITING) {
            viewModel.onBackCleanup()
            onBack()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(state.title.uppercase(), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    // Hide Back button visually in Live Mode to reinforce "Locked" state
                    if (state.mode == TripMode.EDITING) {
                        IconButton(onClick = { handleBack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        }
                    }
                },
                actions = {
                    if (state.mode == TripMode.EDITING) {
                        IconButton(onClick = { viewModel.optimizeRoute() }) {
                            Icon(Icons.Default.AutoFixHigh, null, tint = SakartveloRed)
                        }
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

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // 1. LOGISTICS HEADER (Edit Mode Only)
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

                // 2. LOADING STATE
                if (state.isLoading && state.route.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = SakartveloRed)
                        }
                    }
                }
                // 3. ITINERARY LIST
                else {
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
                            // 3. TACTICAL OVERRIDE: Click sets active target
                            onCardClick = { viewModel.onCardClicked(node.id) }
                        )
                    }
                }

                // 4. ADD BUTTON
                if (state.mode == TripMode.EDITING && !state.isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                            IconButton(
                                onClick = { showAddSheet = true },
                                modifier = Modifier.size(56.dp).background(SakartveloRed, CircleShape)
                            ) {
                                Icon(Icons.Default.Add, null, tint = Color.White)
                            }
                        }
                    }
                }
            }
        }

        if (showAddSheet) {
            ModalBottomSheet(onDismissRequest = { showAddSheet = false }) {
                AddStopSheet(
                    query = state.searchQuery,
                    results = state.searchResults,
                    onQueryChange = { viewModel.onSearchQuery(it) },
                    onAdd = {
                        viewModel.addStop(it)
                        showAddSheet = false
                    }
                )
            }
        }
    }
}

@Composable
fun AddStopSheet(
    query: String,
    results: List<LocationEntity>,
    onQueryChange: (String) -> Unit,
    onAdd: (LocationEntity) -> Unit
) {
    Column(modifier = Modifier.padding(24.dp).fillMaxHeight(0.8f)) {
        Text("ENRICH YOUR JOURNEY", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search places or regions...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(results) { location ->
                Surface(
                    onClick = { onAdd(location) },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    ListItem(
                        headlineContent = { Text(location.nameEn, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(location.region.uppercase(), fontSize = 10.sp, color = SakartveloRed) },
                        trailingContent = { Icon(Icons.Default.AddCircle, null, tint = SakartveloRed) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }
}