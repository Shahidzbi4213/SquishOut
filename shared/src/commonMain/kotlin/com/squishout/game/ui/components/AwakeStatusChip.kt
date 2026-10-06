package com.squishout.game.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Floating frosted status chip displaying how many jellies currently have
 * an open path to the board boundary and are awake & ready to squish out.
 */
@Composable
fun AwakeStatusChip(
    awakeCount: Int,
    modifier: Modifier = Modifier
) {
    val chipShape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .shadow(2.dp, chipShape)
            .clip(chipShape)
            .background(Color(0xFFE8F5E9).copy(alpha = 0.90f))
            .border(1.dp, Color(0xFFC8E6C9), chipShape)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = awakeCount,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "AwakeStatusText"
        ) { count ->
            val text = when {
                count <= 0 -> "✨ All Jellies Sleeping..."
                count == 1 -> "✨ 1 Jelly Awake & Ready!"
                else -> "✨ $count Jellies Awake & Ready!"
            }

            val textColor = if (count > 0) Color(0xFF1B5E20) else Color(0xFF546E7A)

            Text(
                text = text,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
