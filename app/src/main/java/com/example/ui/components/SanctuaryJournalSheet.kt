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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.GladeCatalog
import com.example.model.GladeId
import com.example.model.GroveUiState
import com.example.model.KeepsakeItem
import com.example.ui.theme.CozyCream
import com.example.ui.theme.CozyCreamBorder
import com.example.ui.theme.CozyMint
import com.example.ui.theme.CozyMintDeep
import com.example.ui.theme.InkEspresso
import com.example.ui.theme.InkSoft
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternGlow
import com.example.ui.theme.MeadowSage
import com.example.ui.theme.ParchmentBase
import com.example.ui.theme.ParchmentWarm

/**
 * Serene Sanctuary Journal sheet for exploring the four glades and placing cozy keepsakes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SanctuaryJournalSheet(
    uiState: GroveUiState,
    onSelectGlade: (GladeId) -> Unit,
    onUnlockOrToggleKeepsake: (KeepsakeItem) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val totalLit = uiState.litLanternIds.size
    val totalLanterns = GladeCatalog.totalLanternCount
    val progressFraction = (totalLit.toFloat() / totalLanterns.coerceAtLeast(1)).coerceIn(0f, 1f)

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
                .testTag("sanctuary_journal_sheet"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Sanctuary Journal",
                style = MaterialTheme.typography.headlineMedium,
                color = InkEspresso
            )

            // Overall gentle grove bloom bar
            Surface(
                color = ParchmentBase,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.3.dp, CozyCreamBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Grove Restoration",
                            style = MaterialTheme.typography.titleMedium,
                            color = InkEspresso
                        )
                        Surface(
                            color = CozyCream,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CozyCreamBorder)
                        ) {
                            Text(
                                text = "$totalLit / $totalLanterns lanterns",
                                style = MaterialTheme.typography.labelLarge,
                                color = CozyMintDeep,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        color = CozyMint,
                        trackColor = ParchmentWarm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }

            // Glades Section
            Text(
                text = "Quiet Glades",
                style = MaterialTheme.typography.titleLarge,
                color = InkEspresso
            )

            GladeCatalog.glades.forEach { glade ->
                val isUnlocked = totalLit >= glade.unlockTotalLanterns
                val isCurrent = glade.id == uiState.activeGladeId
                val gladeLit = glade.lanterns.count { it.id in uiState.litLanternIds }

                Surface(
                    onClick = { if (isUnlocked) onSelectGlade(glade.id) },
                    enabled = isUnlocked,
                    color = if (isCurrent) MeadowSage.copy(alpha = 0.22f) else ParchmentBase,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(
                        width = if (isCurrent) 2.dp else 1.2.dp,
                        color = if (isCurrent) CozyMintDeep else CozyCreamBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("glade_card_${glade.id.storageKey}")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    color = if (isUnlocked) CozyMint.copy(alpha = 0.16f) else ParchmentWarm,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isUnlocked) Icons.Rounded.Park else Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = if (isUnlocked) CozyMintDeep else InkSoft,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = glade.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = InkEspresso
                            )
                            Text(
                                text = if (isUnlocked) {
                                    "$gladeLit / ${glade.lanterns.size} lanterns lit • ${glade.subtitle}"
                                } else {
                                    "Unlocks when ${glade.unlockTotalLanterns} lanterns shine across the grove"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkSoft
                            )
                        }
                        if (isCurrent) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Current glade",
                                tint = CozyMintDeep
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Keepsakes Section
            Text(
                text = "Cozy Keepsakes",
                style = MaterialTheme.typography.titleLarge,
                color = InkEspresso
            )

            GladeCatalog.keepsakes.forEach { item ->
                val isUnlocked = item.id in uiState.unlockedKeepsakeIds
                val isActive = item.id in uiState.activeKeepsakeIds
                val canAfford = uiState.dewlight >= item.cost

                Surface(
                    color = ParchmentBase,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.2.dp, CozyCreamBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("keepsake_card_${item.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (isUnlocked) LanternGlow.copy(alpha = 0.48f) else ParchmentWarm,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Spa,
                                contentDescription = null,
                                tint = if (isUnlocked) LanternAmber else InkSoft,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = InkEspresso
                            )
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkSoft
                            )
                        }
                        if (isUnlocked) {
                            OutlinedButton(
                                onClick = { onUnlockOrToggleKeepsake(item) },
                                border = BorderStroke(1.4.dp, CozyMintDeep),
                                modifier = Modifier.testTag("keepsake_toggle_${item.id}")
                            ) {
                                Text(
                                    text = if (isActive) "Placed" else "Place",
                                    color = CozyMintDeep
                                )
                            }
                        } else {
                            Button(
                                onClick = { onUnlockOrToggleKeepsake(item) },
                                enabled = canAfford,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CozyMint,
                                    contentColor = CozyCream
                                ),
                                modifier = Modifier.testTag("keepsake_unlock_${item.id}")
                            ) {
                                Text("${item.cost} Dew")
                            }
                        }
                    }
                }
            }
        }
    }
}
