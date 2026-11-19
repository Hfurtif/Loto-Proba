package com.lotoproba.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lotoproba.app.stats.DailyNapPackage
import com.lotoproba.app.stats.NapItem
import com.lotoproba.app.stats.UserNumbersAnalysis
import com.lotoproba.app.ui.components.AppIcons
import com.lotoproba.app.ui.components.BallVariant
import com.lotoproba.app.ui.components.LotoBall
import com.lotoproba.app.ui.components.StatsHonestyBanner

@Composable
fun NapScreen(
    napPackage: DailyNapPackage,
    onSaveForTracking: () -> Unit,
    userProposedNumbers: List<Int> = emptyList(),
    userNumbersAnalysis: UserNumbersAnalysis? = null,
    userNumbersAiReport: String? = null,
    isAnalyzingUserNumbersWithAi: Boolean = false,
    onSubmitUserNumbers: (String) -> Boolean = { false },
    onRemoveUserNumber: (Int) -> Unit = {},
    onClearUserNumbers: () -> Unit = {},
    onRequestAiAnalysisForUserNumbers: () -> Unit = {},
    onSaveUserNumbersForTracking: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableIntStateOf(0) } // 0: Synthèse Auto, 1: Mes Chiffres

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedMode,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedMode == 0,
                onClick = { selectedMode = 0 },
                modifier = Modifier.testTag("nap_auto_tab"),
                text = {
                    Text(
                        text = "Synthèse Auto",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
            Tab(
                selected = selectedMode == 1,
                onClick = { selectedMode = 1 },
                modifier = Modifier.testTag("nap_user_numbers_tab"),
                text = {
                    Text(
                        text = "Mes Chiffres",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedMode) {
            0 -> AutoNapProposalsContent(
                napPackage = napPackage,
                onSaveForTracking = onSaveForTracking,
                onSwitchToUserNumbers = { selectedMode = 1 }
            )
            1 -> UserProposedNumbersContent(
                selectedNumbers = userProposedNumbers,
                analysis = userNumbersAnalysis,
                aiReport = userNumbersAiReport,
                isAnalyzingWithAi = isAnalyzingUserNumbersWithAi,
                onSubmitNumbers = onSubmitUserNumbers,
                onRemoveNumber = onRemoveUserNumber,
                onClearNumbers = onClearUserNumbers,
                onRequestAiAnalysis = onRequestAiAnalysisForUserNumbers,
                onSaveForTracking = onSaveUserNumbersForTracking
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AutoNapProposalsContent(
    napPackage: DailyNapPackage,
    onSaveForTracking: () -> Unit,
    onSwitchToUserNumbers: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "nap_honesty_banner") {
            StatsHonestyBanner()
        }

        item(key = "shortcut_user_numbers") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSwitchToUserNumbers() }
                    .testTag("shortcut_to_user_numbers_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = AppIcons.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Inspecter vos propres chiffres ?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Saisissez-les dans « Mes Chiffres » pour consulter leur historique et ratio théorique.",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Analyser →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item(key = "nap_guide_card") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            AppIcons.Verified,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Comprendre les formats NAP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1E293B)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "• NAP 1 (N1) : Suivi d'un seul numéro observé dans l'historique.\n" +
                            "• NAP 2 (N2) : Observation de paires fréquemment réunies dans le passé.\n" +
                            "• NAP 3 (N3) : Observation de trios historiques réunis sur un même tirage.",
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // 1. NAP 1
        item(key = "nap1_card") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF59E0B))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "NAP 1 (N1)",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Repère fréquentiel",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFEF3C7))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Indice passé ${napPackage.mainBankerNap1.confidencePercent}%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF92400E)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val bankerNum = napPackage.mainBankerNap1.numbers.firstOrNull() ?: 0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            LotoBall(
                                number = bankerNum,
                                size = 62.dp,
                                variant = BallVariant.GOLD
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Numéro $bankerNum",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = napPackage.mainBankerNap1.description,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                val clipboard =
                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("NAP 1", bankerNum.toString()))
                                Toast.makeText(context, "Numéro $bankerNum copié.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                AppIcons.ContentCopy,
                                contentDescription = "Copier le numéro $bankerNum",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (napPackage.alternativeNap1.isNotEmpty()) {
                        FlowRow(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Autres repères observés :",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                            napPackage.alternativeNap1.forEach { alt ->
                                LotoBall(number = alt, size = 32.dp, variant = BallVariant.DEFAULT)
                            }
                        }
                    }
                }
            }
        }

        // 2. NAP 2
        item(key = "nap2_header") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF3B82F6))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "NAP 2 (N2)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Paires les plus fréquentes (Historique)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        items(
            items = napPackage.topNap2Pairs,
            key = { pair -> "${pair.code}_${pair.numbers.joinToString("-")}" }
        ) { pair ->
            NapPairCard(pair = pair)
        }

        // 3. NAP 3
        item(key = "nap3_header") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF10B981))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "NAP 3 (N3)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Trios les plus fréquents (Historique)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        items(
            items = napPackage.topNap3Trios,
            key = { trio -> "${trio.code}_${trio.numbers.joinToString("-")}" }
        ) { trio ->
            NapTrioCard(trio = trio)
        }

        // 4. PERM 5
        item(key = "perm5_card") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PERM 5 (Synthèse 5 numéros)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                        Text(
                            text = "Descriptif",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        napPackage.perm5.numbers.forEach { num ->
                            LotoBall(number = num, size = 42.dp, variant = BallVariant.DEFAULT)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = napPackage.perm5.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item(key = "save_auto_nap_footer") {
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onSaveForTracking,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_auto_nap_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(AppIcons.BookmarkAdd, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Archiver cette synthèse pour suivi comparatif",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserProposedNumbersContent(
    selectedNumbers: List<Int>,
    analysis: UserNumbersAnalysis?,
    aiReport: String?,
    isAnalyzingWithAi: Boolean,
    onSubmitNumbers: (String) -> Boolean,
    onRemoveNumber: (Int) -> Unit,
    onClearNumbers: () -> Unit,
    onRequestAiAnalysis: () -> Unit,
    onSaveForTracking: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "user_numbers_honesty_banner") {
            StatsHonestyBanner()
        }

        item(key = "user_numbers_input_card") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Inspectez vos propres chiffres (0 à 90)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Saisissez 1 chiffre (NAP 1), 2 (NAP 2), 3 (NAP 3) ou jusqu'à 5 numéros séparés par un espace ou une virgule.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        label = { Text("Ex : 17, 45, 82 ou un chiffre à la fois") },
                        placeholder = { Text("0 à 90 (max 5 chiffres)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("user_numbers_input"),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (onSubmitNumbers(inputText)) {
                                    inputText = ""
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (onSubmitNumbers(inputText)) {
                                    inputText = ""
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("analyze_user_numbers_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(AppIcons.Analytics, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Analyser", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        if (selectedNumbers.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    inputText = ""
                                    onClearNumbers()
                                },
                                modifier = Modifier.testTag("clear_user_numbers_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(AppIcons.DeleteSweep, contentDescription = "Effacer", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Effacer", fontSize = 12.sp)
                            }
                        }
                    }

                    if (selectedNumbers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Votre sélection (${selectedNumbers.size}/5) — Touchez un numéro pour le retirer :",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            selectedNumbers.forEach { num ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                        .clickable { onRemoveNumber(num) }
                                        .padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                                        .testTag("user_selected_ball_$num")
                                ) {
                                    LotoBall(number = num, size = 34.dp, variant = BallVariant.GOLD)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Retirer le numéro $num",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (analysis == null) {
            item(key = "user_numbers_empty_state") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = AppIcons.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Explorez l'historique de vos numéros",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "L'application affiche le ratio combinatoire théorique, vérifie si vos numéros sont déjà sortis ensemble dans l'historique et mesure leurs retards actuels.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            item(key = "user_numbers_verdict") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("user_analysis_verdict_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = analysis.gameTypeLabel,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = Color(0xFF1D4ED8)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFDBEAFE))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = analysis.feasibilityLevel,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF1E3A8A)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White)
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Ratio combinatoire théorique",
                                        fontSize = 11.sp,
                                        color = Color(0xFF475569)
                                    )
                                    Text(
                                        text = analysis.exactHitOddsText,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    if (analysis.numbers.size >= 2) {
                                        Text(
                                            text = "Au moins 1 chiffre (théorique) : ${"%.1f".format(analysis.atLeastOneHitProbabilityPercent)} %",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF2563EB),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White)
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Indice passé observé",
                                        fontSize = 11.sp,
                                        color = Color(0xFF475569)
                                    )
                                    Text(
                                        text = "${analysis.statisticalScorePercent} %",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp,
                                        color = Color(0xFF059669)
                                    )
                                    Text(
                                        text = "Sur ${analysis.totalDrawsAnalyzed} tirages",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = analysis.verdictExplanation,
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFF1E293B)
                        )
                    }
                }
            }

            if (analysis.numbers.size >= 2) {
                item(key = "user_numbers_synergies") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Vérification dans l'historique",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• Tous ensemble (${analysis.numbers.size}/${analysis.numbers.size}) : ${analysis.exactGroupHistoricalHits} fois",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (analysis.exactGroupHistoricalHits > 0) Color(0xFF047857) else MaterialTheme.colorScheme.onSurface
                            )
                            if (analysis.exactGroupHitDates.isNotEmpty()) {
                                Text(
                                    text = "  Dates récentes : ${analysis.exactGroupHitDates.joinToString(", ")}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "• Au moins 2 numéros ensemble : ${analysis.partial2PlusHistoricalHits} tirage(s)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (analysis.pairSynergies.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Co-occurrences observées paire par paire :",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                analysis.pairSynergies.take(6).forEach { syn ->
                                    Text(
                                        text = "  • ${syn.n1.toString().padStart(2, '0')} & ${syn.n2.toString().padStart(2, '0')} : ${syn.coOccurrenceCount} fois (${syn.coOccurrencePercent} %)",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item(key = "user_numbers_details") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Historique de chaque numéro",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        analysis.numberDetails.forEach { detail ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    LotoBall(number = detail.number, size = 40.dp, variant = BallVariant.DEFAULT)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "N° ${detail.formattedNumber} • ${detail.trendLabel}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp
                                        )
                                        Text(
                                            text = "${detail.totalCount} sorties (${detail.frequencyPercent} %) • ${detail.recentCount} récent(s)",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Retard actuel : ${detail.currentGap} tirages (Max: ${detail.maxGap})",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = "${detail.individualScorePercent.toInt()}%",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            item(key = "user_numbers_notes") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                AppIcons.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Notes descriptives",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = Color(0xFF92400E)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        analysis.recommendations.forEach { tip ->
                            Text(
                                text = "• $tip",
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = Color(0xFF78350F),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            item(key = "user_numbers_actions") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = onRequestAiAnalysis,
                        enabled = !isAnalyzingWithAi,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_analyze_user_numbers_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isAnalyzingWithAi) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Lecture IA en cours...", fontSize = 13.sp)
                        } else {
                            Icon(AppIcons.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Synthèse descriptive par IA sur ces chiffres",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                        }
                    }

                    AnimatedVisibility(visible = !aiReport.isNullOrBlank()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("user_numbers_ai_report_card"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        AppIcons.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Synthèse de l'Analyste IA",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = aiReport.orEmpty(),
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onSaveForTracking,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_user_numbers_tracking_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(AppIcons.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Archiver ces chiffres pour suivi comparatif",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun NapPairCard(pair: NapItem) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                pair.numbers.forEachIndexed { idx, num ->
                    LotoBall(number = num, size = 42.dp, variant = BallVariant.DEFAULT)
                    if (idx < pair.numbers.size - 1) {
                        Text(
                            text = "+",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 6.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = pair.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = pair.description,
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }

            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("NAP 2", pair.numbers.joinToString("-")))
                    Toast.makeText(context, "Paire ${pair.numbers.joinToString("-")} copiée.", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    AppIcons.ContentCopy,
                    contentDescription = "Copier la paire",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun NapTrioCard(trio: NapItem) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                trio.numbers.forEachIndexed { idx, num ->
                    LotoBall(number = num, size = 38.dp, variant = BallVariant.DEFAULT)
                    if (idx < trio.numbers.size - 1) {
                        Text(
                            text = "+",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = trio.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                    Text(
                        text = trio.description,
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }

            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("NAP 3", trio.numbers.joinToString("-")))
                    Toast.makeText(context, "Trio ${trio.numbers.joinToString("-")} copié.", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    AppIcons.ContentCopy,
                    contentDescription = "Copier le trio",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
