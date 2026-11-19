package com.lotoproba.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

const val STATS_HONESTY_DISCLAIMER =
    "Statistiques sur les tirages passés, à titre informatif. Elles ne permettent pas de prédire les tirages futurs."

@Composable
fun StatsHonestyBanner(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFEF3C7))
            .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("stats_honesty_banner"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Information statistique",
            tint = Color(0xFFB45309),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = STATS_HONESTY_DISCLAIMER,
            color = Color(0xFF78350F),
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun ScientificDisclaimerCard(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFFEF3C7))
            .border(1.5.dp, Color(0xFFF59E0B), RoundedCornerShape(14.dp))
            .padding(14.dp)
            .testTag("scientific_disclaimer_card")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Avertissement statistique",
                tint = Color(0xFFB45309),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "AVERTISSEMENT STATISTIQUE",
                color = Color(0xFF92400E),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = STATS_HONESTY_DISCLAIMER,
            color = Color(0xFF78350F),
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.5.sp,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFFDE68A))
                .padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF92400E),
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Repère mathématique combinatoire : 1 sur 46 504 458 (0,0000022 %), rigoureusement identique pour toute combinaison de 5 numéros distincts parmi 91 (C(91, 5)). Chaque score reflète uniquement les fréquences observées dans l'historique.",
                    color = Color(0xFF78350F),
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
