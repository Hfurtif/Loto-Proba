package com.lotoproba.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import com.lotoproba.app.data.model.LotoDraw
import com.lotoproba.app.stats.ChiSquareResult
import com.lotoproba.app.stats.GapAndColdAnalysis
import com.lotoproba.app.stats.NumberFrequency
import com.lotoproba.app.stats.NumberStat
import com.lotoproba.app.stats.PairStat
import com.lotoproba.app.stats.PairsAndTriosAnalysis
import com.lotoproba.app.stats.TrioStat
import com.lotoproba.app.ui.components.AppIcons
import com.lotoproba.app.ui.components.BallVariant
import com.lotoproba.app.ui.components.FrequencyBarChart
import com.lotoproba.app.ui.components.LotoBall
import com.lotoproba.app.ui.components.NumberTimelineChart
import com.lotoproba.app.ui.components.StatsHonestyBanner

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
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Graphiques & χ², 1: Chauds & Retards, 2: Paires & Trios, 3: 01..90

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
                text = { Text("Graphiques", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Retards", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Paires/Trios", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Tableau", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

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
        item(key = "honesty_banner_graphs") {
            StatsHonestyBanner()
        }

        item(key = "chi_square_card") {
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
                                AppIcons.Functions,
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
                                    .size(48.dp)
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
                            text = "ddl : ${chiSquareResult.degreesOfFreedom} | p ≈ ${"%.3f".format(chiSquareResult.pValueApprox)}",
                            fontSize = 11.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }
        }

        item(key = "bar_chart_card") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(AppIcons.AutoGraph, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Fréquences observées par numéro",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = "Touchez une barre pour inspecter l'historique du numéro.",
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

        item(key = "timeline_chart_card") {
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
                        text = "Évolution des sorties passées et écarts pour le numéro sélectionné.",
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

@OptIn(ExperimentalLayoutApi::class)
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
        item(key = "honesty_banner_hot_cold") {
            StatsHonestyBanner()
        }

        item(key = "gaps_window_controls") {
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
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("recalculate_gaps_cold_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Recalculer les retards et sorties récentes",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listOf(15, 30, 60), key = { it }) { windowOption ->
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

        item(key = "top_hot_card") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(AppIcons.LocalFireDepartment, contentDescription = null, tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Top 5 Numéros les plus fréquents (Passé)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF991B1B)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        topHot.forEach { item ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSelectNumber(item.number) }
                                    .padding(4.dp)
                            ) {
                                LotoBall(number = item.number, variant = BallVariant.HOT, size = 42.dp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${item.count} sorties", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF991B1B))
                                Text("${"%.1f".format(item.frequencyPercent)} %", fontSize = 10.sp, color = Color(0xFF475569))
                            }
                        }
                    }
                }
            }
        }

        item(key = "least_recent_card") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(AppIcons.SevereCold, contentDescription = null, tint = Color(0xFF0284C7))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Moins Sortis Récemment ($effectiveWindow derniers tirages)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0369A1)
                        )
                    }
                    Text(
                        text = "Numéros ayant le moins d'apparitions observées sur la période récente.",
                        fontSize = 11.sp,
                        color = Color(0xFF075985)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (leastRecentItems.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            leastRecentItems.forEach { item ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onSelectNumber(item.number) }
                                        .padding(4.dp)
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
                                        color = Color(0xFF475569)
                                    )
                                }
                            }
                        }
                    } else {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            topCold.forEach { item ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onSelectNumber(item.number) }
                                        .padding(4.dp)
                                ) {
                                    LotoBall(number = item.number, variant = BallVariant.COLD, size = 42.dp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${item.count} sorties", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF0369A1))
                                    Text("${"%.1f".format(item.frequencyPercent)} %", fontSize = 10.sp, color = Color(0xFF475569))
                                }
                            }
                        }
                    }
                }
            }
        }

        item(key = "longest_gaps_card") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(AppIcons.Schedule, contentDescription = null, tint = Color(0xFFD97706))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Plus Grands Retards (Tirages écoulés)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF92400E)
                        )
                    }
                    Text(
                        text = "Nombre de tirages écoulés depuis la dernière apparition observée de chaque numéro.",
                        fontSize = 11.sp,
                        color = Color(0xFF78350F)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (longestGapItems.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            longestGapItems.forEach { item ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onSelectNumber(item.number) }
                                        .padding(4.dp)
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
                                        color = Color(0xFF475569)
                                    )
                                }
                            }
                        }
                    } else {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            topGaps.forEach { item ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onSelectNumber(item.number) }
                                        .padding(4.dp)
                                ) {
                                    LotoBall(number = item.number, variant = BallVariant.GOLD, size = 42.dp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${item.currentGap} tirages", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF92400E))
                                    Text("Max: ${item.maxGap}", fontSize = 10.sp, color = Color(0xFF475569))
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
        item(key = "honesty_banner_pairs_trios") {
            StatsHonestyBanner()
        }

        item(key = "pairs_trios_controls") {
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
                                text = "Analyse de co-occurrence observée",
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
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("recalculate_pairs_trios_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Recalculer les paires et trios",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listOf(5, 10, 20), key = { it }) { limitOption ->
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

        item(key = "pairs_header") {
            Text(
                text = "Paires les plus fréquentes (Fréquences observées)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        if (topPairs.isEmpty()) {
            item(key = "pairs_empty") {
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
            items(
                items = topPairs,
                key = { pair -> "pair_${pair.n1}_${pair.n2}" }
            ) { pair ->
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
                                text = "Fréquence observée : ${"%.1f".format(pair.frequencyPercent)} %",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item(key = "trios_header") {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Trios les plus fréquents (3 numéros observés ensemble)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        if (topTrios.isEmpty()) {
            item(key = "trios_empty") {
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
            items(
                items = topTrios,
                key = { trio -> "trio_${trio.n1}_${trio.n2}_${trio.n3}" }
            ) { trio ->
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
                                text = "Fréquence observée : ${"%.1f".format(trio.frequencyPercent)} %",
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

    val filterModes = remember {
        listOf(
            0 to "Numéros 01–90",
            1 to "Fréquence observée ↓",
            2 to "Écart actuel ↓"
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "honesty_banner_all_numbers") {
            StatsHonestyBanner()
        }

        item(key = "sort_chips_row") {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filterModes, key = { it.first }) { (modeId, label) ->
                    FilterChip(
                        selected = sortMode == modeId,
                        onClick = {
                            sortMode = modeId
                            if (modeId == 0) {
                                onRecalculateFrequencies01To90()
                            }
                        },
                        label = { Text(label, fontSize = 11.sp) }
                    )
                }
            }
        }

        items(
            items = sorted,
            key = { it.number }
        ) { item ->
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
