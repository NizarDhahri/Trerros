package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.GladeDefinition
import com.example.model.GroveUiState
import com.example.model.SpiritMessage
import com.example.ui.theme.BerryNotice
import com.example.ui.theme.CozyCream
import com.example.ui.theme.CozyCreamBorder
import com.example.ui.theme.CozyMint
import com.example.ui.theme.CozyMintDeep
import com.example.ui.theme.InkEspresso
import com.example.ui.theme.InkSoft
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternCore
import com.example.ui.theme.LanternGlow
import com.example.ui.theme.MeadowSage

/**
 * Minimal, reference-inspired HUD overlaid gently over the watercolor canvas.
 */
@Composable
fun CozyHudOverlay(
    glade: GladeDefinition,
    uiState: GroveUiState,
    onOpenSettings: () -> Unit,
    onOpenJournal: () -> Unit,
    onRecenter: () -> Unit,
    onDismissHint: () -> Unit,
    onDismissSpiritMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val litInGlade = glade.lanterns.count { it.id in uiState.litLanternIds }
    val totalInGlade = glade.lanterns.size

    Box(modifier = modifier.fillMaxSize()) {
        // Top minimal glade nameplate pill
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = CozyCream.copy(alpha = 0.94f),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.5.dp, CozyCreamBorder),
                shadowElevation = 3.dp,
                modifier = Modifier.testTag("glade_title_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(MeadowSage.copy(alpha = 0.22f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Spa,
                            contentDescription = null,
                            tint = CozyMintDeep,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = glade.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = InkEspresso,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$litInGlade of $totalInGlade lanterns lit",
                            style = MaterialTheme.typography.labelMedium,
                            color = InkSoft
                        )
                    }
                }
            }

            // Recenter button
            CozyMintIconButton(
                icon = Icons.Rounded.MyLocation,
                contentDescription = stringResource(R.string.cd_center_grove),
                onClick = onRecenter,
                containerColor = CozyCream.copy(alpha = 0.94f),
                borderColor = CozyCreamBorder,
                iconTint = InkEspresso,
                modifier = Modifier.testTag("recenter_button")
            )
        }

        // Gentle guidance banner & Spirit dialogue near bottom-center
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 16.dp, end = 16.dp, bottom = 86.dp)
                .widthIn(max = 520.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnimatedVisibility(
                visible = uiState.activeSpiritMessage != null,
                enter = fadeIn() + slideInVertically { it / 3 },
                exit = fadeOut() + slideOutVertically { it / 3 }
            ) {
                uiState.activeSpiritMessage?.let { msg ->
                    SpiritDialogueCard(
                        message = msg,
                        onDismiss = onDismissSpiritMessage
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.gentleHint != null && uiState.activeSpiritMessage == null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                uiState.gentleHint?.let { hint ->
                    Surface(
                        color = CozyCream.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.3.dp, CozyCreamBorder),
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(onClick = onDismissHint)
                            .testTag("gentle_hint_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(LanternAmber, CircleShape)
                            )
                            Text(
                                text = hint,
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkEspresso,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.cd_close_dialog),
                                tint = InkSoft,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bottom-left Cozy Grove inspired pill + mint tactile buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Dewlight counter cream pill
            Surface(
                color = CozyCream,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.8.dp, CozyCreamBorder),
                shadowElevation = 4.dp,
                modifier = Modifier.testTag("dewlight_counter_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(LanternCore, LanternAmber, LanternGlow)
                                ),
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = "${uiState.dewlight}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = InkEspresso
                    )
                    Text(
                        text = "Dew",
                        style = MaterialTheme.typography.labelMedium,
                        color = InkSoft
                    )
                }
            }

            // Mint-teal Settings button
            CozyMintIconButton(
                icon = Icons.Rounded.Settings,
                contentDescription = stringResource(R.string.cd_audio_settings),
                onClick = onOpenSettings,
                modifier = Modifier.testTag("settings_button")
            )

            // Mint-teal Sanctuary Journal button with optional rose-berry notice dot
            Box {
                CozyMintIconButton(
                    icon = Icons.Rounded.AutoStories,
                    contentDescription = stringResource(R.string.cd_sanctuary_journal),
                    onClick = onOpenJournal,
                    modifier = Modifier.testTag("journal_button")
                )
                if (uiState.newlyUnlockedNotice) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(13.dp)
                            .background(CozyCream, CircleShape)
                            .padding(2.dp)
                            .background(BerryNotice, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
private fun CozyMintIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = CozyMint,
    borderColor: Color = CozyMintDeep,
    iconTint: Color = CozyCream
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 500f),
        label = "hud_btn_scale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        color = containerColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.8.dp, borderColor),
        shadowElevation = if (isPressed) 1.dp else 4.dp,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .minimumInteractiveComponentSize()
            .size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Subtle warm top inner highlight rim
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(2.dp))
            )
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun SpiritDialogueCard(
    message: SpiritMessage,
    onDismiss: () -> Unit
) {
    Surface(
        color = CozyCream,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(2.dp, CozyCreamBorder),
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("spirit_dialogue_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(CozyMint, CircleShape)
                    )
                    Text(
                        text = message.spiritName,
                        style = MaterialTheme.typography.titleLarge,
                        color = InkEspresso
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("dismiss_spirit_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.cd_close_dialog),
                        tint = InkSoft
                    )
                }
            }
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyLarge,
                color = InkSoft
            )
            if (message.giftAwarded > 0) {
                Surface(
                    color = LanternGlow.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, LanternAmber.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(LanternAmber, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+${message.giftAwarded} Dewlight gift received",
                            style = MaterialTheme.typography.labelLarge,
                            color = InkEspresso
                        )
                    }
                }
            }
        }
    }
}
