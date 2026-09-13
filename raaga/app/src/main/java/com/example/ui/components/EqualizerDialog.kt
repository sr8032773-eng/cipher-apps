package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.audio.EqualizerPreset
import com.example.ui.theme.RaagaAmber
import com.example.ui.theme.RaagaDarkSurface
import com.example.ui.theme.RaagaDarkSurfaceHighlight
import com.example.ui.theme.RaagaDarkSurfaceVariant
import com.example.ui.theme.RaagaTextPrimary
import com.example.ui.theme.RaagaTextSecondary
import com.example.ui.theme.RaagaViolet

@Composable
fun EqualizerDialog(
    currentPreset: EqualizerPreset,
    onSelectPreset: (EqualizerPreset) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = RaagaDarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_equalizer")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(RaagaViolet.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = RaagaViolet
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Sound Equalizer & Presets",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = RaagaTextPrimary
                        )
                        Text(
                            text = "Acoustic modeling for ragas & instruments",
                            style = MaterialTheme.typography.bodySmall,
                            color = RaagaTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Band sliders preview (Bass, Mid, Treble)
                EqBandPreview(label = "Bass / Tanpura Drone", gain = currentPreset.bassGain)
                Spacer(modifier = Modifier.height(10.dp))
                EqBandPreview(label = "Mids / Sitar & Flute", gain = currentPreset.midGain)
                Spacer(modifier = Modifier.height(10.dp))
                EqBandPreview(label = "Treble / Percussion Snap", gain = currentPreset.trebleGain)

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Acoustic Tuning Presets",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = RaagaTextSecondary,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Presets list
                EqualizerPreset.entries.forEach { preset ->
                    val isSelected = preset == currentPreset
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) RaagaAmber.copy(alpha = 0.15f) else RaagaDarkSurfaceVariant)
                            .clickable { onSelectPreset(preset) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = preset.displayName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) RaagaAmber else RaagaTextPrimary
                        )

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = RaagaAmber,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Done", color = RaagaAmber, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EqBandPreview(label: String, gain: Float) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = RaagaTextSecondary)
            Text(
                text = "${((gain - 1.0f) * 6).toInt()} dB",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = RaagaAmber
            )
        }
        Slider(
            value = gain,
            onValueChange = {},
            valueRange = 0.6f..1.8f,
            enabled = false,
            colors = SliderDefaults.colors(
                disabledActiveTrackColor = RaagaAmber,
                disabledInactiveTrackColor = RaagaDarkSurfaceHighlight,
                disabledThumbColor = RaagaAmber
            ),
            modifier = Modifier.height(24.dp)
        )
    }
}
