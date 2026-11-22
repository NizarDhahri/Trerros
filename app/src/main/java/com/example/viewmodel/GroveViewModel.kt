package com.example.viewmodel

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AmbientAudioEngine
import com.example.audio.AmbientAudioEngine.SfxType
import com.example.data.GamePreferencesRepository
import com.example.model.FireflyMote
import com.example.model.GladeCatalog
import com.example.model.GladeDefinition
import com.example.model.GladeId
import com.example.model.GlowParticle
import com.example.model.GroveUiState
import com.example.model.KeepsakeItem
import com.example.model.LanternNode
import com.example.model.ParticleKind
import com.example.model.PropType
import com.example.model.SpiritMessage
import com.example.model.TimeOfDayMood
import com.example.model.WatercolorRipple
import com.example.model.WorldProp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

/**
 * Main ViewModel coordinating Willowlight's serene simulation loop, touch interactions,
 * offline DataStore persistence, and procedural ambient audio engine.
 */
class GroveViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GamePreferencesRepository(application.applicationContext)
    val audioEngine = AmbientAudioEngine()

    private val _uiState = MutableStateFlow(
        GroveUiState(
            litLanternIds = GladeCatalog.defaultLitLanternIds,
            fireflies = spawnInitialFireflies(GladeCatalog.gladeById(GladeId.POSTHOUSE))
        )
    )
    val uiState: StateFlow<GroveUiState> = _uiState.asStateFlow()

    private var simulationJob: Job? = null
    private var isAppPaused = false
    private var nextId = 100L
    private var lastSweepChimeTime = 0L
    private var lastSpiritGreetingIdx = 0

    init {
        viewModelScope.launch {
            val snapshot = repository.snapshotFlow.first()
            val glade = GladeCatalog.gladeById(snapshot.activeGladeId)
            _uiState.update {
                it.copy(
                    activeGladeId = snapshot.activeGladeId,
                    dewlight = snapshot.dewlight,
                    litLanternIds = snapshot.litLanternIds,
                    unlockedKeepsakeIds = snapshot.unlockedKeepsakeIds,
                    activeKeepsakeIds = snapshot.activeKeepsakeIds,
                    claimedSpiritGiftIds = snapshot.claimedSpiritGiftIds,
                    keeperPosition = glade.hearthCenter,
                    keeperTarget = glade.hearthCenter,
                    fireflies = spawnInitialFireflies(glade),
                    mood = snapshot.mood,
                    audio = snapshot.audio
                )
            }
            audioEngine.updateSettings(snapshot.audio)
            if (!isAppPaused) {
                audioEngine.start()
                startSimulationLoop()
            }
        }
    }

    fun onAppResumed() {
        isAppPaused = false
        audioEngine.resume()
        startSimulationLoop()
    }

    fun onAppPaused() {
        isAppPaused = true
        simulationJob?.cancel()
        simulationJob = null
        audioEngine.pause()
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
        audioEngine.release()
    }

    fun currentGlade(): GladeDefinition = GladeCatalog.gladeById(_uiState.value.activeGladeId)

    /**
     * Connects a lit lantern to an adjacent dormant lantern, blooming watercolor across the glade.
     */
    fun weaveLanternConnection(fromLanternId: String, toLanternId: String): Boolean {
        val state = _uiState.value
        val glade = GladeCatalog.gladeById(state.activeGladeId)
        val fromNode = glade.lanterns.firstOrNull { it.id == fromLanternId } ?: return false
        val toNode = glade.lanterns.firstOrNull { it.id == toLanternId } ?: return false

        if (fromNode.id !in state.litLanternIds || toNode.id in state.litLanternIds) return false
        if (toNode.id !in fromNode.connectedToIds && fromNode.id !in toNode.connectedToIds) return false

        return lightDormantLantern(toNode, glade)
    }

    /**
     * Allows tapping a dormant lantern directly if an adjacent lantern is already lit.
     */
    fun tapDormantLantern(lantern: LanternNode) {
        val state = _uiState.value
        val glade = GladeCatalog.gladeById(state.activeGladeId)
        if (lantern.id in state.litLanternIds) {
            // Gentle chime when tapping an already-lit lantern
            audioEngine.playSfx(SfxType.THREAD_SHIMMER, panFor(lantern.position, glade))
            emitBurst(lantern.position, 8, ParticleKind.EMBER)
            return
        }

        val hasLitNeighbor = lantern.connectedToIds.any { it in state.litLanternIds } ||
            glade.lanterns.any { it.id in state.litLanternIds && lantern.id in it.connectedToIds }

        if (!hasLitNeighbor) {
            showGentleHint("Light a closer lantern first to weave a thread here.")
            audioEngine.playSfx(SfxType.UI_TAP)
            return
        }

        lightDormantLantern(lantern, glade)
    }

    private fun lightDormantLantern(toNode: LanternNode, glade: GladeDefinition): Boolean {
        val state = _uiState.value
        if (state.dewlight < toNode.dewCost) {
            val needed = toNode.dewCost - state.dewlight
            showGentleHint("Gather $needed more Dewlight from drifting fireflies or trees.")
            audioEngine.playSfx(SfxType.UI_TAP)
            return false
        }

        val prevLitCount = state.litLanternIds.size
        val updatedLit = state.litLanternIds + toNode.id
        val updatedDew = state.dewlight - toNode.dewCost
        val newLitCount = updatedLit.size
        val unlockedNewGlade = GladeCatalog.glades.any {
            it.unlockTotalLanterns in (prevLitCount + 1)..newLitCount
        }

        _uiState.update {
            it.copy(
                dewlight = updatedDew,
                litLanternIds = updatedLit,
                gentleHint = if (unlockedNewGlade) {
                    "A new quiet glade has opened in your Sanctuary Journal."
                } else null,
                newlyUnlockedNotice = it.newlyUnlockedNotice || unlockedNewGlade,
                ripples = it.ripples + WatercolorRipple(
                    id = nextId++,
                    center = toNode.position,
                    progress = 0f,
                    maxRadius = toNode.bloomRadius,
                    isWarmBloom = true
                )
            )
        }

        emitBurst(toNode.position, 22, ParticleKind.SPARKLE)
        emitBurst(toNode.position, 10, ParticleKind.PETAL)
        audioEngine.playSfx(SfxType.LANTERN_BLOOM, panFor(toNode.position, glade))
        persistProgress()
        return true
    }

    /**
     * Collects any firefly motes near the player's touch or swipe path.
     */
    fun gatherFirefliesNear(worldPos: Offset, touchRadius: Float = 66f): Int {
        val state = _uiState.value
        val glade = GladeCatalog.gladeById(state.activeGladeId)
        val collected = state.fireflies.filter {
            hypot(it.position.x - worldPos.x, it.position.y - worldPos.y) <= touchRadius
        }
        if (collected.isEmpty()) return 0

        val collectedIds = collected.map { it.id }.toSet()
        val bonus = collected.sumOf { if (it.isGoldenFruit) 2 else 1 }

        _uiState.update { cur ->
            val remaining = cur.fireflies.filterNot { it.id in collectedIds }
            val replenished = if (remaining.size < 9) {
                remaining + List(collected.size) { spawnRandomFirefly(glade) }
            } else {
                remaining
            }
            cur.copy(
                dewlight = cur.dewlight + bonus,
                fireflies = replenished
            )
        }

        collected.forEach { mote ->
            emitBurst(mote.position, 9, ParticleKind.SPARKLE)
        }

        val now = System.currentTimeMillis()
        if (now - lastSweepChimeTime > 95L) {
            lastSweepChimeTime = now
            audioEngine.playSfx(SfxType.DEW_CHIME, panFor(worldPos, glade))
        }
        persistProgress()
        return bonus
    }

    /**
     * Handles tapping on trees, cottages, benches, and ponds for gentle feedback.
     */
    fun tapWorldProp(prop: WorldProp) {
        val state = _uiState.value
        val glade = GladeCatalog.gladeById(state.activeGladeId)
        val isAreaLit = isWorldPointLit(prop.position, glade, state.litLanternIds)

        if (!isAreaLit) {
            showGentleHint("Weave lantern light nearby to awaken this spot.")
            audioEngine.playSfx(SfxType.UI_TAP)
            return
        }

        val now = System.currentTimeMillis()
        when (prop.type) {
            PropType.ORANGE_TREE, PropType.PEACH_TREE, PropType.WILLOW_TREE -> {
                _uiState.update {
                    val spawned = if (it.fireflies.size < 14) {
                        it.fireflies + FireflyMote(
                            id = nextId++,
                            position = prop.position + Offset(Random.nextFloat() * 60f - 30f, 35f),
                            velocity = Offset(Random.nextFloat() * 14f - 7f, 12f),
                            phase = Random.nextFloat() * 6.28f,
                            radius = 11f,
                            isGoldenFruit = true
                        )
                    } else it.fireflies
                    it.copy(
                        propShakeTimes = it.propShakeTimes + (prop.id to now),
                        fireflies = spawned
                    )
                }
                emitBurst(prop.position - Offset(0f, 20f), 10, ParticleKind.PETAL)
                emitBurst(prop.position - Offset(0f, 15f), 6, ParticleKind.LEAF)
                audioEngine.playSfx(SfxType.TREE_RUSTLE, panFor(prop.position, glade))
            }
            PropType.COZY_BENCH -> {
                _uiState.update {
                    it.copy(
                        keeperTarget = prop.position + Offset(0f, 18f),
                        keeperSitting = true,
                        ripples = it.ripples + WatercolorRipple(
                            id = nextId++,
                            center = prop.position,
                            progress = 0f,
                            maxRadius = 85f,
                            isWarmBloom = false
                        )
                    )
                }
                audioEngine.playSfx(SfxType.UI_TAP, panFor(prop.position, glade))
            }
            PropType.POSTHOUSE_COTTAGE, PropType.TEA_GAZEBO, PropType.OBSERVATORY_TREE -> {
                _uiState.update {
                    it.copy(
                        propShakeTimes = it.propShakeTimes + (prop.id to now),
                        ripples = it.ripples + WatercolorRipple(
                            id = nextId++,
                            center = prop.position,
                            progress = 0f,
                            maxRadius = 110f,
                            isWarmBloom = true
                        )
                    )
                }
                emitBurst(prop.position - Offset(0f, 35f), 12, ParticleKind.EMBER)
                audioEngine.playSfx(SfxType.SPIRIT_COO, panFor(prop.position, glade))
            }
            PropType.MOONWELL_POND, PropType.WILDFLOWERS -> {
                _uiState.update {
                    it.copy(
                        ripples = it.ripples + WatercolorRipple(
                            id = nextId++,
                            center = prop.position,
                            progress = 0f,
                            maxRadius = 120f,
                            isWarmBloom = false
                        )
                    )
                }
                emitBurst(prop.position, 8, ParticleKind.PETAL)
                audioEngine.playSfx(SfxType.DEW_CHIME, panFor(prop.position, glade))
            }
            else -> {
                walkKeeperTo(prop.position + Offset(0f, 36f))
            }
        }
    }

    fun tapSpirit() {
        val state = _uiState.value
        val glade = GladeCatalog.gladeById(state.activeGladeId)
        val spirit = glade.spirit
        if (spirit.requiredLanternId !in state.litLanternIds) {
            showGentleHint("Bring lantern light closer to meet ${spirit.name}.")
            return
        }

        val greeting = spirit.greetings[lastSpiritGreetingIdx % spirit.greetings.size]
        lastSpiritGreetingIdx++

        val giftAmount = if (spirit.id !in state.claimedSpiritGiftIds) spirit.giftDew else 0
        _uiState.update {
            it.copy(
                dewlight = it.dewlight + giftAmount,
                claimedSpiritGiftIds = it.claimedSpiritGiftIds + spirit.id,
                activeSpiritMessage = SpiritMessage(
                    spiritName = spirit.name,
                    species = spirit.species,
                    text = greeting,
                    giftAwarded = giftAmount
                )
            )
        }
        emitBurst(spirit.position - Offset(0f, 20f), 12, ParticleKind.SPARKLE)
        audioEngine.playSfx(SfxType.SPIRIT_COO, panFor(spirit.position, glade))
        if (giftAmount > 0) persistProgress()
    }

    fun dismissSpiritMessage() {
        audioEngine.playSfx(SfxType.UI_TAP)
        _uiState.update { it.copy(activeSpiritMessage = null) }
    }

    fun walkKeeperTo(worldPos: Offset) {
        val glade = currentGlade()
        val clamped = Offset(
            worldPos.x.coerceIn(110f, glade.worldWidth - 110f),
            worldPos.y.coerceIn(150f, glade.worldHeight - 120f)
        )
        _uiState.update {
            it.copy(
                keeperTarget = clamped,
                keeperFacingRight = clamped.x >= it.keeperPosition.x,
                keeperSitting = false,
                ripples = it.ripples + WatercolorRipple(
                    id = nextId++,
                    center = clamped,
                    progress = 0f,
                    maxRadius = 64f,
                    isWarmBloom = false
                )
            )
        }
    }

    fun onThreadShimmerTick(worldPos: Offset) {
        val now = System.currentTimeMillis()
        if (now - lastSweepChimeTime > 170L) {
            lastSweepChimeTime = now
            audioEngine.playSfx(SfxType.THREAD_SHIMMER, panFor(worldPos, currentGlade()))
        }
    }

    fun openJournal() {
        audioEngine.playSfx(SfxType.UI_TAP)
        _uiState.update {
            it.copy(isJournalOpen = true, isSettingsOpen = false, newlyUnlockedNotice = false)
        }
    }

    fun closeJournal() {
        audioEngine.playSfx(SfxType.UI_TAP)
        _uiState.update { it.copy(isJournalOpen = false) }
    }

    fun openSettings() {
        audioEngine.playSfx(SfxType.UI_TAP)
        _uiState.update { it.copy(isSettingsOpen = true, isJournalOpen = false) }
    }

    fun closeSettings() {
        audioEngine.playSfx(SfxType.UI_TAP)
        _uiState.update { it.copy(isSettingsOpen = false) }
    }

    fun selectGlade(gladeId: GladeId) {
        val glade = GladeCatalog.gladeById(gladeId)
        val totalLit = _uiState.value.litLanternIds.size
        if (totalLit < glade.unlockTotalLanterns) return

        audioEngine.playSfx(SfxType.LANTERN_BLOOM)
        _uiState.update {
            it.copy(
                activeGladeId = gladeId,
                keeperPosition = glade.hearthCenter,
                keeperTarget = glade.hearthCenter,
                keeperSitting = false,
                fireflies = spawnInitialFireflies(glade),
                particles = emptyList(),
                ripples = emptyList(),
                activeSpiritMessage = null,
                isJournalOpen = false,
                gentleHint = glade.subtitle
            )
        }
        persistProgress()
    }

    fun unlockOrToggleKeepsake(item: KeepsakeItem) {
        val state = _uiState.value
        if (item.id in state.unlockedKeepsakeIds) {
            val nextActive = if (item.id in state.activeKeepsakeIds) {
                state.activeKeepsakeIds - item.id
            } else {
                state.activeKeepsakeIds + item.id
            }
            audioEngine.playSfx(SfxType.UI_TAP)
            _uiState.update { it.copy(activeKeepsakeIds = nextActive) }
            persistProgress()
            return
        }

        if (state.dewlight < item.cost) {
            showGentleHint("Need ${item.cost - state.dewlight} more Dewlight for ${item.title}.")
            audioEngine.playSfx(SfxType.UI_TAP)
            return
        }

        audioEngine.playSfx(SfxType.LANTERN_BLOOM)
        _uiState.update {
            it.copy(
                dewlight = it.dewlight - item.cost,
                unlockedKeepsakeIds = it.unlockedKeepsakeIds + item.id,
                activeKeepsakeIds = it.activeKeepsakeIds + item.id
            )
        }
        persistProgress()
    }

    fun setMusicVolume(volume: Float) {
        val updated = _uiState.value.audio.copy(
            musicVolume = volume.coerceIn(0f, 1f),
            isMusicMuted = if (volume > 0.02f) false else _uiState.value.audio.isMusicMuted
        )
        applyAudioSettings(updated)
    }

    fun toggleMusicMute() {
        val cur = _uiState.value.audio
        applyAudioSettings(cur.copy(isMusicMuted = !cur.isMusicMuted))
    }

    fun setSfxVolume(volume: Float) {
        val updated = _uiState.value.audio.copy(
            sfxVolume = volume.coerceIn(0f, 1f),
            isSfxMuted = if (volume > 0.02f) false else _uiState.value.audio.isSfxMuted
        )
        applyAudioSettings(updated)
    }

    fun toggleSfxMute() {
        val cur = _uiState.value.audio
        val next = cur.copy(isSfxMuted = !cur.isSfxMuted)
        applyAudioSettings(next)
        if (!next.isSfxMuted) audioEngine.playSfx(SfxType.UI_TAP)
    }

    private fun applyAudioSettings(audio: com.example.model.AudioSettings) {
        audioEngine.updateSettings(audio)
        _uiState.update { it.copy(audio = audio) }
        viewModelScope.launch { repository.saveAudioSettings(audio) }
    }

    fun setMood(mood: TimeOfDayMood) {
        audioEngine.playSfx(SfxType.UI_TAP)
        _uiState.update { it.copy(mood = mood) }
        viewModelScope.launch { repository.saveMood(mood) }
    }

    fun resetSanctuary() {
        audioEngine.playSfx(SfxType.UI_TAP)
        val firstGlade = GladeCatalog.gladeById(GladeId.POSTHOUSE)
        viewModelScope.launch {
            repository.resetSanctuaryProgress()
        }
        _uiState.update {
            it.copy(
                activeGladeId = GladeId.POSTHOUSE,
                dewlight = 4,
                litLanternIds = GladeCatalog.defaultLitLanternIds,
                unlockedKeepsakeIds = emptySet(),
                activeKeepsakeIds = emptySet(),
                claimedSpiritGiftIds = emptySet(),
                keeperPosition = firstGlade.hearthCenter,
                keeperTarget = firstGlade.hearthCenter,
                keeperSitting = false,
                fireflies = spawnInitialFireflies(firstGlade),
                particles = emptyList(),
                ripples = emptyList(),
                activeSpiritMessage = null,
                isSettingsOpen = false,
                gentleHint = "Your meadow is fresh and quiet once more."
            )
        }
    }

    fun dismissHint() {
        _uiState.update { it.copy(gentleHint = null) }
    }

    private fun showGentleHint(message: String) {
        _uiState.update { it.copy(gentleHint = message) }
    }

    private fun emitBurst(origin: Offset, count: Int, kind: ParticleKind) {
        val newParticles = List(count) {
            val angle = Random.nextFloat() * 6.283f
            val speed = when (kind) {
                ParticleKind.PETAL -> 16f + Random.nextFloat() * 26f
                ParticleKind.LEAF -> 18f + Random.nextFloat() * 24f
                ParticleKind.EMBER -> 12f + Random.nextFloat() * 18f
                ParticleKind.SPARKLE -> 22f + Random.nextFloat() * 44f
            }
            val vyBias = if (kind == ParticleKind.EMBER) -24f else -8f
            GlowParticle(
                id = nextId++,
                position = origin + Offset(Random.nextFloat() * 14f - 7f, Random.nextFloat() * 14f - 7f),
                velocity = Offset(cos(angle) * speed, sin(angle) * speed + vyBias),
                life = 0f,
                maxLife = 1.1f + Random.nextFloat() * 0.9f,
                size = 4f + Random.nextFloat() * 5f,
                kind = kind
            )
        }
        _uiState.update {
            it.copy(particles = (it.particles + newParticles).takeLast(90))
        }
    }

    private fun startSimulationLoop() {
        if (simulationJob?.isActive == true) return
        simulationJob = viewModelScope.launch {
            var lastTime = System.nanoTime()
            while (isActive && !isAppPaused) {
                delay(24L)
                val now = System.nanoTime()
                val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.005f, 0.06f)
                lastTime = now
                stepSimulation(dt)
            }
        }
    }

    private fun stepSimulation(dt: Float) {
        val state = _uiState.value
        val glade = GladeCatalog.gladeById(state.activeGladeId)

        // Move Keeper gently toward target
        val dx = state.keeperTarget.x - state.keeperPosition.x
        val dy = state.keeperTarget.y - state.keeperPosition.y
        val dist = hypot(dx, dy)
        val nextKeeper = if (dist > 3f) {
            val speed = 135f * dt
            val step = speed.coerceAtMost(dist)
            state.keeperPosition + Offset(dx / dist * step, dy / dist * step)
        } else {
            state.keeperTarget
        }

        // Drift fireflies lazily across the glade
        val nextFireflies = state.fireflies.map { mote ->
            val nextPhase = mote.phase + dt * 1.9f
            val driftX = mote.velocity.x + cos(nextPhase) * 8f * dt
            val driftY = mote.velocity.y + sin(nextPhase * 0.8f) * 8f * dt
            var nx = mote.position.x + driftX * dt
            var ny = mote.position.y + driftY * dt
            var vx = mote.velocity.x
            var vy = mote.velocity.y
            if (nx < 90f || nx > glade.worldWidth - 90f) {
                vx = -vx
                nx = nx.coerceIn(90f, glade.worldWidth - 90f)
            }
            if (ny < 130f || ny > glade.worldHeight - 130f) {
                vy = -vy
                ny = ny.coerceIn(130f, glade.worldHeight - 130f)
            }
            mote.copy(
                position = Offset(nx, ny),
                velocity = Offset(vx.coerceIn(-22f, 22f), vy.coerceIn(-22f, 22f)),
                phase = nextPhase
            )
        }

        // Advance particles
        val nextParticles = state.particles.mapNotNull { p ->
            val nLife = p.life + dt
            if (nLife >= p.maxLife) null
            else {
                val gravity = if (p.kind == ParticleKind.PETAL || p.kind == ParticleKind.LEAF) 16f else 0f
                p.copy(
                    position = p.position + Offset(p.velocity.x * dt, p.velocity.y * dt),
                    velocity = Offset(p.velocity.x * 0.985f, p.velocity.y * 0.985f + gravity * dt),
                    life = nLife
                )
            }
        }

        // Advance watercolor ripples
        val nextRipples = state.ripples.mapNotNull { r ->
            val np = r.progress + dt * if (r.isWarmBloom) 0.85f else 1.45f
            if (np >= 1f) null else r.copy(progress = np)
        }

        _uiState.update {
            it.copy(
                keeperPosition = nextKeeper,
                fireflies = nextFireflies,
                particles = nextParticles,
                ripples = nextRipples
            )
        }
    }

    private fun persistProgress() {
        val s = _uiState.value
        viewModelScope.launch {
            repository.saveProgress(
                activeGladeId = s.activeGladeId,
                dewlight = s.dewlight,
                litLanternIds = s.litLanternIds,
                unlockedKeepsakeIds = s.unlockedKeepsakeIds,
                activeKeepsakeIds = s.activeKeepsakeIds,
                claimedSpiritGiftIds = s.claimedSpiritGiftIds
            )
        }
    }

    private fun panFor(pos: Offset, glade: GladeDefinition): Float =
        ((pos.x / glade.worldWidth) * 2f - 1f).coerceIn(-0.7f, 0.7f)

    private fun isWorldPointLit(
        point: Offset,
        glade: GladeDefinition,
        litIds: Set<String>
    ): Boolean = glade.lanterns.any { lantern ->
        lantern.id in litIds &&
            hypot(point.x - lantern.position.x, point.y - lantern.position.y) <= lantern.bloomRadius * 1.04f
    }

    private fun spawnInitialFireflies(glade: GladeDefinition): List<FireflyMote> =
        List(10) { spawnRandomFirefly(glade) }

    private fun spawnRandomFirefly(glade: GladeDefinition): FireflyMote {
        val x = 140f + Random.nextFloat() * (glade.worldWidth - 280f)
        val y = 180f + Random.nextFloat() * (glade.worldHeight - 340f)
        return FireflyMote(
            id = nextId++,
            position = Offset(x, y),
            velocity = Offset(Random.nextFloat() * 24f - 12f, Random.nextFloat() * 24f - 12f),
            phase = Random.nextFloat() * 6.28f,
            radius = 9f + Random.nextFloat() * 3f
        )
    }
}
