package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.MusicOff
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AudioSettings
import com.example.model.TimeOfDayMood
import com.example.ui.theme.BerryNotice
import com.example.ui.theme.CozyCream
import com.example.ui.theme.CozyCreamBorder
import com.example.ui.theme.CozyMint
import com.example.ui.theme.CozyMintDeep
import com.example.ui.theme.InkEspresso
import com.example.ui.theme.InkSoft
import com.example.ui.theme.LanternGlow
import com.example.ui.theme.MeadowSage
import com.example.ui.theme.ParchmentBase
import com.example.ui.theme.ParchmentWarm

/**
 * Quiet settings sheet with independent Music & SFX controls, Time-of-Day atmosphere selection,
 * and gentle grove reset.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioSettingsSheet(
    audio: AudioSettings,
    currentMood: TimeOfDayMood,
    onMusicVolumeChange: (Float) -> Unit,
    onToggleMusicMute: () -> Unit,
    onSfxVolumeChange: (Float) -> Unit,
    onToggleSfxMute: () -> Unit,
    onSelectMood: (TimeOfDayMood) -> Unit,
    onResetSanctuary: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var confirmReset by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CozyCream,
        contentColor = InkEspresso
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .testTag("audio_settings_sheet"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Sound & Atmosphere",
                style = MaterialTheme.typography.headlineMedium,
                color = InkEspresso
            )

            // Music channel card
            Surface(
                color = ParchmentBase,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.2.dp, CozyCreamBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Ambient Music",
                                style = MaterialTheme.typography.titleMedium,
                                color = InkEspresso
                            )
                            Text(
                                text = if (audio.isMusicMuted) "Muted" else "${(audio.musicVolume * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkSoft
                            )
                        }
                        IconButton(
                            onClick = onToggleMusicMute,
                            modifier = Modifier
                                .background(
                                    color = if (audio.isMusicMuted) ParchmentWarm else CozyMint.copy(alpha = 0.16f),
                                    shape = CircleShape
                                )
                                .testTag("music_mute_button")
                        ) {
                            Icon(
                                imageVector = if (audio.isMusicMuted) Icons.Rounded.MusicOff else Icons.Rounded.MusicNote,
                                contentDescription = stringResource(R.string.cd_mute_music),
                                tint = if (audio.isMusicMuted) InkSoft else CozyMintDeep
                            )
                        }
                    }
                    Slider(
                        value = if (audio.isMusicMuted) 0f else audio.musicVolume,
                        onValueChange = onMusicVolumeChange,
                        colors = SliderDefaults.colors(
                            thumbColor = CozyMintDeep,
                            activeTrackColor = CozyMint,
                            inactiveTrackColor = ParchmentWarm
                        ),
                        modifier = Modifier.testTag("music_volume_slider")
                    )
                }
            }

            // SFX channel card
            Surface(
                color = ParchmentBase,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.2.dp, CozyCreamBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Chimes & Nature Sounds",
                                style = MaterialTheme.typography.titleMedium,
                                color = InkEspresso
                            )
                            Text(
                                text = if (audio.isSfxMuted) "Muted" else "${(audio.sfxVolume * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkSoft
                            )
                        }
                        IconButton(
                            onClick = onToggleSfxMute,
                            modifier = Modifier
                                .background(
                                    color = if (audio.isSfxMuted) ParchmentWarm else CozyMint.copy(alpha = 0.16f),
                                    shape = CircleShape
                                )
                                .testTag("sfx_mute_button")
                        ) {
                            Icon(
                                imageVector = if (audio.isSfxMuted) {
                                    Icons.AutoMirrored.Rounded.VolumeOff
                                } else {
                                    Icons.AutoMirrored.Rounded.VolumeUp
                                },
                                contentDescription = stringResource(R.string.cd_mute_sfx),
                                tint = if (audio.isSfxMuted) InkSoft else CozyMintDeep
                            )
                        }
                    }
                    Slider(
                        value = if (audio.isSfxMuted) 0f else audio.sfxVolume,
                        onValueChange = onSfxVolumeChange,
                        colors = SliderDefaults.colors(
                            thumbColor = CozyMintDeep,
                            activeTrackColor = CozyMint,
                            inactiveTrackColor = ParchmentWarm
                        ),
                        modifier = Modifier.testTag("sfx_volume_slider")
                    )
                }
            }

            // Atmosphere Time-of-Day tint
            Text(
                text = "Grove Lighting",
                style = MaterialTheme.typography.titleLarge,
                color = InkEspresso
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TimeOfDayMood.entries.forEach { mood ->
                    val selected = mood == currentMood
                    Surface(
                        onClick = { onSelectMood(mood) },
                        color = if (selected) MeadowSage.copy(alpha = 0.24f) else ParchmentBase,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            width = if (selected) 2.dp else 1.2.dp,
                            color = if (selected) CozyMintDeep else CozyCreamBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mood_option_${mood.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        color = if (selected) LanternGlow.copy(alpha = 0.55f) else ParchmentWarm,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.WbTwilight,
                                    contentDescription = null,
                                    tint = if (selected) CozyMintDeep else InkSoft
                                )
                            }
                            Column {
                                Text(
                                    text = mood.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = InkEspresso
                                )
                                Text(
                                    text = mood.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = InkSoft
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Gentle Reset Option
            if (!confirmReset) {
                OutlinedButton(
                    onClick = { confirmReset = true },
                    border = BorderStroke(1.2.dp, CozyCreamBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reset_request_button")
                ) {
                    Text("Begin a Fresh Meadow", color = InkSoft)
                }
            } else {
                Surface(
                    color = ParchmentBase,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, BerryNotice.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Return all glades to quiet parchment sketches?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkEspresso
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = { confirmReset = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Keep Progress", color = InkSoft)
                            }
                            OutlinedButton(
                                onClick = {
                                    confirmReset = false
                                    onResetSanctuary()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BerryNotice),
                                border = BorderStroke(1.4.dp, BerryNotice),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reset_confirm_button")
                            ) {
                                Text("Reset")
                            }
                        }
                    }
                }
            }
        }
    }
}
