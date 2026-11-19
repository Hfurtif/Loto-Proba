package com.lotoproba.app.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lotoproba.app.data.model.LotoSource
import com.lotoproba.app.ui.components.AppIcons

@Composable
fun AddSourceDialog(
    sourceToEdit: LotoSource?,
    onDismiss: () -> Unit,
    onSave: (name: String, country: String, url: String, preset: String) -> Unit
) {
    var name by remember(sourceToEdit) { mutableStateOf(sourceToEdit?.name ?: "") }
    var country by remember(sourceToEdit) { mutableStateOf(sourceToEdit?.country ?: "") }
    var url by remember(sourceToEdit) { mutableStateOf(sourceToEdit?.url ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(AppIcons.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (sourceToEdit == null) "Ajouter une source de résultats" else "Modifier la source",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Par défaut, l'application utilise le fichier CSV embarqué (assets/niger_2years.csv). Vous pouvez ajouter vos propres liens web (ex. page Google Sites ou tableau HTML) à synchroniser.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom de la source (ex: Loto Bénin 5/90)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Pays / Zone") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL de la page web (https://...)") },
                    placeholder = { Text("https://...") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedUrl = url.trim()
                    if (name.isBlank() || trimmedUrl.isBlank()) {
                        errorMessage = "Le nom et l'adresse de la source sont obligatoires."
                        return@Button
                    }
                    val preset = if (trimmedUrl.startsWith("asset://")) "CSV" else "AUTO"
                    onSave(name.trim(), country.trim(), trimmedUrl, preset)
                }
            ) {
                Text(if (sourceToEdit == null) "Ajouter" else "Enregistrer")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
