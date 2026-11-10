package com.example.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
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
import com.example.data.model.LotoSource

@Composable
fun AddSourceDialog(
    sourceToEdit: LotoSource?,
    onDismiss: () -> Unit,
    onSave: (name: String, country: String, url: String, preset: String) -> Unit
) {
    var name by remember { mutableStateOf(sourceToEdit?.name ?: "") }
    var country by remember { mutableStateOf(sourceToEdit?.country ?: "") }
    var url by remember { mutableStateOf(sourceToEdit?.url ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (sourceToEdit == null) "Ajouter un lien de jeu / pays" else "Modifier le lien de la source",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Indiquez l'URL de la page web contenant les résultats du loto (5/90). L'application ira lire automatiquement cette page chaque jour.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom du jeu (ex: Loto Bénin Star)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Pays (ex: Bénin, Côte d'Ivoire, etc.)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL de la page des résultats") },
                    placeholder = { Text("https://votresite.com/resultats") },
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
                    if (name.isBlank() || url.isBlank()) {
                        errorMessage = "Le nom et l'URL sont obligatoires."
                        return@Button
                    }
                    onSave(name.trim(), country.trim(), url.trim(), "AUTO")
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
