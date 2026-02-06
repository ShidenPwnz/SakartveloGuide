package com.example.sakartveloguide.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.sakartveloguide.presentation.theme.SakartveloRed

@Composable
fun ErrorBoundary(
    content: @Composable () -> Unit
) {
    var errorOccurred by remember { mutableStateOf<Throwable?>(null) }

    if (errorOccurred != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "CONNECTION INTERRUPTED",
                    color = SakartveloRed,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "The journey engine encountered a localized anomaly. Your progress is safe.",
                    color = Color.White.copy(0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = { errorOccurred = null },
                    colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("RE-INITIALIZE")
                }
            }
        }
    } else {
        // In a real production app, you'd wrap this in a Try-Catch
        // using a custom CompositionLocal or a parent Layout.
        content()
    }
}