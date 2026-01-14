package com.example.sakartveloguide.presentation.detail.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sakartveloguide.presentation.theme.SakartveloRed

@Composable
fun ExpandableText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    textAlign: TextAlign,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .clickable { isExpanded = !isExpanded }
            // ARCHITECT'S MOVE: Smoothly push lower items down
            .animateContentSize() 
    ) {
        Text(
            text = text,
            style = style,
            color = color,
            textAlign = textAlign,
            // When not expanded, show 3 lines and add "..."
            maxLines = if (isExpanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis
        )
        
        // Visual Hint if text is long (Optional: "Show More")
        if (!isExpanded && text.length > 100) { // Heuristic to only show for long text
            Text(
                text = "read more",
                style = MaterialTheme.typography.labelSmall,
                color = SakartveloRed.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}