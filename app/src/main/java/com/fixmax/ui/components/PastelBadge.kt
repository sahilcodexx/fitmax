package com.fixmax.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fixmax.ui.theme.*

enum class BadgeStyle {
    PURPLE,
    GREEN,
    PEACH
}

@Composable
fun PastelBadge(
    text: String,
    style: BadgeStyle = BadgeStyle.GREEN,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (style) {
        BadgeStyle.PURPLE -> BadgePurpleBg to BadgePurpleText
        BadgeStyle.GREEN -> BadgeGreenBg to BadgeGreenText
        BadgeStyle.PEACH -> BadgePeachBg to BadgePeachText
    }

    Box(
        modifier = modifier
            .background(color = bg, shape = RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
