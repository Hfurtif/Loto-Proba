package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiEvaluationRecord
import com.example.data.model.LotoDraw
import com.example.ui.components.BallVariant
import com.example.ui.components.LotoBall

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
    val context = LocalContext.current
    var apiKeyInput by remember(customApiKey) { mutableStateOf(customApiKey) }
    var isApiKeyMasked by remember { mutableStateOf(true) }
    var isApiKeyDrawerOpen by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // En-tête : Bannière IA
        item {
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Intelligence Artificielle",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Prédiction & Analyse Rétrospective",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Badge de statut Clé
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
                        text = "L'IA analyse l'historique complet pour prédire les combinaisons du prochain tirage, puis vérifie le résultat réel pour diagnostiquer si elle a eu raison ou tort et vous dire exactement quoi faire.",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // 🔑 TIROIR DES PARAMÈTRES DE LA CLÉ API (MODIFIABLE PAR L'UTILISATEUR)
        item {
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
                            .clickable { isApiKeyDrawerOpen = !isApiKeyDrawerOpen }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Paramètres de la Clé API Gemini",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Icon(
                            imageVector = if (isApiKeyDrawerOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    AnimatedVisibility(visible = isApiKeyDrawerOpen) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Text(
                                text = "Vous pouvez entrer ici votre propre clé API Gemini (gratuite sur Google AI Studio). Elle sera sauvegardée sur votre appareil et vous pourrez la modifier quand vous le souhaitez :",
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
                                    IconButton(onClick = { isApiKeyMasked = !isApiKeyMasked }) {
                                        Icon(
                                            imageVector = if (isApiKeyMasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
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

        // 🚀 LES DEUX BOUTONS D'ACTION MAJEURS
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Bouton 1 : Prédire pour demain
                Button(
                    onClick = onGeneratePrediction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isAiGenerating && !isAiEvaluating
                ) {
                    if (isAiGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Analyse & Prédiction IA en cours...", fontSize = 14.sp)
                    } else {
                        Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("1. Prédire le Prochain Tirage (IA)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Bouton 2 : Analyser le nouveau résultat sorti
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
                        Text("Vérification & Diagnostic en cours...", fontSize = 14.sp)
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "2. Analyser le Résultat Sorti (Vérifier l'IA)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 🎯 CARTE D'ÉVALUATION DU DERNIER TIRAGE SORTI (SI ÉVALUÉ)
        val latestEvaluated = aiEvaluations.firstOrNull { it.evaluated }
        if (latestEvaluated != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
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
                                text = "🎯 DIAGNOSTIC & VÉRITÉ DU TIRAGE",
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

                        // Verdict
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (latestEvaluated.verdictSummary?.contains("SUCCÈS") == true || latestEvaluated.matchedCount >= 2)
                                        Color(0xFF10B981).copy(alpha = 0.15f)
                                    else
                                        Color(0xFFF59E0B).copy(alpha = 0.15f)
                                )
                                .padding(10.dp)
                        ) {
                            Text(
                                text = latestEvaluated.verdictSummary ?: "Évaluation terminée",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = if (latestEvaluated.verdictSummary?.contains("SUCCÈS") == true || latestEvaluated.matchedCount >= 2)
                                    Color(0xFF047857)
                                else
                                    Color(0xFFB45309)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Numéros réels du tirage
                        Text(
                            text = "Tirage officiel :",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val actualList = latestEvaluated.getActualWinningList()
                            val perm5List = latestEvaluated.getPerm5List()
                            actualList.forEach { num ->
                                val wasPredicted = perm5List.contains(num)
                                LotoBall(
                                    number = num,
                                    size = 36.dp,
                                    variant = if (wasPredicted) BallVariant.GOLD else BallVariant.DEFAULT,
                                    isSelected = wasPredicted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 🧐 Pourquoi ces numéros sont sortis
                        Text(
                            text = "🧐 Pourquoi ces numéros sont sortis (Analyse IA) :",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = latestEvaluated.explanationWhy ?: "Analyse des dynamiques de séries et des écarts.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 💡 QUE FAUT-IL FAIRE DANS CE CAS POUR LE PROCHAIN TIRAGE ?
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "💡 Que faut-il faire dans ce cas pour le prochain tirage ?",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = latestEvaluated.actionAdviceNextDraw ?: "Conserver une gestion stricte des mises et surveiller les numéros en retard d'écart.",
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

        // 🔮 PRÉDICTION EN COURS (SI NON ÉVALUÉE OU DERNIÈRE)
        val activePrediction = latestPrediction
        if (activePrediction != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔮 PRÉDICTION IA POUR LE : ${activePrediction.targetDate}",
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
                                Text("Score ${activePrediction.confidenceScore}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Banker N1
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Banker (NAP 1) :", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            LotoBall(number = activePrediction.bankerN1, size = 40.dp, variant = BallVariant.GOLD, isSelected = true)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Indispensable en base", fontSize = 11.sp, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Paires NAP 2
                        Text("Paires fortes (NAP 2) :", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            activePrediction.getNap2List().forEach { pair ->
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

                        // Grille PERM 5
                        Text("Grille complète (PERM 5) :", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            activePrediction.getPerm5List().forEach { num ->
                                LotoBall(number = num, size = 36.dp, variant = BallVariant.DEFAULT)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Raisonnement IA
                        Text(
                            text = "Raisonnement de l'IA :",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = activePrediction.aiReasoning,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 📜 HISTORIQUE DES ANALYSES IA
        if (aiEvaluations.size > 1) {
            item {
                Text(
                    text = "📜 Historique des prédictions & analyses précédentes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(aiEvaluations.drop(1), key = { it.id }) { record ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
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
                                text = "Prédiction pour le ${record.targetDate}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                            Text(
                                text = "Banker : ${record.bankerN1} • Perm 5 : ${record.perm5Csv}",
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

                        IconButton(onClick = { onDeleteEvaluation(record) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer", modifier = Modifier.size(16.dp), tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
