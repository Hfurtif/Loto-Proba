package com.lotoproba.app.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lotoproba.app.data.model.AiEvaluationRecord
import com.lotoproba.app.data.model.LotoDraw
import com.lotoproba.app.ui.components.AppIcons
import com.lotoproba.app.ui.components.BallVariant
import com.lotoproba.app.ui.components.LotoBall
import com.lotoproba.app.ui.components.StatsHonestyBanner

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiScreen(
    customApiKey: String,
    isKeyConfigured: Boolean,
    isAiGenerating: Boolean,
    isAiEvaluating: Boolean,
    latestPrediction: AiEvaluationRecord?,
    aiEvaluations: List<AiEvaluationRecord>,
    latestDraw: LotoDraw?,
    onSaveApiKey: (String) -> Unit,
    onClearApiKey: () -> Unit,
    onGeneratePrediction: () -> Unit,
    onEvaluateLatestDraw: () -> Unit,
    onDeleteEvaluation: (AiEvaluationRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    var apiKeyInput by remember(customApiKey) { mutableStateOf(customApiKey) }
    var isApiKeyMasked by remember { mutableStateOf(true) }
    var isApiKeySectionExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "ai_honesty_banner") {
            StatsHonestyBanner()
        }

        item(key = "ai_header_card") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(AppIcons.AutoAwesome, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Analyste statistique IA",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Synthèse descriptive & comparaison rétrospective",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        val statusText = if (customApiKey.isNotEmpty()) "Clé Perso" else if (isKeyConfigured) "Clé Active" else "Mode Local"
                        val statusColor = if (isKeyConfigured) Color(0xFF10B981) else Color(0xFFF59E0B)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(statusText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "L'outil résume les fréquences observées dans l'historique archivé (ou hors ligne sans clé API), puis compare rétrospectivement la synthèse au dernier tirage officiel enregistré.",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Paramètres repliables de la clé API Gemini
        item(key = "api_key_section_card") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isApiKeySectionExpanded = !isApiKeySectionExpanded }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                AppIcons.Key,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Paramètres de la Clé API Gemini (optionnel)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Icon(
                            imageVector = if (isApiKeySectionExpanded) AppIcons.ExpandLess else AppIcons.ExpandMore,
                            contentDescription = if (isApiKeySectionExpanded) "Replier" else "Déplier",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    AnimatedVisibility(visible = isApiKeySectionExpanded) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Text(
                                text = "Sans clé API, l'application fonctionne entièrement en mode statistique local. Vous pouvez saisir une clé Gemini personnelle si vous souhaitez des synthèses textuelles enrichies :",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = apiKeyInput,
                                onValueChange = { apiKeyInput = it },
                                label = { Text("Clé API Gemini (AIzaSy...)") },
                                placeholder = { Text("Collez votre clé API ici") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                visualTransformation = if (isApiKeyMasked) PasswordVisualTransformation() else VisualTransformation.None,
                                trailingIcon = {
                                    IconButton(
                                        onClick = { isApiKeyMasked = !isApiKeyMasked },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isApiKeyMasked) AppIcons.VisibilityOff else AppIcons.Visibility,
                                            contentDescription = if (isApiKeyMasked) "Afficher" else "Masquer"
                                        )
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onSaveApiKey(apiKeyInput) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Enregistrer la clé", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                if (customApiKey.isNotEmpty()) {
                                    OutlinedButton(
                                        onClick = {
                                            onClearApiKey()
                                            apiKeyInput = ""
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Réinitialiser", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Deux boutons d'action
        item(key = "ai_action_buttons") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onGeneratePrediction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isAiGenerating && !isAiEvaluating
                ) {
                    if (isAiGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Synthèse IA en cours...", fontSize = 14.sp)
                    } else {
                        Icon(AppIcons.Psychology, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("1. Générer une synthèse historique (IA)", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                FilledTonalButton(
                    onClick = onEvaluateLatestDraw,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isAiGenerating && !isAiEvaluating && latestDraw != null
                ) {
                    if (isAiEvaluating) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Comparaison en cours...", fontSize = 14.sp)
                    } else {
                        Icon(AppIcons.Verified, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "2. Comparer au dernier tirage enregistré",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        val latestEvaluated = aiEvaluations.firstOrNull { it.evaluated }
        if (latestEvaluated != null) {
            item(key = "ai_latest_evaluated") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BILAN RÉTROSPECTIF DU TIRAGE",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.5.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = latestEvaluated.actualDrawDate ?: "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val hasMultipleMatches = latestEvaluated.matchedCount >= 2
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (hasMultipleMatches) Color(0xFF10B981).copy(alpha = 0.15f)
                                    else Color(0xFFF59E0B).copy(alpha = 0.15f)
                                )
                                .padding(10.dp)
                        ) {
                            Text(
                                text = latestEvaluated.verdictSummary ?: "Comparaison terminée",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = if (hasMultipleMatches) Color(0xFF047857) else Color(0xFFB45309)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Tirage officiel :",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val actualList = latestEvaluated.getActualWinningList()
                            val perm5List = latestEvaluated.getPerm5List()
                            actualList.forEach { num ->
                                val wasInSelection = perm5List.contains(num)
                                LotoBall(
                                    number = num,
                                    size = 36.dp,
                                    variant = if (wasInSelection) BallVariant.GOLD else BallVariant.DEFAULT,
                                    isSelected = wasInSelection
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Observation rétrospective :",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = latestEvaluated.explanationWhy
                                ?: "Lecture descriptive des fréquences observées et des écarts.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        AppIcons.Lightbulb,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Rappel méthodologique",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = latestEvaluated.actionAdviceNextDraw
                                        ?: "Chaque tirage est indépendant : les fréquences observées dans le passé ne modifient pas les tirages suivants.",
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        val activeSummary = latestPrediction
        if (activeSummary != null) {
            item(key = "ai_latest_summary") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SYNTHÈSE IA DU : ${activeSummary.targetDate}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "Indice ${activeSummary.confidenceScore}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Repère (NAP 1) :", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            LotoBall(
                                number = activeSummary.bankerN1,
                                size = 40.dp,
                                variant = BallVariant.GOLD,
                                isSelected = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Repère fréquentiel observé",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Paires historiques (NAP 2) :", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            activeSummary.getNap2List().forEach { pair ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(pair.joinToString(" - "), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Sélection descriptive (PERM 5) :", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            activeSummary.getPerm5List().forEach { num ->
                                LotoBall(number = num, size = 36.dp, variant = BallVariant.DEFAULT)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Commentaire statistique :",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = activeSummary.aiReasoning,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (aiEvaluations.size > 1) {
            item(key = "ai_history_header") {
                Text(
                    text = "Historique des synthèses & comparaisons",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(
                items = aiEvaluations.drop(1),
                key = { it.id }
            ) { record ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Synthèse du ${record.targetDate}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                            Text(
                                text = "Repère : ${record.bankerN1} • Perm 5 : ${record.perm5Csv}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (record.evaluated) {
                                Text(
                                    text = record.verdictSummary ?: "Évalué",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (record.matchedCount >= 2) Color(0xFF047857) else Color(0xFFB45309)
                                )
                            }
                        }

                        IconButton(
                            onClick = { onDeleteEvaluation(record) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Supprimer le rapport",
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
