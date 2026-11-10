package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.data.network.ImportReport

@Composable
fun ImportReportDialog(
    report: ImportReport,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (report.validCount > 0) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (report.validCount > 0) Color(0xFF10B981) else Color(0xFFEF4444)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Rapport d'analyse et import", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Résumé chiffré
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Lignes traitées", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${report.totalProcessed}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Column {
                        Text("Tirages valides", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${report.validCount}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF10B981))
                    }
                    Column {
                        Text("Doublons ignorés", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${report.duplicateCount}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFF59E0B))
                    }
                    Column {
                        Text("Rejetées", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${report.invalidCount}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFEF4444))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (report.invalidLines.isNotEmpty()) {
                    Text(
                        text = "Détail des lignes invalides ignorées (${report.invalidLines.size}) :",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFEF4444)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        items(report.invalidLines) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFEF2F2)
                                )
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = "Ligne ${item.lineNumber} : ${item.reason}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF991B1B)
                                    )
                                    Text(
                                        text = item.rawText,
                                        fontSize = 10.sp,
                                        color = Color(0xFFB91C1C),
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFD1FAE5))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Toutes les lignes analysées sont conformes aux règles (5 numéros distincts entre 0 et 90).",
                            color = Color(0xFF065F46),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Compris")
            }
        }
    )
}
