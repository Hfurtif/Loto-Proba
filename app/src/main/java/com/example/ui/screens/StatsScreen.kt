package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SevereCold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LotoDraw
import com.example.stats.ChiSquareResult
import com.example.stats.NumberStat
import com.example.stats.PairStat
import com.example.stats.TrioStat
import com.example.ui.components.BallVariant
import com.example.ui.components.FrequencyBarChart
import com.example.ui.components.LotoBall
import com.example.ui.components.NumberTimelineChart

@Composable
fun StatsScreen(
    stats: List<NumberStat>,
    draws: List<LotoDraw>,
    topPairs: List<PairStat>,
    topTrios: List<TrioStat>,
    chiSquareResult: ChiSquareResult,
    selectedNumberForCurve: Int,
    onSelectNumberForCurve: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Graphiques & Chi², 1: Chads & Froids, 2: Paires & Trios, 3: Tous (0..90)

    val topHot = remember(stats) { stats.sortedByDescending { it.count }.take(5) }
    val topCold = remember(stats) { stats.sortedBy { it.count }.take(5) }
    val topGaps = remember(stats) { stats.sortedByDescending { it.currentGap }.take(5) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Graphiques & χ²", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Chauds & Retards", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Paires & Trios", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Tableau (0-90)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedTab) {
            0 -> GraphsAndChiSquareTab(
                stats = stats,
                draws = draws,
                chiSquareResult = chiSquareResult,
                selectedNumberForCurve = selectedNumberForCurve,
                onSelectNumberForCurve = onSelectNumberForCurve
            )
            1 -> HotColdTab(
                topHot = topHot,
                topCold = topCold,
                topGaps = topGaps,
                onSelectNumber = {
                    onSelectNumberForCurve(it)
                    selectedTab = 0
                }
            )
            2 -> PairsAndTriosTab(
                topPairs = topPairs,
                topTrios = topTrios
            )
            3 -> AllNumbersTab(
                stats = stats,
                onSelectNumber = {
                    onSelectNumberForCurve(it)
                    selectedTab = 0
                }
            )
        }
    }
}

@Composable
private fun GraphsAndChiSquareTab(
    stats: List<NumberStat>,
    draws: List<LotoDraw>,
    chiSquareResult: ChiSquareResult,
    selectedNumberForCurve: Int,
    onSelectNumberForCurve: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. CARTE DU TEST DU KHI-CARRÉ (Étape 4)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (chiSquareResult.isCompatibleWithRandom) Color(0xFFEFF6FF) else Color(0xFFFEF3C7)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Functions,
                                contentDescription = null,
                                tint = if (chiSquareResult.isCompatibleWithRandom) Color(0xFF2563EB) else Color(0xFFD97706)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Test d'équité du Chi-carré (χ²)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (chiSquareResult.isCompatibleWithRandom) Color(0xFFDBEAFE) else Color(0xFFFDE68A))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (chiSquareResult.isCompatibleWithRandom) "Hasard confirmé" else "Écart détecté",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (chiSquareResult.isCompatibleWithRandom) Color(0xFF1D4ED8) else Color(0xFF92400E)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = chiSquareResult.plainFrenchExplanation,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Valeur χ² : ${"%.2f".format(chiSquareResult.chiSquareValue)} (seuil : ${chiSquareResult.criticalValue005})",
                            fontSize = 11.sp,
                            color = Color(0xFF475569)
                        )
                        Text(
                            text = "Degrés de liberté : 90 | p ≈ ${"%.3f".format(chiSquareResult.pValueApprox)}",
                            fontSize = 11.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }
        }

        // 2. DIAGRAMME EN BARRES DES FRÉQUENCES
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoGraph, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Graphique des fréquences de sortie",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = "Touchez une barre pour inspecter le numéro.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FrequencyBarChart(
                        stats = stats,
                        totalDraws = draws.size,
                        selectedNumber = selectedNumberForCurve,
                        onSelectNumber = onSelectNumberForCurve
                    )
                }
            }
        }

        // 3. COURBE DE L'HISTORIQUE DU NUMÉRO SÉLECTIONNÉ
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Courbe de l'historique chronologique",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Évolution des sorties dans le temps et écarts pour le numéro sélectionné.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val selectedStat = stats.find { it.number == selectedNumberForCurve }
                    NumberTimelineChart(
                        number = selectedNumberForCurve,
                        stats = selectedStat,
                        draws = draws,
                        onNumberChange = onSelectNumberForCurve
                    )
                }
            }
        }
    }
}

