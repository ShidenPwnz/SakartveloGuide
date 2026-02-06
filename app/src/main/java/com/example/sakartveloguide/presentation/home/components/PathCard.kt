package com.example.sakartveloguide.presentation.home.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sakartveloguide.R
import com.example.sakartveloguide.domain.model.TripPath
import com.example.sakartveloguide.presentation.theme.SakartveloRed
import kotlinx.coroutines.delay

@Composable
fun PathCard(
    trip: TripPath,
    languageCode: String,
    onCardClick: (String) -> Unit,
    onHideTutorial: () -> Unit
) {
    val scrollState = rememberScrollState()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isUserActive = isPressed || scrollState.isScrollInProgress

    LaunchedEffect(isUserActive) {
        if (!isUserActive) {
            delay(3000)
            while (true) {
                if (scrollState.value < scrollState.maxValue) {
                    scrollState.animateScrollTo(
                        value = scrollState.maxValue,
                        animationSpec = tween(durationMillis = (scrollState.maxValue - scrollState.value) * 50, easing = LinearEasing)
                    )
                }
                delay(2000)
                scrollState.animateScrollTo(value = 0, animationSpec = tween(durationMillis = 2500, easing = FastOutSlowInEasing))
                delay(4000)
            }
        }
    }

    Card(
        onClick = { onCardClick(trip.id) },
        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        interactionSource = interactionSource
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(trip.imageUrl).crossfade(true).build(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(0.8f), Color.Transparent, Color.Black.copy(0.95f)))))

            Text(
                text = trip.title.get(languageCode).uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 2.sp,
                modifier = Modifier.align(Alignment.TopStart).padding(24.dp).padding(top = 10.dp)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(24.dp)
                    .animateContentSize()
            ) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .verticalScroll(scrollState)
                ) {
                    Text(
                        text = trip.description.get(languageCode),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 22.sp
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.cta_initialize_journey), // ARCHITECT'S FIX: Localized
                    color = SakartveloRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                )
            }
        }
    }
}