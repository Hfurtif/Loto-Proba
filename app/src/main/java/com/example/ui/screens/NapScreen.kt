package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stats.DailyNapPackage
import com.example.stats.NapItem
import com.example.ui.components.BallVariant
import com.example.ui.components.LotoBall

@Composable
fun NapScreen(
    napPackage: DailyNapPackage,
    onSaveForTracking: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // En-tête explicatif clair sur les règles NAP 1, NAP 2, NAP 3
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Guide des Pronostics du Jour",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1E293B)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "• NAP 1 (N1) : Vous choisissez 1 seul chiffre. S'il sort parmi les 5 tirés, vous gagnez !\n" +
                               "• NAP 2 (N2) : Vous choisissez 2 chiffres. Les 2 doivent sortir ensemble.\n" +
                               "• NAP 3 (N3) : Vous choisissez 3 chiffres. Les 3 doivent impérativement sortir ensemble.",
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // ==========================================
        // 1. SECTION NAP 1 (N1 - Chiffre Sûr / Banker)
        // ==========================================
        item {
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
                                text = "LE CHIFFRE SÛR (Banker)",
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
                                text = "Confiance ${napPackage.mainBankerNap1.confidencePercent}%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF92400E)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mise en avant géante du chiffre sûr
                    val bankerNum = napPackage.mainBankerNap1.numbers.firstOrNull() ?: 0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("NAP 1", bankerNum.toString()))
                                Toast.makeText(context, "Numéro $bankerNum copié !", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copier", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Chiffres d'appui (alternatives NAP 1)
                    if (napPackage.alternativeNap1.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "2nd choix Banker : ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            napPackage.alternativeNap1.forEach { alt ->
                                LotoBall(number = alt, size = 32.dp, variant = BallVariant.DEFAULT)
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. SECTION NAP 2 (N2 - Les 2 Chiffres Sûrs)
        // ==========================================
        item {
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
                    text = "Les 2 Chiffres Sûrs (Top Paires)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        items(napPackage.topNap2Pairs) { pair ->
            NapPairCard(pair = pair)
        }

        // ==========================================
        // 3. SECTION NAP 3 (N3 - Les 3 Chiffres Sûrs)
        // ==========================================
        item {
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
                    text = "Les 3 Chiffres Sûrs (Trios Direct)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        items(napPackage.topNap3Trios) { trio ->
            NapTrioCard(trio = trio)
        }

        // ==========================================
        // 4. SECTION PERM 5 (Grille Complète 5 Numéros)
        // ==========================================
        item {
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
                            text = "PERM 5 (Grille Complète)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                        Text(
                            text = "Les 5 réunis",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
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

        // Bouton d'enregistrement pour vérification
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onSaveForTracking,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Enregistrer ces pronostics pour vérifier le tirage de demain",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                pair.numbers.forEachIndexed { idx, num ->
                    LotoBall(number = num, size = 42.dp, variant = BallVariant.DEFAULT)
                    if (idx < pair.numbers.size - 1) {
                        Text(
                            text = "+",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 6.dp),
                            color = Color.Gray
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
                    Toast.makeText(context, "Paire ${pair.numbers.joinToString("-")} copiée !", Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copier", tint = Color.Gray, modifier = Modifier.size(18.dp))
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                trio.numbers.forEachIndexed { idx, num ->
                    LotoBall(number = num, size = 38.dp, variant = BallVariant.DEFAULT)
                    if (idx < trio.numbers.size - 1) {
                        Text(
                            text = "+",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 4.dp),
                            color = Color.Gray
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
                    Toast.makeText(context, "Trio ${trio.numbers.joinToString("-")} copié !", Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copier", tint = Color.Gray, modifier = Modifier.size(18.dp))
            }
        }
    }
}
