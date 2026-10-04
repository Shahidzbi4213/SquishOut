package com.squishout.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.squishout.game.theme.SquishColors
import com.squishout.game.theme.SquishTypography

@Composable
fun BoosterDock(
    undoCount: Int,
    hintCount: Int,
    wandCount: Int,
    onUndo: () -> Unit,
    onHint: () -> Unit,
    onWand: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(12.dp, RoundedCornerShape(36.dp))
            .clip(RoundedCornerShape(36.dp))
            .background(Color.White.copy(alpha = 0.92f))
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BoosterButton(
                icon = "↩",
                badge = undoCount,
                badgeColor = SquishColors.KiwiDark,
                onClick = onUndo
            )
            BoosterButton(
                icon = "💡",
                badge = hintCount,
                badgeColor = SquishColors.Strawberry,
                onClick = onHint
            )
            BoosterButton(
                icon = "🪄",
                badge = wandCount,
                badgeColor = SquishColors.LemonDark,
                onClick = onWand
            )
        }
    }
}

@Composable
private fun BoosterButton(
    icon: String,
    badge: Int,
    badgeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(54.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(SquishColors.BackgroundMint)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            fontSize = 24.sp,
            color = SquishColors.TextPrimary
        )

        // Badge counter
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 2.dp, y = (-2).dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$badge",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
