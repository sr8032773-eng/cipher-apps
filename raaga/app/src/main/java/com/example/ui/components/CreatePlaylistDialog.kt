package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.RaagaAmber
import com.example.ui.theme.RaagaDarkSurface
import com.example.ui.theme.RaagaDarkSurfaceHighlight
import com.example.ui.theme.RaagaTextPrimary
import com.example.ui.theme.RaagaTextSecondary

@Composable
fun CreatePlaylistDialog(
    onCreate: (name: String, description: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = RaagaDarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_create_playlist")
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "New Playlist",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = RaagaTextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Playlist Name") },
                    placeholder = { Text("e.g. Monsoon Rain Vibes") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = RaagaTextPrimary,
                        unfocusedTextColor = RaagaTextPrimary,
                        focusedBorderColor = RaagaAmber,
                        unfocusedBorderColor = RaagaDarkSurfaceHighlight,
                        focusedLabelColor = RaagaAmber,
                        unfocusedLabelColor = RaagaTextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_playlist_name")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    placeholder = { Text("e.g. Sitar and morning flute melodies") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = RaagaTextPrimary,
                        unfocusedTextColor = RaagaTextPrimary,
                        focusedBorderColor = RaagaAmber,
                        unfocusedBorderColor = RaagaDarkSurfaceHighlight,
                        focusedLabelColor = RaagaAmber,
                        unfocusedLabelColor = RaagaTextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = RaagaTextSecondary)
                    }
                    TextButton(
                        onClick = {
                            if (name.isNotBlank()) {
                                onCreate(name, description)
                            }
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.testTag("btn_confirm_create_playlist")
                    ) {
                        Text(
                            "Create",
                            color = if (name.isNotBlank()) RaagaAmber else RaagaTextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
