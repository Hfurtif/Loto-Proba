package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BallColdEnd
import com.example.ui.theme.BallColdStart
import com.example.ui.theme.BallGoldEnd
import com.example.ui.theme.BallGoldStart
import com.example.ui.theme.BallHotEnd
import com.example.ui.theme.BallHotStart

enum class BallVariant {
    DEFAULT,
    HOT,
    COLD,
    GOLD,
    MATCHED,
    UNMATCHED
}

@Composable
fun LotoBall(
    number: Int,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    variant: BallVariant = BallVariant.DEFAULT,
    isSelected: Boolean = false
) {
    val (brush, textColor) = when (variant) {
        BallVariant.DEFAULT -> Pair(
            Brush.verticalGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))),
            Color.White
        )
        BallVariant.HOT -> Pair(
            Brush.verticalGradient(listOf(BallHotStart, BallHotEnd)),
            Color.White
        )
        BallVariant.COLD -> Pair(
            Brush.verticalGradient(listOf(BallColdStart, BallColdEnd)),
            Color.White
        )
        BallVariant.GOLD -> Pair(
            Brush.verticalGradient(listOf(BallGoldStart, BallGoldEnd)),
            Color(0xFF1E293B)
        )
        BallVariant.MATCHED -> Pair(
            Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF047857))),
            Color.White
        )
        BallVariant.UNMATCHED -> Pair(
            Brush.verticalGradient(listOf(Color(0xFF64748B), Color(0xFF475569))),
            Color(0xFFE2E8F0)
        )
    }

    val borderWidth = if (isSelected) 3.dp else 1.dp
    val borderColor = if (isSelected) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.4f)

    Box(
        modifier = modifier
            .size(size)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(brush)
            .border(borderWidth, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Reflet brillant supérieur
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(width = size * 0.7f, height = size * 0.35f)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.45f), Color.Transparent)
                    )
                )
        )

        // Numéro centré
        val fontSize = when {
            size >= 52.dp -> 18.sp
            size >= 44.dp -> 15.sp
            size >= 36.dp -> 13.sp
            else -> 11.sp
        }

        Text(
            text = number.toString().padStart(2, '0'),
            color = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
