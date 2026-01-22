package com.example.sakartveloguide.presentation.builder

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sakartveloguide.R
import com.example.sakartveloguide.data.local.entity.LocationEntity
import com.example.sakartveloguide.domain.util.getDisplayName // New Import
import com.example.sakartveloguide.domain.util.getDisplayDesc // New Import
import com.example.sakartveloguide.presentation.theme.SakartveloRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionBuilderScreen(
    viewModel: MissionBuilderViewModel,
    onBack: () -> Unit,
    onProceed: (List<Int>) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.builder_title), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Black) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = MaterialTheme.colorScheme.onBackground) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            if (state.selectedIds.isNotEmpty()) {
                Surface(color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, SakartveloRed.copy(0.3f))) {
                    Row(Modifier.padding(24.dp).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Column {
                            Text(stringResource(R.string.builder_loadout), color = SakartveloRed, style = MaterialTheme.typography.labelSmall)
                            Text("${state.selectedIds.size} PLACES", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
                        }
                        Button(onClick = { onProceed(state.selectedIds.toList()) }, colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed)) {
                            Text(stringResource(R.string.builder_proceed))
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text("REGION", color = SakartveloRed, modifier = Modifier.padding(start = 24.dp, top = 8.dp), style = MaterialTheme.typography.labelSmall)
            LazyRow(contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.regions) { reg ->
                    FilterChip(selected = state.activeRegion == reg, onClick = { viewModel.setRegion(reg) }, label = { Text(reg) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SakartveloRed, labelColor = MaterialTheme.colorScheme.onSurface, selectedLabelColor = Color.White))
                }
            }
            Text("CATEGORY", color = SakartveloRed, modifier = Modifier.padding(start = 24.dp), style = MaterialTheme.typography.labelSmall)
            LazyRow(contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.categories) { cat ->
                    FilterChip(selected = state.activeCategory == cat, onClick = { viewModel.setCategory(cat) }, label = { Text(cat) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant, labelColor = MaterialTheme.colorScheme.onSurface, selectedLabelColor = MaterialTheme.colorScheme.onSurface))
                }
            }
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.inventory) { location ->
                    ExpandableLocationCard(location = location, lang = state.currentLanguage, isSelected = state.selectedIds.contains(location.id), onToggle = { viewModel.toggleLocation(location.id) })
                }
            }
        }
    }
}

@Composable
fun ExpandableLocationCard(location: LocationEntity, lang: String, isSelected: Boolean, onToggle: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    Surface(modifier = Modifier.fillMaxWidth().animateContentSize().clickable { isExpanded = !isExpanded }, color = if (isSelected) SakartveloRed.copy(0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(0.5f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, if (isSelected) SakartveloRed else Color.Transparent)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(location.getDisplayName(lang), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(location.region.uppercase(), color = SakartveloRed, style = MaterialTheme.typography.labelSmall)
                }
                IconButton(onClick = onToggle) { Icon(imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.AddCircle, contentDescription = null, tint = if (isSelected) SakartveloRed else MaterialTheme.colorScheme.onSurface.copy(0.3f), modifier = Modifier.size(32.dp)) }
            }
            if (isExpanded) {
                Spacer(Modifier.height(12.dp)); Text(location.getDisplayDesc(lang), color = MaterialTheme.colorScheme.onSurface.copy(0.7f), style = MaterialTheme.typography.bodySmall, lineHeight = 18.sp); Spacer(Modifier.height(8.dp)); if (location.imageUrl.isNotEmpty()) { AsyncImage(model = location.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop) }
            }
        }
    }
}