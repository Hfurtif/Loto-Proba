package com.lotoproba.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lotoproba.app.stats.GeneratedCombination
import com.lotoproba.app.ui.components.AppIcons
import com.lotoproba.app.ui.components.BallVariant
import com.lotoproba.app.ui.components.LotoBall
import com.lotoproba.app.ui.components.ScientificDisclaimerCard

@Composable
fun CombinationsScreen(
    combinations: List<GeneratedCombination>,
    weightRecent: Float,
    weightGlobal: Float,
    weightGap: Float,
    onWeightsChanged: (recent: Float, global: Float, gap: Float) -> Unit,
    onSaveForTracking: () -> Unit,
    modifier: Modifier = Modifier
) {
    var localRecent by remember(weightRecent) { mutableFloatStateOf(weightRecent) }
    var localGlobal by remember(weightGlobal) { mutableFloatStateOf(weightGlobal) }
    var localGap by remember(weightGap) { mutableFloatStateOf(weightGap) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "scientific_disclaimer") {
            ScientificDisclaimerCard()
        }

        item(key = "weights_sliders_card") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(AppIcons.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pondérations descriptives des tirages passés",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = "Ajustez le poids relatif de chaque critère observé dans l'historique.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Fréquence observée récente (30 derniers tirages) : ${localRecent.toInt()} %",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = localRecent,
                        onValueChange = {
                            localRecent = it
                            onWeightsChanged(localRecent, localGlobal, localGap)
                        },
                        valueRange = 0f..100f
                    )

                    Text(
                        text = "Fréquence observée globale (Tout l'historique) : ${localGlobal.toInt()} %",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = localGlobal,
                        onValueChange = {
                            localGlobal = it
                            onWeightsChanged(localRecent, localGlobal, localGap)
                        },
                        valueRange = 0f..100f
                    )

                    Text(
                        text = "Écart actuel (Retard observé) : ${localGap.toInt()} %",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = localGap,
                        onValueChange = {
                            localGap = it
                            onWeightsChanged(localRecent, localGlobal, localGap)
                        },
                        valueRange = 0f..100f
                    )
                }
            }
        }

        item(key = "save_combinations_button") {
            Button(
                onClick = onSaveForTracking,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(AppIcons.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Archiver ces 5 sélections pour suivi comparatif")
            }
        }

        items(
            items = combinations,
            key = { it.rank }
        ) { combo ->
            CombinationItemCard(combo = combo)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CombinationItemCard(combo: GeneratedCombination) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (combo.rank) {
                                1 -> Color(0xFFF59E0B)
                                2 -> Color(0xFF3B82F6)
                                3 -> Color(0xFF10B981)
                                else -> Color(0xFF64748B)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${combo.rank}${if (combo.rank == 1) "ère" else "e"} sélection",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Indice passé : ${combo.scorePercent} %",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "(Indicateur descriptif)",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                combo.numbers.forEach { num ->
                    LotoBall(
                        number = num,
                        size = 44.dp,
                        variant = if (combo.rank == 1) BallVariant.GOLD else BallVariant.DEFAULT
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(8.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            AppIcons.Casino,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Ratio combinatoire théorique (constant) :",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = combo.trueProbabilityText + " — identique pour toute combinaison de 5 numéros.",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