@Composable
private fun HotColdTab(
    topHot: List<NumberStat>,
    topCold: List<NumberStat>,
    topGaps: List<NumberStat>,
    onSelectNumber: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Numéros les plus sortis (Chauds)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Top 5 Numéros Chauds (Les plus sortis)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF991B1B)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        topHot.forEach { item ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { onSelectNumber(item.number) }
                            ) {
                                LotoBall(number = item.number, variant = BallVariant.HOT, size = 42.dp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${item.count} sorties", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF991B1B))
                                Text("${"%.1f".format(item.frequencyPercent)}%", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        // Numéros les moins sortis (Froids)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SevereCold, contentDescription = null, tint = Color(0xFF0284C7))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Top 5 Numéros Froids (Les moins sortis)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0369A1)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        topCold.forEach { item ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { onSelectNumber(item.number) }
                            ) {
                                LotoBall(number = item.number, variant = BallVariant.COLD, size = 42.dp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${item.count} sorties", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF0369A1))
                                Text("${"%.1f".format(item.frequencyPercent)}%", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        // Écarts actuels (Retards)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFD97706))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Plus Grands Écarts Actuels (Retards)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF92400E)
                        )
                    }
                    Text(
                        text = "Nombre de tirages consécutifs sans apparition du numéro.",
                        fontSize = 11.sp,
                        color = Color(0xFF78350F)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        topGaps.forEach { item ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { onSelectNumber(item.number) }
                            ) {
                                LotoBall(number = item.number, variant = BallVariant.GOLD, size = 42.dp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${item.currentGap} tirages", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF92400E))
                                Text("Max: ${item.maxGap}", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PairsAndTriosTab(
    topPairs: List<PairStat>,
    topTrios: List<TrioStat>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Paires les plus fréquentes (Sorties simultanées)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        items(topPairs) { pair ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LotoBall(number = pair.n1, size = 36.dp)
                        LotoBall(number = pair.n2, size = 36.dp)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${pair.count} fois ensemble",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${"%.1f".format(pair.frequencyPercent)} % des tirages",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Trios les plus fréquents (3 numéros ensemble)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        items(topTrios) { trio ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LotoBall(number = trio.n1, size = 34.dp)
                        LotoBall(number = trio.n2, size = 34.dp)
                        LotoBall(number = trio.n3, size = 34.dp)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${trio.count} fois",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${"%.1f".format(trio.frequencyPercent)} %",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AllNumbersTab(
    stats: List<NumberStat>,
    onSelectNumber: (Int) -> Unit
) {
    var sortMode by remember { mutableIntStateOf(0) } // 0: Par numéro, 1: Par sorties DESC, 2: Par écart DESC

    val sorted = remember(stats, sortMode) {
        when (sortMode) {
            0 -> stats.sortedBy { it.number }
            1 -> stats.sortedByDescending { it.count }
            2 -> stats.sortedByDescending { it.currentGap }
            else -> stats
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = sortMode == 0,
                onClick = { sortMode = 0 },
                label = { Text("Numéro (0..90)", fontSize = 11.sp) }
            )
            FilterChip(
                selected = sortMode == 1,
                onClick = { sortMode = 1 },
                label = { Text("Fréquence ↓", fontSize = 11.sp) }
            )
            FilterChip(
                selected = sortMode == 2,
                onClick = { sortMode = 2 },
                label = { Text("Écart actuel ↓", fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(sorted) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectNumber(item.number) },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LotoBall(number = item.number, size = 36.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Numéro ${item.number.toString().padStart(2, '0')}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Récents (30j) : ${item.recentCount}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${item.count} sorties (${"%.1f".format(item.frequencyPercent)} %)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Écart actuel : ${item.currentGap} (Max: ${item.maxGap})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
