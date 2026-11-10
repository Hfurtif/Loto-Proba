package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Avertissement",
                tint = Color(0xFFB45309),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "RAPPEL SCIENTIFIQUE FONDAMENTAL",
                color = Color(0xFF92400E),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "« Les tirages sont indépendants. Le score reflète l'historique, il ne prédit pas le résultat de demain. »",
            color = Color(0xFF78350F),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
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
                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Vraie probabilité mathématique : 1 chance sur 46 504 458 (soit 0,0000022 %), rigoureusement identique pour toute combinaison de 5 numéros distincts parmi 91 (C(91, 5)). Le score est un indicateur descriptif et JAMAIS une probabilité de sortie.",
                    color = Color(0xFF78350F),
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
