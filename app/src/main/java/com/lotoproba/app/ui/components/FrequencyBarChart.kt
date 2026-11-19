package com.lotoproba.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lotoproba.app.stats.NumberStat

@Composable
fun FrequencyBarChart(
    stats: List<NumberStat>,
    totalDraws: Int,
    selectedNumber: Int?,
    onSelectNumber: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var rangeFilter by remember { mutableIntStateOf(0) }
    val textMeasurer = rememberTextMeasurer()

    val ranges = remember {
        listOf(
            Pair("0 à 17", 0..17),
            Pair("18 à 35", 18..35),
            Pair("36 à 53", 36..53),
            Pair("54 à 71", 54..71),
            Pair("72 à 90", 72..90),
            Pair("Tous (0-90)", 0..90)
        )
    }

    val currentRange = ranges[rangeFilter].second
    val filteredStats = remember(stats, rangeFilter) {
        stats.filter { it.number in currentRange }
    }
    val maxCount = (stats.maxOfOrNull { it.count } ?: 10).coerceAtLeast(5)
    val expectedCount = if (totalDraws > 0) (totalDraws * 5.0) / 91.0 else 0.0

    Column(modifier = modifier.fillMaxWidth()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(ranges, key = { _, pair -> pair.first }) { index, pair ->
                FilterChip(
                    selected = rangeFilter == index,
                    onClick = { rangeFilter = index },
                    label = { Text(pair.first, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .height(3.dp)
                        .background(Color(0xFFEF4444))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Moyenne théorique (${"%.1f".format(expectedCount)})",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Max observé : $maxCount",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(8.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(filteredStats) {
                        detectTapGestures { offset ->
                            if (filteredStats.isEmpty()) return@detectTapGestures
                            val barWidth = size.width.toFloat() / filteredStats.size
                            val tappedIndex = (offset.x / barWidth).toInt().coerceIn(0, filteredStats.size - 1)
                            onSelectNumber(filteredStats[tappedIndex].number)
                        }
                    }
            ) {
                val chartHeight = size.height - 35f
                val countItems = filteredStats.size
                if (countItems == 0) return@Canvas
                val slotWidth = size.width / countItems
                val barWidth = (slotWidth * 0.75f).coerceAtLeast(3f)

                if (expectedCount > 0 && maxCount > 0) {
                    val expectedY = chartHeight * (1f - (expectedCount.toFloat() / maxCount.toFloat()))
                    drawLine(
                        color = Color(0xFFEF4444),
                        start = Offset(0f, expectedY),
                        end = Offset(size.width, expectedY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }

                filteredStats.forEachIndexed { index, stat ->
                    val xCenter = (index * slotWidth) + (slotWidth / 2f)
                    val barHeight = if (maxCount > 0) {
                        (stat.count.toFloat() / maxCount.toFloat()) * chartHeight
                    } else 0f

                    val isSelected = selectedNumber == stat.number
                    val barColor = when {
                        isSelected -> Color(0xFFF59E0B)
                        stat.count >= expectedCount -> Color(0xFF10B981)
                        else -> Color(0xFF3B82F6)
                    }

                    val left = xCenter - (barWidth / 2f)
                    val top = chartHeight - barHeight

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4f, 4f)
                    )

                    if (countItems <= 20 || index % 5 == 0 || isSelected) {
                        val numText = stat.number.toString().padStart(2, '0')
                        val textLayout = textMeasurer.measure(
                            text = numText,
                            style = TextStyle(
                                fontSize = if (countItems <= 20) 10.sp else 8.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFFF59E0B) else Color.Gray
                            )
                        )
                        drawText(
                            textLayoutResult = textLayout,
                            topLeft = Offset(xCenter - (textLayout.size.width / 2f), chartHeight + 6f)
                        )
                    }

                    if (isSelected || (countItems <= 18 && stat.count > 0)) {
                        val countText = stat.count.toString()
                        val textLayout = textMeasurer.measure(
                            text = countText,
                            style = TextStyle(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFFF59E0B) else Color.White
                            )
                        )
                        drawText(
                            textLayoutResult = textLayout,
                            topLeft = Offset(xCenter - (textLayout.size.width / 2f), (top - 14f).coerceAtLeast(0f))
                        )
                    }
                }
            }
        }
    }
}
