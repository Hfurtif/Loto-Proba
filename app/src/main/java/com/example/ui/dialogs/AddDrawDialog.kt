package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AddDrawDialog(
    onDismiss: () -> Unit,
    onConfirm: (date: String, n1: Int, n2: Int, n3: Int, n4: Int, n5: Int, name: String) -> Boolean
) {
    val today = remember {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.format(Calendar.getInstance().time)
    }

    var dateText by remember { mutableStateOf(today) }
    var nameText by remember { mutableStateOf("Tirage Quotidien") }
    var n1Text by remember { mutableStateOf("") }
    var n2Text by remember { mutableStateOf("") }
    var n3Text by remember { mutableStateOf("") }
    var n4Text by remember { mutableStateOf("") }
    var n5Text by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nouveau tirage", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Nom du tirage") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "5 numéros distincts (0 à 90) :",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val mod = Modifier.weight(1f)
                    OutlinedTextField(
                        value = n1Text,
                        onValueChange = { if (it.length <= 2) n1Text = it },
                        label = { Text("N1") },
                        modifier = mod,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = n2Text,
                        onValueChange = { if (it.length <= 2) n2Text = it },
                        label = { Text("N2") },
                        modifier = mod,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = n3Text,
                        onValueChange = { if (it.length <= 2) n3Text = it },
                        label = { Text("N3") },
                        modifier = mod,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = n4Text,
                        onValueChange = { if (it.length <= 2) n4Text = it },
                        label = { Text("N4") },
                        modifier = mod,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = n5Text,
                        onValueChange = { if (it.length <= 2) n5Text = it },
                        label = { Text("N5") },
                        modifier = mod,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
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
                    val nums = listOfNotNull(
                        n1Text.toIntOrNull(),
                        n2Text.toIntOrNull(),
                        n3Text.toIntOrNull(),
                        n4Text.toIntOrNull(),
                        n5Text.toIntOrNull()
                    )
                    if (nums.size != 5) {
                        errorMessage = "Veuillez saisir les 5 numéros."
                        return@Button
                    }
                    if (nums.any { it !in 0..90 }) {
                        errorMessage = "Chaque numéro doit être compris entre 0 et 90."
                        return@Button
                    }
                    if (nums.toSet().size != 5) {
                        errorMessage = "Les 5 numéros doivent être strictement distincts."
                        return@Button
                    }
                    val ok = onConfirm(dateText.trim(), nums[0], nums[1], nums[2], nums[3], nums[4], nameText.trim())
                    if (ok) {
                        onDismiss()
                    }
                }
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
