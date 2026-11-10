package com.example.ui.dialogs

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ManualImportDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    val sampleExample = """
2026-11-07 : 12, 45, 68, 89, 05
2026-11-06 : 02, 19, 44, 73, 90
2026-11-05 ; 14 ; 27 ; 33 ; 56 ; 82
    """.trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ContentPaste, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Import manuel / Collage CSV", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Collez ici les lignes de tirages extraites d'un site ou d'un fichier CSV. Chaque ligne doit comporter une date et 5 numéros distincts entre 0 et 90.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp)
                ) {
                    Column {
                        Text("Formats acceptés :", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("• YYYY-MM-DD : N1, N2, N3, N4, N5", fontSize = 10.sp)
                        Text("• DD/MM/YYYY ; N1 ; N2 ; N3 ; N4 ; N5", fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Texte des tirages ou CSV") },
                    placeholder = { Text(sampleExample, fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    maxLines = 15
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = { inputText = sampleExample },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Charger un exemple", fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onImport(inputText)
                        onDismiss()
                    }
                },
                enabled = inputText.isNotBlank()
            ) {
                Text("Analyser et importer")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
