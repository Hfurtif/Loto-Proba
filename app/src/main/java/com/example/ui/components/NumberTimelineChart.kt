package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LotoDraw
import com.example.stats.NumberStat

@Composable
fun NumberTimelineChart(
    number: Int,
    stats: NumberStat?,
    draws: List<LotoDraw>,
    onNumberChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val chronologicalDraws = draws.sortedBy { it.dateString }
    val totalDraws = chronologicalDraws.size

    Column(modifier = modifier.fillMaxWidth()) {
        // En-tête avec navigation de numéro et statistiques
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onNumberChange((number - 1 + 91) % 91) }
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Numéro précédent")
                }

                LotoBall(number = number, size = 44.dp, variant = BallVariant.GOLD)

                IconButton(
                    onClick = { onNumberChange((number + 1) % 91) }
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Numéro suivant")
                }
            }

            if (stats != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Sorties : ${stats.count} (${"%.1f".format(stats.frequencyPercent)} %)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Écart actuel : ${stats.currentGap} tirages (Max: ${stats.maxGap})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Courbe cumulative / timeline
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(8.dp)
        ) {
            if (totalDraws == 0) {
                Text(
                    text = "Aucun tirage disponible",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 12.sp
                )
            } else {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val chartWidth = size.width
                    val chartHeight = size.height - 25f

                    // Calcul de la courbe cumulative d'occurrences
                    val points = mutableListOf<Offset>()
                    var cumulativeCount = 0
                    val totalExpected = if (totalDraws > 0) (totalDraws * 5.0) / 91.0 else 1.0
                    val maxPossibleY = (stats?.count ?: 1).coerceAtLeast(3).toFloat()

                    chronologicalDraws.forEachIndexed { index, draw ->
                        if (draw.containsNumber(number)) {
                            cumulativeCount++
                        }
                        val x = (index.toFloat() / (totalDraws - 1).coerceAtLeast(1)) * chartWidth
                        val y = chartHeight * (1f - (cumulativeCount.toFloat() / maxPossibleY))
                        points.add(Offset(x, y))
                    }

                    // Ligne de tendance théorique linéaire attendue
                    val endYExpected = chartHeight * (1f - (totalExpected.toFloat() / maxPossibleY).coerceIn(0f, 1f))
                    drawLine(
                        color = Color(0xFFEF4444).copy(alpha = 0.5f),
                        start = Offset(0f, chartHeight),
                        end = Offset(chartWidth, endYExpected),
                        strokeWidth = 2f
                    )

                    // Dessiner la courbe cumulative
                    if (points.size > 1) {
                        val path = Path().apply {
                            moveTo(points[0].x, points[0].y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFF3B82F6),
                            style = Stroke(width = 3f)
                        )
                    }

                    // Marquer chaque apparition par un point lumineux
                    chronologicalDraws.forEachIndexed { index, draw ->
                        if (draw.containsNumber(number)) {
                            val x = (index.toFloat() / (totalDraws - 1).coerceAtLeast(1)) * chartWidth
                            val y = points.getOrNull(index)?.y ?: chartHeight
                            drawCircle(
                                color = Color(0xFF10B981),
                                radius = 4f,
                                center = Offset(x, y)
                            )
                        }
                    }

                    // Libellé axe temporel
                    val firstDate = chronologicalDraws.firstOrNull()?.dateString ?: ""
                    val lastDate = chronologicalDraws.lastOrNull()?.dateString ?: ""

                    val startLayout = textMeasurer.measure(
                        text = firstDate,
                        style = TextStyle(fontSize = 9.sp, color = Color.Gray)
                    )
                    drawText(startLayout, topLeft = Offset(0f, chartHeight + 4f))

                    val endLayout = textMeasurer.measure(
                        text = lastDate,
                        style = TextStyle(fontSize = 9.sp, color = Color.Gray)
                    )
                    drawText(endLayout, topLeft = Offset(chartWidth - endLayout.size.width, chartHeight + 4f))
                }
            }
        }
    }
}
