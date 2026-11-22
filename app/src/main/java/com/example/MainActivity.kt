package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.GladeCatalog
import com.example.ui.canvas.GroveCanvas
import com.example.ui.components.AudioSettingsSheet
import com.example.ui.components.CozyHudOverlay
import com.example.ui.components.SanctuaryJournalSheet
import com.example.ui.theme.WillowlightTheme
import com.example.viewmodel.GroveViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WillowlightTheme {
                WillowlightScreen()
            }
        }
    }
}

@Composable
fun WillowlightScreen(
    viewModel: GroveViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val glade = remember(uiState.activeGladeId) {
        GladeCatalog.gladeById(uiState.activeGladeId)
    }
    var recenterTrigger by remember { mutableIntStateOf(0) }

    // Pause and resume gameplay simulation & ambient audio cleanly with app lifecycle
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.onAppResumed()
                Lifecycle.Event.ON_PAUSE -> viewModel.onAppPaused()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Back gesture closes open overlays cleanly
    BackHandler(
        enabled = uiState.isJournalOpen || uiState.isSettingsOpen || uiState.activeSpiritMessage != null
    ) {
        when {
            uiState.isJournalOpen -> viewModel.closeJournal()
            uiState.isSettingsOpen -> viewModel.closeSettings()
            uiState.activeSpiritMessage != null -> viewModel.dismissSpiritMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GroveCanvas(
            glade = glade,
            uiState = uiState,
            recenterTrigger = recenterTrigger,
            onWeaveLantern = viewModel::weaveLanternConnection,
            onTapLantern = viewModel::tapDormantLantern,
            onGatherFireflies = { pos -> viewModel.gatherFirefliesNear(pos) },
            onTapProp = viewModel::tapWorldProp,
            onTapSpirit = viewModel::tapSpirit,
            onWalkKeeper = viewModel::walkKeeperTo,
            onThreadShimmer = viewModel::onThreadShimmerTick
        )

        CozyHudOverlay(
            glade = glade,
            uiState = uiState,
            onOpenSettings = viewModel::openSettings,
            onOpenJournal = viewModel::openJournal,
            onRecenter = { recenterTrigger++ },
            onDismissHint = viewModel::dismissHint,
            onDismissSpiritMessage = viewModel::dismissSpiritMessage
        )

        if (uiState.isJournalOpen) {
            SanctuaryJournalSheet(
                uiState = uiState,
                onSelectGlade = viewModel::selectGlade,
                onUnlockOrToggleKeepsake = viewModel::unlockOrToggleKeepsake,
                onDismiss = viewModel::closeJournal
            )
        }

        if (uiState.isSettingsOpen) {
            AudioSettingsSheet(
                audio = uiState.audio,
                currentMood = uiState.mood,
                onMusicVolumeChange = viewModel::setMusicVolume,
                onToggleMusicMute = viewModel::toggleMusicMute,
                onSfxVolumeChange = viewModel::setSfxVolume,
                onToggleSfxMute = viewModel::toggleSfxMute,
                onSelectMood = viewModel::setMood,
                onResetSanctuary = viewModel::resetSanctuary,
                onDismiss = viewModel::closeSettings
            )
        }
    }
}
