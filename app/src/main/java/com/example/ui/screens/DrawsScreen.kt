package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LotoDraw
import com.example.data.model.LotoSource
import com.example.ui.components.BallVariant
import com.example.ui.components.LotoBall

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

    val filteredDraws = if (filterNumber != null && filterNumber in 0..90) {
        draws.filter { it.containsNumber(filterNumber) }
    } else {
        draws
    }

    var showSourceDropdown by remember { mutableStateOf(false) }
    var isToolsDrawerOpen by remember { mutableStateOf(false) }
    var isSearchDrawerOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        val latestDraw = draws.firstOrNull()

        // 🎯 CARTE DU DERNIER RÉSULTAT DU JOUR (FRONT & CENTER)
        if (latestDraw != null) {
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🎯 DERNIER RÉSULTAT",
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

                        // Bouton copier
                        IconButton(
                            onClick = {
                                val text = "${latestDraw.dateString} : ${latestDraw.getNumbersList().joinToString(" - ")}"
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Dernier Tirage", text))
                                Toast.makeText(context, "Résultat copié !", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copier",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = latestDraw.dateString,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5 Grosses Boules de Tirage
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
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

                    // Bouton d'actualisation directe
                    Button(
                        onClick = onRefreshWeb,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isRefreshing
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Vérification sur le site en cours...", fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Actualiser les résultats du jour", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // En-tête : Source active & lien (compact et modifiable)
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
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showSourceDropdown = true }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Public, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
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
                                            Text("${source.country} • ${source.url}", fontSize = 11.sp, color = Color.Gray, maxLines = 1)
                                        }
                                    },
                                    onClick = {
                                        onSelectSource(source.id)
                                        showSourceDropdown = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("+ Ajouter un autre pays / lien...", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
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
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Changer le lien", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Lien : ${currentSource?.url ?: "Non configuré"}",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Statut : ${currentSource?.lastUpdateStatus ?: "Synchronisé"}",
                    fontSize = 11.sp,
                    color = if (currentSource?.lastUpdateStatus?.contains("succès", ignoreCase = true) == true ||
                        currentSource?.lastUpdateStatus?.contains("officiel", ignoreCase = true) == true ||
                        currentSource?.lastUpdateStatus?.contains("jour", ignoreCase = true) == true
                    ) Color(0xFF10B981) else Color(0xFFF59E0B)
                )

                if (latestDraw == null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onRefreshWeb,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isRefreshing
                    ) {
                        Text("Télécharger tous les résultats")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Spacer(modifier = Modifier.height(10.dp))

        // 🗄️ TIROIR D'ACTIONS & OUTILS (BOUTONS RANGÉS EN GRILLE)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // En-tête cliquable du tiroir
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isToolsDrawerOpen = !isToolsDrawerOpen }
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🗄️ Tiroir des Outils & Actions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isToolsDrawerOpen) "Replier" else "Ouvrir (4 outils)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (isToolsDrawerOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isToolsDrawerOpen) "Fermer le tiroir" else "Ouvrir le tiroir",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Contenu dépliable : les boutons bien rangés en grille équilibrée 2x2
                AnimatedVisibility(visible = isToolsDrawerOpen) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Ligne 1 : Import & Export
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = onOpenManualImport,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Importer CSV", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val csv = onExportCsv()
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Loto-Proba Export", csv))
                                    Toast.makeText(context, "${draws.size} tirages copiés au format CSV !", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Exporter CSV", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Ligne 2 : Ajouter tirage & Changer de pays
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onOpenAddDraw,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
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
                                    .height(44.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Gérer pays", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 🔍 TIROIR DE RECHERCHE & FILTRAGE (DÉPLIABLE)
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
                        .clickable { isSearchDrawerOpen = !isSearchDrawerOpen }
                        .padding(horizontal = 4.dp, vertical = 4.dp),
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
                            text = if (filterNumber != null) "Filtré par n°$filterNumber (${filteredDraws.size} tirages)" else "🔍 Tiroir de recherche & Filtre",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Icon(
                        imageVector = if (isSearchDrawerOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Ouvrir le filtre",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                AnimatedVisibility(visible = isSearchDrawerOpen) {
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
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            trailingIcon = {
                                if (filterNumberText.isNotEmpty()) {
                                    IconButton(onClick = { filterNumberText = "" }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Clear, contentDescription = "Effacer")
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        if (filterNumberText.isNotEmpty()) {
                            Text(
                                text = "${filteredDraws.size} trouvés",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Indicateur du nombre de tirages affichés
        Text(
            text = "📋 ${filteredDraws.size} tirage(s) sur ${draws.size} (Base Room locale)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Liste des tirages
        if (filteredDraws.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (filterNumber != null) "Aucun tirage avec le numéro $filterNumber" else "Aucun tirage enregistré pour cette source",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredDraws, key = { it.id }) { draw ->
                    DrawItemCard(
                        draw = draw,
                        highlightNumber = filterNumber,
                        onDelete = { onDeleteDraw(draw) }
                    )
                }
            }
        }
    }
}

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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val numbers = draw.getNumbersList()
                    numbers.forEach { num ->
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
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Supprimer",
                    tint = Color.Gray.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
