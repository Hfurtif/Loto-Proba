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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.lotoproba.app.data.model.ProposedCombination
import com.lotoproba.app.stats.TrackingSummary
import com.lotoproba.app.ui.components.AppIcons
import com.lotoproba.app.ui.components.BallVariant
import com.lotoproba.app.ui.components.LotoBall
import com.lotoproba.app.ui.components.StatsHonestyBanner

@Composable
fun TrackingScreen(
    trackedCombinations: List<ProposedCombination>,
    summary: TrackingSummary,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "tracking_honesty_banner") {
            StatsHonestyBanner()
        }

        item(key = "tracking_summary_card") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(AppIcons.Balance, contentDescription = null, tint = Color(0xFF2563EB))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bilan cumulé vs Hasard pur",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Text(
                            text = "${summary.totalTestedCombinations} sélections évaluées",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Moyenne observée", fontSize = 11.sp, color = Color(0xFF475569))
                            Text(
                                text = "${"%.3f".format(summary.averageMatchesObserved)} correspondance",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Color(0xFF2563EB)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(Color(0xFFCBD5E1))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Espérance théorique", fontSize = 11.sp, color = Color(0xFF475569))
                            Text(
                                text = "0,275 correspondance",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Color(0xFF059669)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Répartition observée vs Théorique :",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val total = summary.totalTestedCombinations.coerceAtLeast(1)
                    val pct0 = (summary.count0.toDouble() / total) * 100.0
                    val pct1 = (summary.count1.toDouble() / total) * 100.0
                    val pct2 = (summary.count2.toDouble() / total) * 100.0
                    val pct3Plus = (summary.count3Plus.toDouble() / total) * 100.0

                    DistributionRow(
                        label = "0 numéro",
                        observedPct = pct0,
                        theoreticalPct = summary.theoreticalPct0,
                        count = summary.count0
                    )
                    DistributionRow(
                        label = "1 numéro",
                        observedPct = pct1,
                        theoreticalPct = summary.theoreticalPct1,
                        count = summary.count1
                    )
                    DistributionRow(
                        label = "2 numéros",
                        observedPct = pct2,
                        theoreticalPct = summary.theoreticalPct2,
                        count = summary.count2
                    )
                    DistributionRow(
                        label = "3+ numéros",
                        observedPct = pct3Plus,
                        theoreticalPct = summary.theoreticalPct3Plus,
                        count = summary.count3Plus
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFDBEAFE))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = summary.conclusionText,
                            fontSize = 12.sp,
                            color = Color(0xFF1E3A8A),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        item(key = "tracking_history_header") {
            Text(
                text = "Historique des comparaisons archivées :",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        if (trackedCombinations.isEmpty()) {
            item(key = "tracking_empty") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                AppIcons.HourglassEmpty,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aucune sélection archivée pour le moment.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        } else {
            items(
                items = trackedCombinations,
                key = { it.id }
            ) { item ->
                TrackedCombinationCard(item = item)
            }
        }
    }
}

@Composable
private fun DistributionRow(
    label: String,
    observedPct: Double,
    theoreticalPct: Double,
    count: Int
) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "$label ($count fois)", fontSize = 11.sp, color = Color(0xFF334155))
            Text(
                text = "Obs : ${"%.1f".format(observedPct)} % | Théo : ${"%.1f".format(theoreticalPct)} %",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF475569)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { (observedPct / 100.0).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Color(0xFF3B82F6),
            trackColor = Color(0xFFE2E8F0)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrackedCombinationCard(item: ProposedCombination) {
    val matchedList = item.getMatchedNumbersList()
    val allNumbers = item.getNumbersList()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Tirage du ${item.targetDateString}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(#${item.rank})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (item.evaluated) {
                    val totalNums = allNumbers.size
                    val isFullMatch = when (totalNums) {
                        1 -> item.matchedCount == 1
                        2 -> item.matchedCount == 2
                        3 -> item.matchedCount == 3
                        else -> item.matchedCount >= 3
                    }
                    val badgeColor = when {
                        isFullMatch -> Color(0xFFD1FAE5)
                        item.matchedCount > 0 -> Color(0xFFDBEAFE)
                        else -> Color(0xFFF1F5F9)
                    }
                    val textColor = when {
                        isFullMatch -> Color(0xFF047857)
                        item.matchedCount > 0 -> Color(0xFF1D4ED8)
                        else -> Color(0xFF64748B)
                    }
                    val statusText = when (totalNums) {
                        1 -> if (item.matchedCount == 1) "NAP 1 observé (1/1)" else "Non sorti"
                        2 -> if (item.matchedCount == 2) "NAP 2 observé (2/2)" else "${item.matchedCount}/2 sorti"
                        3 -> if (item.matchedCount == 3) "NAP 3 observé (3/3)" else "${item.matchedCount}/3 sorti"
                        else -> "${item.matchedCount}/5 observé(s)"
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "En attente du tirage",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allNumbers.forEach { num ->
                    val isMatched = matchedList.contains(num)
                    val variant = when {
                        !item.evaluated -> BallVariant.DEFAULT
                        isMatched -> BallVariant.MATCHED
                        else -> BallVariant.UNMATCHED
                    }
                    LotoBall(
                        number = num,
                        size = 40.dp,
                        variant = variant,
                        isSelected = isMatched
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Indice : ${item.scorePercent}% (${item.weightsSummary})",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.evaluated && matchedList.isNotEmpty()) {
                    Text(
                        text = "Observé(s) : ${matchedList.joinToString(", ")}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }
    }
}
