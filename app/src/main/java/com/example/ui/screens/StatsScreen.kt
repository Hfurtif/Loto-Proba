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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SevereCold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LotoDraw
import com.example.stats.ChiSquareResult
import com.example.stats.GapAndColdAnalysis
import com.example.stats.NumberStat
import com.example.stats.NumberFrequency
import com.example.stats.PairStat
import com.example.stats.PairsAndTriosAnalysis
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
    frequencies01To90: List<NumberFrequency> = emptyList(),
    onRecalculateFrequencies01To90: () -> Unit = {},
    pairsAndTriosAnalysis: PairsAndTriosAnalysis? = null,
    pairTrioLimit: Int = 10,
    onSelectPairTrioLimit: (Int) -> Unit = {},
    onRecalculatePairsAndTrios: () -> Unit = {},
    gapAndColdAnalysis: GapAndColdAnalysis? = null,
    recentWindowSize: Int = 30,
    onSelectRecentWindowSize: (Int) -> Unit = {},
    onRecalculateGapsAndCold: () -> Unit = {},
    onRecalculateChiSquare: () -> Unit = {},
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
                onSelectNumberForCurve = onSelectNumberForCurve,
                onRecalculateChiSquare = onRecalculateChiSquare
            )
            1 -> HotColdTab(
                topHot = topHot,
                topCold = topCold,
                topGaps = topGaps,
                gapAndColdAnalysis = gapAndColdAnalysis,
                recentWindowSize = recentWindowSize,
                onSelectRecentWindowSize = onSelectRecentWindowSize,
                onRecalculateGapsAndCold = onRecalculateGapsAndCold,
                onSelectNumber = {
                    onSelectNumberForCurve(it)
                    selectedTab = 0
                }
            )
            2 -> PairsAndTriosTab(
                topPairs = pairsAndTriosAnalysis?.topPairs ?: topPairs,
                topTrios = pairsAndTriosAnalysis?.topTrios ?: topTrios,
                analyzedDrawsCount = pairsAndTriosAnalysis?.totalDrawsAnalyzed ?: draws.size,
                selectedLimit = pairTrioLimit,
                onSelectLimit = onSelectPairTrioLimit,
                onRecalculate = onRecalculatePairsAndTrios
            )
            3 -> AllNumbersTab(
                stats = stats,
                frequencies01To90 = frequencies01To90,
                onRecalculateFrequencies01To90 = onRecalculateFrequencies01To90,
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
    onSelectNumberForCurve: (Int) -> Unit,
    onRecalculateChiSquare: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. CARTE DU TEST DU KHI-CARRÉ (Étape 4)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chi_square_card"),
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
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

                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                            IconButton(
                                onClick = onRecalculateChiSquare,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("recalculate_chi_square_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Recalculer le test du Chi-carré",
                                    modifier = Modifier.size(18.dp),
                                    tint = if (chiSquareResult.isCompatibleWithRandom) Color(0xFF2563EB) else Color(0xFFD97706)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = chiSquareResult.plainFrenchExplanation,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1E293B)
                    )

                    if (chiSquareResult.hasSufficientData) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Théorique uniforme : ${"%.1f".format(chiSquareResult.expectedFrequencyPerNumber)} sorties/numéro • Observé : ${chiSquareResult.minObservedFrequency} à ${chiSquareResult.maxObservedFrequency} (${chiSquareResult.totalDrawsAnalyzed} tirages)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

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
                            text = "Degrés de liberté : ${chiSquareResult.degreesOfFreedom} | p ≈ ${"%.3f".format(chiSquareResult.pValueApprox)}",
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
    gapAndColdAnalysis: GapAndColdAnalysis?,
    recentWindowSize: Int,
    onSelectRecentWindowSize: (Int) -> Unit,
    onRecalculateGapsAndCold: () -> Unit,
    onSelectNumber: (Int) -> Unit
) {
    val analyzedDraws = gapAndColdAnalysis?.totalDrawsAnalyzed ?: 0
    val effectiveWindow = gapAndColdAnalysis?.recentWindowSize ?: recentWindowSize
    val leastRecentItems = gapAndColdAnalysis?.leastRecentlyDrawn?.take(5).orEmpty()
    val longestGapItems = gapAndColdAnalysis?.longestGaps?.take(5).orEmpty()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Contrôle de la fenêtre récente & recalcul des retards
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Analyse des retards & sorties récentes",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Sur $analyzedDraws tirage(s) • Fenêtre récente : $effectiveWindow tirages",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = onRecalculateGapsAndCold,
                            modifier = Modifier.testTag("recalculate_gaps_cold_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Recalculer les retards et sorties récentes",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15, 30, 60).forEach { windowOption ->
                            FilterChip(
                                selected = recentWindowSize == windowOption,
                                onClick = { onSelectRecentWindowSize(windowOption) },
                                label = { Text("$windowOption tirages", fontSize = 11.sp) },
                                modifier = Modifier.testTag("recent_window_${windowOption}_chip")
                            )
                        }
                    }
                }
            }
        }

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

        // Numéros les moins sortis récemment
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
                            text = "Moins Sortis Récemment ($effectiveWindow derniers tirages)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0369A1)
                        )
                    }
                    Text(
                        text = "Numéros ayant le moins d'apparitions sur la période récente.",
                        fontSize = 11.sp,
                        color = Color(0xFF075985)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (leastRecentItems.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            leastRecentItems.forEach { item ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable { onSelectNumber(item.number) }
                                ) {
                                    LotoBall(number = item.number, variant = BallVariant.COLD, size = 42.dp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${item.recentAppearances} récent(s)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF0369A1)
                                    )
                                    Text(
                                        text = "Retard: ${item.drawsSinceLastAppearance}",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    } else {
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
                            text = "Plus Grands Retards (Tirages écoulés)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF92400E)
                        )
                    }
                    Text(
                        text = "Nombre de tirages écoulés depuis la dernière apparition de chaque numéro.",
                        fontSize = 11.sp,
                        color = Color(0xFF78350F)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (longestGapItems.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            longestGapItems.forEach { item ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable { onSelectNumber(item.number) }
                                ) {
                                    LotoBall(number = item.number, variant = BallVariant.GOLD, size = 42.dp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${item.drawsSinceLastAppearance} tirages",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF92400E)
                                    )
                                    Text(
                                        text = if (item.neverAppeared) "Jamais sorti" else "Max: ${item.maxGap}",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    } else {
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
}

@Composable
private fun PairsAndTriosTab(
    topPairs: List<PairStat>,
    topTrios: List<TrioStat>,
    analyzedDrawsCount: Int,
    selectedLimit: Int,
    onSelectLimit: (Int) -> Unit,
    onRecalculate: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Analyse de co-occurrence",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Analysé sur $analyzedDrawsCount tirage(s) historique(s)",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = onRecalculate,
                            modifier = Modifier.testTag("recalculate_pairs_trios_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Recalculer les paires et trios",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 20).forEach { limitOption ->
                            FilterChip(
                                selected = selectedLimit == limitOption,
                                onClick = { onSelectLimit(limitOption) },
                                label = { Text("Top $limitOption", fontSize = 11.sp) },
                                modifier = Modifier.testTag("pair_trio_limit_${limitOption}_chip")
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Paires les plus fréquentes (Sorties simultanées)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        if (topPairs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = "Aucune paire détectée pour le moment. Ajoutez ou importez des tirages pour lancer l'analyse.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        } else {
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
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Trios les plus fréquents (3 numéros ensemble)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        if (topTrios.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = "Aucun trio fréquent disponible pour cet historique.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        } else {
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
}

@Composable
private fun AllNumbersTab(
    stats: List<NumberStat>,
    frequencies01To90: List<NumberFrequency>,
    onRecalculateFrequencies01To90: () -> Unit,
    onSelectNumber: (Int) -> Unit
) {
    var sortMode by remember { mutableIntStateOf(0) } // 0: 01..90, 1: Par sorties DESC, 2: Par écart DESC

    val frequencyLookup = remember(frequencies01To90) {
        frequencies01To90.associateBy { it.number }
    }

    val sorted = remember(stats, sortMode) {
        when (sortMode) {
            0 -> stats.filter { it.number in 1..90 }.sortedBy { it.number }
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
                onClick = {
                    sortMode = 0
                    onRecalculateFrequencies01To90()
                },
                label = { Text("Numéros 01–90", fontSize = 11.sp) }
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
                val freqInfo = frequencyLookup[item.number]
                val displayCount = freqInfo?.count ?: item.count
                val displayPercent = freqInfo?.frequencyPercent ?: item.frequencyPercent
                val displayLabel = freqInfo?.formattedNumber ?: item.number.toString().padStart(2, '0')
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
                                    text = "Numéro $displayLabel",
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
                                text = "$displayCount sorties (${"%.1f".format(displayPercent)} %)",
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
