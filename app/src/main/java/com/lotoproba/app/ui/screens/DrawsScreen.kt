package com.lotoproba.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lotoproba.app.data.model.LotoDraw
import com.lotoproba.app.data.model.LotoSource
import com.lotoproba.app.ui.components.AppIcons
import com.lotoproba.app.ui.components.BallVariant
import com.lotoproba.app.ui.components.LotoBall

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DrawsScreen(
    sources: List<LotoSource>,
    selectedSourceId: String,
    draws: List<LotoDraw>,
    isRefreshing: Boolean,
    onSelectSource: (String) -> Unit,
    onRefreshWeb: () -> Unit,
    onOpenManualImport: () -> Unit,
    onOpenAddDraw: () -> Unit,
    onOpenAddSource: () -> Unit,
    onEditCurrentSource: (LotoSource) -> Unit,
    onExportCsv: () -> String,
    onDeleteDraw: (LotoDraw) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentSource = sources.find { it.id == selectedSourceId }

    var filterNumberText by remember { mutableStateOf("") }
    val filterNumber = filterNumberText.toIntOrNull()

    val filteredDraws = remember(draws, filterNumber) {
        if (filterNumber != null && filterNumber in 0..90) {
            draws.filter { it.containsNumber(filterNumber) }
        } else {
            draws
        }
    }

    var showSourceDropdown by remember { mutableStateOf(false) }
    var isToolsSectionExpanded by remember { mutableStateOf(false) }
    var isSearchSectionExpanded by remember { mutableStateOf(false) }
    val latestDraw = draws.firstOrNull()

    // Une seule zone de défilement verticale (LazyColumn) pour fonctionner aussi bien en portrait qu'en paysage.
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("draws_lazy_column"),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Dernier tirage enregistré
        if (latestDraw != null) {
            item(key = "latest_draw_card") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                Text(
                                    text = "DERNIER RÉSULTAT",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${currentSource?.country ?: "Niger"}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = {
                                    val text =
                                        "${latestDraw.dateString} : ${latestDraw.getNumbersList().joinToString(" - ")}"
                                    val clipboard =
                                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Dernier Tirage", text))
                                    Toast.makeText(context, "Tirage copié.", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("copy_latest_draw_button")
                            ) {
                                Icon(
                                    AppIcons.ContentCopy,
                                    contentDescription = "Copier le dernier tirage",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Text(
                            text = latestDraw.dateString,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            latestDraw.getNumbersList().forEach { num ->
                                LotoBall(
                                    number = num,
                                    size = 48.dp,
                                    variant = BallVariant.GOLD,
                                    isSelected = true
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onRefreshWeb,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("refresh_draws_button"),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isRefreshing
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mise à jour en cours...", fontSize = 13.sp)
                            } else {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (currentSource?.url?.startsWith("asset://") == true) {
                                        "Vérifier le CSV local intégré"
                                    } else {
                                        "Actualiser depuis la source"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Source active & accès rapide
        item(key = "active_source_card") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showSourceDropdown = true }
                                    .padding(horizontal = 4.dp, vertical = 6.dp)
                                    .testTag("source_selector_dropdown")
                            ) {
                                Icon(
                                    AppIcons.Public,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = currentSource?.name ?: "Choisir une source",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(" ▾", fontSize = 13.sp)
                            }

                            DropdownMenu(
                                expanded = showSourceDropdown,
                                onDismissRequest = { showSourceDropdown = false }
                            ) {
                                sources.forEach { source ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(source.name, fontWeight = FontWeight.SemiBold)
                                                Text(
                                                    "${source.country} • ${source.url}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1
                                                )
                                            }
                                        },
                                        onClick = {
                                            onSelectSource(source.id)
                                            showSourceDropdown = false
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "+ Ajouter une source web...",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    onClick = {
                                        showSourceDropdown = false
                                        onOpenAddSource()
                                    }
                                )
                            }
                        }

                        if (currentSource != null) {
                            OutlinedButton(
                                onClick = { onEditCurrentSource(currentSource) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Configurer", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val urlLabel = if (currentSource?.url?.startsWith("asset://") == true) {
                        "CSV intégré : assets/niger_2years.csv"
                    } else {
                        "Lien : ${currentSource?.url ?: "Non configuré"}"
                    }
                    Text(
                        text = urlLabel,
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    val isHealthyStatus = currentSource?.lastUpdateStatus?.contains("succès", ignoreCase = true) == true ||
                        currentSource?.lastUpdateStatus?.contains("jour", ignoreCase = true) == true ||
                        currentSource?.lastUpdateStatus?.contains("intégrées", ignoreCase = true) == true ||
                        currentSource?.lastUpdateStatus?.contains("chargé", ignoreCase = true) == true
                    Text(
                        text = "Statut : ${currentSource?.lastUpdateStatus ?: "Synchronisé"}",
                        fontSize = 11.sp,
                        color = if (isHealthyStatus) Color(0xFF10B981) else Color(0xFFF59E0B)
                    )

                    if (latestDraw == null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onRefreshWeb,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isRefreshing
                        ) {
                            Text("Charger les tirages de cette source")
                        }
                    }
                }
            }
        }

        // 3. Section repliable : Outils rapides du tableau (inline, pas un tiroir latéral)
        item(key = "tools_section_card") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isToolsSectionExpanded = !isToolsSectionExpanded }
                            .padding(horizontal = 6.dp, vertical = 8.dp)
                            .testTag("toggle_inline_tools_section"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                AppIcons.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Outils rapides de saisie & CSV",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isToolsSectionExpanded) "Replier" else "Déplier",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (isToolsSectionExpanded) AppIcons.ExpandLess else AppIcons.ExpandMore,
                                contentDescription = if (isToolsSectionExpanded) "Replier les outils" else "Déplier les outils",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    AnimatedVisibility(visible = isToolsSectionExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = onOpenManualImport,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("inline_import_csv_button"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(AppIcons.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Importer CSV", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val csv = onExportCsv()
                                        val clipboard =
                                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("LotoProba Export", csv))
                                        Toast.makeText(
                                            context,
                                            "${draws.size} tirages copiés au format CSV.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("inline_export_csv_button"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(AppIcons.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Exporter CSV", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onOpenAddDraw,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("inline_add_draw_button"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ajouter tirage", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }

                                OutlinedButton(
                                    onClick = onOpenAddSource,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("inline_add_source_button"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(AppIcons.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Nouvelle source", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Section repliable : Filtre par numéro
        item(key = "filter_section_card") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isSearchSectionExpanded = !isSearchSectionExpanded }
                            .padding(horizontal = 4.dp, vertical = 6.dp)
                            .testTag("toggle_inline_filter_section"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (filterNumber != null) {
                                    "Filtré par n°$filterNumber (${filteredDraws.size} tirages)"
                                } else {
                                    "Filtrer l'historique par numéro (0–90)"
                                },
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Icon(
                            imageVector = if (isSearchSectionExpanded) AppIcons.ExpandLess else AppIcons.ExpandMore,
                            contentDescription = "Déplier le filtre",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    AnimatedVisibility(visible = isSearchSectionExpanded) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedTextField(
                                value = filterNumberText,
                                onValueChange = { if (it.length <= 2) filterNumberText = it },
                                label = { Text("Numéro (0-90)", fontSize = 11.sp) },
                                placeholder = { Text("Ex: 17", fontSize = 11.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("filter_number_input"),
                                singleLine = true,
                                trailingIcon = {
                                    if (filterNumberText.isNotEmpty()) {
                                        IconButton(
                                            onClick = { filterNumberText = "" },
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(Icons.Default.Clear, contentDescription = "Effacer le filtre")
                                        }
                                    }
                                }
                            )

                            if (filterNumberText.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${filteredDraws.size} trouvé(s)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Compteur de tirages
        item(key = "draws_count_header") {
            Text(
                text = "${filteredDraws.size} tirage(s) sur ${draws.size} (Base Room locale)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // 6. Liste des tirages (ou état vide)
        if (filteredDraws.isEmpty()) {
            item(key = "empty_draws_state") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            AppIcons.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (filterNumber != null) {
                                "Aucun tirage contenant le numéro $filterNumber"
                            } else {
                                "Aucun tirage enregistré pour cette source"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(
                items = filteredDraws,
                key = { "${it.sourceId}_${it.dateString}_${it.id}" }
            ) { draw ->
                DrawItemCard(
                    draw = draw,
                    highlightNumber = filterNumber,
                    onDelete = { onDeleteDraw(draw) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DrawItemCard(
    draw: LotoDraw,
    highlightNumber: Int?,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = draw.dateString,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = draw.drawName,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    draw.getNumbersList().forEach { num ->
                        val isHighlighted = highlightNumber != null && highlightNumber == num
                        LotoBall(
                            number = num,
                            size = 38.dp,
                            variant = if (isHighlighted) BallVariant.GOLD else BallVariant.DEFAULT,
                            isSelected = isHighlighted
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Supprimer le tirage du ${draw.dateString}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
