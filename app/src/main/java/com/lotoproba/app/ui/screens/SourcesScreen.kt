package com.lotoproba.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lotoproba.app.data.model.LotoSource
import com.lotoproba.app.ui.components.AppIcons

@Composable
fun SourcesScreen(
    sources: List<LotoSource>,
    selectedSourceId: String,
    onSelectSource: (String) -> Unit,
    onAddSource: () -> Unit,
    onEditSource: (LotoSource) -> Unit,
    onDeleteSource: (LotoSource) -> Unit,
    onRefreshSource: () -> Unit,
    onResetSampleData: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "sources_header_card") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Sources de tirages & CSV intégré",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "La source par défaut utilise le fichier CSV embarqué (assets/niger_2years.csv) pour fonctionner 100 % hors ligne. Vous pouvez ajouter vos propres URL de résultats (Google Sites, HTML, CSV) avec délai maximal de 15 s et conservation des données locales en cas d'erreur.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        item(key = "sources_actions_row") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sources enregistrées (${sources.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Button(
                    onClick = onAddSource,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_source_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ajouter un lien", fontSize = 12.sp)
                }
            }
        }

        items(
            items = sources,
            key = { it.id }
        ) { source ->
            val isSelected = source.id == selectedSourceId
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectSource(source.id) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFFEFF6FF) else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 1.dp)
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
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectSource(source.id) }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = source.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) Color(0xFF1D4ED8) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = source.country,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row {
                            IconButton(
                                onClick = { onEditSource(source) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Modifier la source ${source.name}",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (sources.size > 1) {
                                IconButton(
                                    onClick = { onDeleteSource(source) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Supprimer la source ${source.name}",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (source.url.startsWith("asset://")) {
                            "Source locale : assets/niger_2years.csv"
                        } else {
                            "URL : ${source.url}"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    val isHealthyStatus = source.lastUpdateStatus.contains("succès", ignoreCase = true) ||
                        source.lastUpdateStatus.contains("jour", ignoreCase = true) ||
                        source.lastUpdateStatus.contains("intégrées", ignoreCase = true) ||
                        source.lastUpdateStatus.contains("chargé", ignoreCase = true)
                    Text(
                        text = "Dernière synchro : ${source.lastUpdateStatus}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isHealthyStatus) Color(0xFF059669) else Color(0xFFD97706)
                    )
                }
            }
        }

        item(key = "sources_footer_actions") {
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onRefreshSource,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Synchroniser la source active", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onResetSampleData,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(AppIcons.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Recharger les tirages du CSV intégré", fontSize = 12.sp)
                }
            }
        }
    }
}
