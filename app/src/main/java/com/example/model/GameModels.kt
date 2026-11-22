package com.example.model

import androidx.compose.ui.geometry.Offset

/** Identifies each of the four hand-crafted watercolor glades. */
enum class GladeId(val storageKey: String) {
    POSTHOUSE("posthouse"),
    ORCHARD("orchard"),
    BROOK("brook"),
    HOLLOW("hollow");

    companion object {
        fun fromKey(key: String?): GladeId =
            entries.firstOrNull { it.storageKey == key } ?: POSTHOUSE
    }
}

/** Atmospheric lighting tint selectable in settings. */
enum class TimeOfDayMood(val label: String, val subtitle: String) {
    MORNING_MIST("Morning Mist", "Crisp parchment & soft dawn breeze"),
    GOLDEN_AFTERNOON("Golden Afternoon", "Warm sunbeams & honeyed sage"),
    TWILIGHT_GLOW("Twilight Glow", "Cozy dusk & luminous lantern pools");

    companion object {
        fun fromName(name: String?): TimeOfDayMood =
            entries.firstOrNull { it.name == name } ?: GOLDEN_AFTERNOON
    }
}

/** Visual style of a lantern post in the world. */
enum class LanternStyle {
    HEARTH_POST,
    IRON_HOOK,
    STONE_CAIRN,
    WILLOW_HANGING
}

/** A lantern node that can be woven with warm light threads to bloom watercolor into the grove. */
data class LanternNode(
    val id: String,
    val gladeId: GladeId,
    val name: String,
    val position: Offset,
    val bloomRadius: Float,
    val dewCost: Int,
    val startsLit: Boolean = false,
    val style: LanternStyle = LanternStyle.IRON_HOOK,
    val connectedToIds: List<String> = emptyList()
)

/** Types of hand-illustrated props in the glade. */
enum class PropType {
    POSTHOUSE_COTTAGE,
    TEA_GAZEBO,
    OBSERVATORY_TREE,
    ORANGE_TREE,
    PEACH_TREE,
    CYPRESS_TREE,
    WILLOW_TREE,
    COZY_BENCH,
    MOSS_STONE_CLUSTER,
    PARCEL_STACK,
    WILDFLOWERS,
    WOODEN_BRIDGE,
    STEPPING_STONES,
    MOONWELL_POND,
    BUSH_CLUSTER
}

/** A static or gently interactive illustrated prop in the world. */
data class WorldProp(
    val id: String,
    val type: PropType,
    val position: Offset,
    val scale: Float = 1f,
    val interactiveHint: String? = null
)

/** Friendly forest spirits that awaken when nearby lanterns bloom. */
enum class SpiritSpecies {
    POST_CROW,
    DEW_FOX,
    MOSS_TURTLE,
    STAR_OWL
}

data class SpiritNpc(
    val id: String,
    val name: String,
    val species: SpiritSpecies,
    val position: Offset,
    val requiredLanternId: String,
    val greetings: List<String>,
    val giftDew: Int = 3
)

/** Cozy keepsakes players can unlock with Dewlight and display in their glades. */
data class KeepsakeItem(
    val id: String,
    val title: String,
    val description: String,
    val cost: Int,
    val gladeId: GladeId,
    val position: Offset
)

/** Static definition of a glade. */
data class GladeDefinition(
    val id: GladeId,
    val title: String,
    val subtitle: String,
    val unlockTotalLanterns: Int,
    val worldWidth: Float,
    val worldHeight: Float,
    val hearthCenter: Offset,
    val pathPolylines: List<List<Offset>>,
    val brookPolyline: List<Offset> = emptyList(),
    val lanterns: List<LanternNode>,
    val props: List<WorldProp>,
    val spirit: SpiritNpc
)

/** A floating Dewlight firefly mote drifting across the glade. */
data class FireflyMote(
    val id: Long,
    val position: Offset,
    val velocity: Offset,
    val phase: Float,
    val radius: Float,
    val isGoldenFruit: Boolean = false
)

/** A soft particle released on taps, tree rustles, or lantern blooms. */
data class GlowParticle(
    val id: Long,
    val position: Offset,
    val velocity: Offset,
    val life: Float,
    val maxLife: Float,
    val size: Float,
    val kind: ParticleKind
)

enum class ParticleKind {
    SPARKLE,
    PETAL,
    LEAF,
    EMBER
}

/** An expanding watercolor ripple when tapping or blooming. */
data class WatercolorRipple(
    val id: Long,
    val center: Offset,
    val progress: Float,
    val maxRadius: Float,
    val isWarmBloom: Boolean
)

/** Independent audio channel settings. */
data class AudioSettings(
    val musicVolume: Float = 0.65f,
    val sfxVolume: Float = 0.75f,
    val isMusicMuted: Boolean = false,
    val isSfxMuted: Boolean = false
)

/** Active Spirit dialogue card state. */
data class SpiritMessage(
    val spiritName: String,
    val species: SpiritSpecies,
    val text: String,
    val giftAwarded: Int = 0
)

/** Complete UI state exposed by GroveViewModel. */
data class GroveUiState(
    val activeGladeId: GladeId = GladeId.POSTHOUSE,
    val dewlight: Int = 4,
    val litLanternIds: Set<String> = emptySet(),
    val unlockedKeepsakeIds: Set<String> = emptySet(),
    val activeKeepsakeIds: Set<String> = emptySet(),
    val claimedSpiritGiftIds: Set<String> = emptySet(),
    val keeperPosition: Offset = Offset(580f, 640f),
    val keeperTarget: Offset = Offset(580f, 640f),
    val keeperFacingRight: Boolean = true,
    val keeperSitting: Boolean = false,
    val fireflies: List<FireflyMote> = emptyList(),
    val particles: List<GlowParticle> = emptyList(),
    val ripples: List<WatercolorRipple> = emptyList(),
    val propShakeTimes: Map<String, Long> = emptyMap(),
    val mood: TimeOfDayMood = TimeOfDayMood.GOLDEN_AFTERNOON,
    val audio: AudioSettings = AudioSettings(),
    val activeSpiritMessage: SpiritMessage? = null,
    val gentleHint: String? = "Drag warm light from a lit lantern to a sketched lantern to bloom the grove",
    val isJournalOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val newlyUnlockedNotice: Boolean = false
)
