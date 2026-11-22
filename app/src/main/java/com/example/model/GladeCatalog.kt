package com.example.model

import androidx.compose.ui.geometry.Offset

/**
 * Hand-crafted catalog of all four Willowlight glades, their lantern connection graphs,
 * illustrated world props, friendly forest spirits, and unlockable keepsakes.
 */
object GladeCatalog {

    val glades: List<GladeDefinition> = listOf(
        // 1. Posthouse Meadow (inspired directly by the cozy reference scene)
        GladeDefinition(
            id = GladeId.POSTHOUSE,
            title = "Posthouse Meadow",
            subtitle = "A quiet clearing where letters and warm lantern light wait.",
            unlockTotalLanterns = 0,
            worldWidth = 1200f,
            worldHeight = 1400f,
            hearthCenter = Offset(540f, 660f),
            pathPolylines = listOf(
                listOf(Offset(250f, 630f), Offset(450f, 680f), Offset(620f, 640f), Offset(820f, 760f)),
                listOf(Offset(540f, 670f), Offset(520f, 950f), Offset(610f, 1140f))
            ),
            lanterns = listOf(
                LanternNode(
                    id = "ph_hearth",
                    gladeId = GladeId.POSTHOUSE,
                    name = "Posthouse Lamp",
                    position = Offset(340f, 595f),
                    bloomRadius = 290f,
                    dewCost = 0,
                    startsLit = true,
                    style = LanternStyle.HEARTH_POST,
                    connectedToIds = listOf("ph_orchard", "ph_bench")
                ),
                LanternNode(
                    id = "ph_orchard",
                    gladeId = GladeId.POSTHOUSE,
                    name = "Orange Tree Lantern",
                    position = Offset(615f, 565f),
                    bloomRadius = 275f,
                    dewCost = 0,
                    startsLit = false,
                    style = LanternStyle.IRON_HOOK,
                    connectedToIds = listOf("ph_hearth", "ph_pines", "ph_stones")
                ),
                LanternNode(
                    id = "ph_bench",
                    gladeId = GladeId.POSTHOUSE,
                    name = "Benchside Lamp",
                    position = Offset(495f, 835f),
                    bloomRadius = 260f,
                    dewCost = 2,
                    startsLit = false,
                    style = LanternStyle.IRON_HOOK,
                    connectedToIds = listOf("ph_hearth", "ph_stones")
                ),
                LanternNode(
                    id = "ph_stones",
                    gladeId = GladeId.POSTHOUSE,
                    name = "Mossy Cairn",
                    position = Offset(825f, 890f),
                    bloomRadius = 285f,
                    dewCost = 3,
                    startsLit = false,
                    style = LanternStyle.STONE_CAIRN,
                    connectedToIds = listOf("ph_orchard", "ph_bench", "ph_pines")
                ),
                LanternNode(
                    id = "ph_pines",
                    gladeId = GladeId.POSTHOUSE,
                    name = "Cypress Ridge Lamp",
                    position = Offset(930f, 500f),
                    bloomRadius = 295f,
                    dewCost = 4,
                    startsLit = false,
                    style = LanternStyle.WILLOW_HANGING,
                    connectedToIds = listOf("ph_orchard", "ph_stones")
                )
            ),
            props = listOf(
                WorldProp("ph_cottage", PropType.POSTHOUSE_COTTAGE, Offset(240f, 480f), 1.08f, "Tap to warm the hearth"),
                WorldProp("ph_parcels", PropType.PARCEL_STACK, Offset(365f, 675f), 0.95f),
                WorldProp("ph_bush1", PropType.BUSH_CLUSTER, Offset(455f, 620f), 1.0f),
                WorldProp("ph_orange", PropType.ORANGE_TREE, Offset(625f, 470f), 1.05f, "Tap to rustle oranges"),
                WorldProp("ph_bench_prop", PropType.COZY_BENCH, Offset(555f, 775f), 1.0f, "Rest by the meadow"),
                WorldProp("ph_rocks", PropType.MOSS_STONE_CLUSTER, Offset(790f, 960f), 1.05f),
                WorldProp("ph_cypress1", PropType.CYPRESS_TREE, Offset(930f, 385f), 1.0f),
                WorldProp("ph_cypress2", PropType.CYPRESS_TREE, Offset(1025f, 495f), 0.9f),
                WorldProp("ph_cypress3", PropType.CYPRESS_TREE, Offset(350f, 250f), 0.9f),
                WorldProp("ph_cypress4", PropType.CYPRESS_TREE, Offset(695f, 230f), 0.95f),
                WorldProp("ph_orange2", PropType.ORANGE_TREE, Offset(460f, 1085f), 0.95f, "Tap to rustle oranges"),
                WorldProp("ph_flowers1", PropType.WILDFLOWERS, Offset(560f, 1110f), 1.0f),
                WorldProp("ph_flowers2", PropType.WILDFLOWERS, Offset(745f, 640f), 0.95f),
                WorldProp("ph_stones_step", PropType.STEPPING_STONES, Offset(890f, 650f), 1.0f)
            ),
            spirit = SpiritNpc(
                id = "spirit_crow",
                name = "Pip the Post Crow",
                species = SpiritSpecies.POST_CROW,
                position = Offset(220f, 705f),
                requiredLanternId = "ph_hearth",
                greetings = listOf(
                    "Morning, Keeper. No rush today—the morning parcels can wait until the kettle sings.",
                    "Look how the sage meadow remembers its colors whenever you string a lantern thread.",
                    "I tucked a little Dewlight beneath the porch steps for you."
                ),
                giftDew = 3
            )
        ),

        // 2. Peach Orchard
        GladeDefinition(
            id = GladeId.ORCHARD,
            title = "Peach Orchard",
            subtitle = "Sun-warmed terraces heavy with blossoms and gentle breeze.",
            unlockTotalLanterns = 3,
            worldWidth = 1200f,
            worldHeight = 1400f,
            hearthCenter = Offset(580f, 680f),
            pathPolylines = listOf(
                listOf(Offset(280f, 960f), Offset(510f, 760f), Offset(680f, 560f), Offset(910f, 440f)),
                listOf(Offset(510f, 760f), Offset(790f, 880f))
            ),
            lanterns = listOf(
                LanternNode(
                    id = "or_gate",
                    gladeId = GladeId.ORCHARD,
                    name = "Orchard Gate Lantern",
                    position = Offset(440f, 790f),
                    bloomRadius = 290f,
                    dewCost = 0,
                    startsLit = true,
                    style = LanternStyle.HEARTH_POST,
                    connectedToIds = listOf("or_gazebo", "or_terrace")
                ),
                LanternNode(
                    id = "or_gazebo",
                    gladeId = GladeId.ORCHARD,
                    name = "Tea Gazebo Lamp",
                    position = Offset(665f, 575f),
                    bloomRadius = 285f,
                    dewCost = 2,
                    startsLit = false,
                    style = LanternStyle.WILLOW_HANGING,
                    connectedToIds = listOf("or_gate", "or_blossom", "or_ridge")
                ),
                LanternNode(
                    id = "or_terrace",
                    gladeId = GladeId.ORCHARD,
                    name = "Lower Terrace Lamp",
                    position = Offset(775f, 895f),
                    bloomRadius = 270f,
                    dewCost = 3,
                    startsLit = false,
                    style = LanternStyle.STONE_CAIRN,
                    connectedToIds = listOf("or_gate", "or_blossom")
                ),
                LanternNode(
                    id = "or_blossom",
                    gladeId = GladeId.ORCHARD,
                    name = "Blossom Bower",
                    position = Offset(925f, 645f),
                    bloomRadius = 280f,
                    dewCost = 4,
                    startsLit = false,
                    style = LanternStyle.IRON_HOOK,
                    connectedToIds = listOf("or_gazebo", "or_terrace", "or_ridge")
                ),
                LanternNode(
                    id = "or_ridge",
                    gladeId = GladeId.ORCHARD,
                    name = "Hilltop Peach Post",
                    position = Offset(345f, 445f),
                    bloomRadius = 295f,
                    dewCost = 4,
                    startsLit = false,
                    style = LanternStyle.IRON_HOOK,
                    connectedToIds = listOf("or_gazebo", "or_blossom")
                )
            ),
            props = listOf(
                WorldProp("or_gaz", PropType.TEA_GAZEBO, Offset(665f, 470f), 1.05f, "Tap for warm tea steam"),
                WorldProp("or_peach1", PropType.PEACH_TREE, Offset(360f, 660f), 1.05f, "Tap to scatter petals"),
                WorldProp("or_peach2", PropType.PEACH_TREE, Offset(905f, 535f), 1.0f, "Tap to scatter petals"),
                WorldProp("or_peach3", PropType.PEACH_TREE, Offset(340f, 340f), 0.95f, "Tap to scatter petals"),
                WorldProp("or_orange1", PropType.ORANGE_TREE, Offset(820f, 975f), 1.0f, "Tap to rustle fruit"),
                WorldProp("or_bench1", PropType.COZY_BENCH, Offset(535f, 865f), 1.0f, "Rest beneath blossoms"),
                WorldProp("or_bush1", PropType.BUSH_CLUSTER, Offset(545f, 560f), 1.0f),
                WorldProp("or_flowers1", PropType.WILDFLOWERS, Offset(445f, 940f), 1.05f),
                WorldProp("or_flowers2", PropType.WILDFLOWERS, Offset(760f, 700f), 1.0f),
                WorldProp("or_stones1", PropType.MOSS_STONE_CLUSTER, Offset(260f, 930f), 0.95f),
                WorldProp("or_cypress1", PropType.CYPRESS_TREE, Offset(995f, 330f), 0.95f)
            ),
            spirit = SpiritNpc(
                id = "spirit_fox",
                name = "Mallow the Dew Fox",
                species = SpiritSpecies.DEW_FOX,
                position = Offset(580f, 625f),
                requiredLanternId = "or_gazebo",
                greetings = listOf(
                    "The peaches ripen quickest when nobody watches the clock.",
                    "Sit beneath the gazebo whenever you wish—the wind chimes keep gentle company.",
                    "Here are a few drops of morning nectar for your lantern threads."
                ),
                giftDew = 4
            )
        ),

        // 3. Whispering Brook
        GladeDefinition(
            id = GladeId.BROOK,
            title = "Whispering Brook",
            subtitle = "A glassy stream winding past willow boughs and timber planks.",
            unlockTotalLanterns = 6,
            worldWidth = 1200f,
            worldHeight = 1400f,
            hearthCenter = Offset(590f, 680f),
            pathPolylines = listOf(
                listOf(Offset(250f, 680f), Offset(590f, 670f), Offset(930f, 720f))
            ),
            brookPolyline = listOf(
                Offset(520f, 120f),
                Offset(620f, 390f),
                Offset(575f, 675f),
                Offset(515f, 960f),
                Offset(635f, 1290f)
            ),
            lanterns = listOf(
                LanternNode(
                    id = "br_bridge",
                    gladeId = GladeId.BROOK,
                    name = "Bridgehead Lantern",
                    position = Offset(465f, 635f),
                    bloomRadius = 290f,
                    dewCost = 0,
                    startsLit = true,
                    style = LanternStyle.HEARTH_POST,
                    connectedToIds = listOf("br_willow", "br_south")
                ),
                LanternNode(
                    id = "br_willow",
                    gladeId = GladeId.BROOK,
                    name = "Weeping Willow Lamp",
                    position = Offset(765f, 595f),
                    bloomRadius = 290f,
                    dewCost = 3,
                    startsLit = false,
                    style = LanternStyle.WILLOW_HANGING,
                    connectedToIds = listOf("br_bridge", "br_north", "br_east")
                ),
                LanternNode(
                    id = "br_south",
                    gladeId = GladeId.BROOK,
                    name = "Mossy Bank Cairn",
                    position = Offset(375f, 945f),
                    bloomRadius = 275f,
                    dewCost = 3,
                    startsLit = false,
                    style = LanternStyle.STONE_CAIRN,
                    connectedToIds = listOf("br_bridge", "br_east")
                ),
                LanternNode(
                    id = "br_north",
                    gladeId = GladeId.BROOK,
                    name = "Upstream Bend Lamp",
                    position = Offset(445f, 325f),
                    bloomRadius = 280f,
                    dewCost = 4,
                    startsLit = false,
                    style = LanternStyle.IRON_HOOK,
                    connectedToIds = listOf("br_willow")
                ),
                LanternNode(
                    id = "br_east",
                    gladeId = GladeId.BROOK,
                    name = "Reedbank Beacon",
                    position = Offset(830f, 930f),
                    bloomRadius = 285f,
                    dewCost = 5,
                    startsLit = false,
                    style = LanternStyle.STONE_CAIRN,
                    connectedToIds = listOf("br_willow", "br_south")
                )
            ),
            props = listOf(
                WorldProp("br_woodbridge", PropType.WOODEN_BRIDGE, Offset(580f, 675f), 1.1f),
                WorldProp("br_willow_tree", PropType.WILLOW_TREE, Offset(820f, 470f), 1.12f, "Tap to sway willow fronds"),
                WorldProp("br_rocks1", PropType.MOSS_STONE_CLUSTER, Offset(360f, 845f), 1.0f),
                WorldProp("br_bench", PropType.COZY_BENCH, Offset(335f, 585f), 1.0f, "Listen to the brook"),
                WorldProp("br_peach", PropType.PEACH_TREE, Offset(850f, 835f), 0.95f, "Tap to scatter petals"),
                WorldProp("br_step1", PropType.STEPPING_STONES, Offset(555f, 940f), 1.0f),
                WorldProp("br_flowers1", PropType.WILDFLOWERS, Offset(705f, 750f), 1.0f),
                WorldProp("br_flowers2", PropType.WILDFLOWERS, Offset(375f, 405f), 1.0f),
                WorldProp("br_bush1", PropType.BUSH_CLUSTER, Offset(440f, 250f), 1.0f),
                WorldProp("br_cypress1", PropType.CYPRESS_TREE, Offset(950f, 340f), 0.95f)
            ),
            spirit = SpiritNpc(
                id = "spirit_turtle",
                name = "Bramble the Moss Turtle",
                species = SpiritSpecies.MOSS_TURTLE,
                position = Offset(680f, 815f),
                requiredLanternId = "br_willow",
                greetings = listOf(
                    "I have watched this stream for seventy summers. It never hurries to the sea.",
                    "Even the stones grow softer when dappled with willow shade and lantern light.",
                    "Take these river-washed Dewlights along on your walk."
                ),
                giftDew = 4
            )
        ),

        // 4. Starlight Hollow
        GladeDefinition(
            id = GladeId.HOLLOW,
            title = "Starlight Hollow",
            subtitle = "An ancient mossy ring around a quiet moonwell.",
            unlockTotalLanterns = 10,
            worldWidth = 1200f,
            worldHeight = 1400f,
            hearthCenter = Offset(600f, 690f),
            pathPolylines = listOf(
                listOf(Offset(320f, 910f), Offset(520f, 780f), Offset(680f, 600f), Offset(860f, 440f))
            ),
            lanterns = listOf(
                LanternNode(
                    id = "ho_arch",
                    gladeId = GladeId.HOLLOW,
                    name = "Hollow Entrance Lamp",
                    position = Offset(440f, 815f),
                    bloomRadius = 295f,
                    dewCost = 0,
                    startsLit = true,
                    style = LanternStyle.HEARTH_POST,
                    connectedToIds = listOf("ho_moonwell", "ho_west")
                ),
                LanternNode(
                    id = "ho_moonwell",
                    gladeId = GladeId.HOLLOW,
                    name = "Moonwell Cairn",
                    position = Offset(615f, 650f),
                    bloomRadius = 310f,
                    dewCost = 4,
                    startsLit = false,
                    style = LanternStyle.STONE_CAIRN,
                    connectedToIds = listOf("ho_arch", "ho_treehouse", "ho_east")
                ),
                LanternNode(
                    id = "ho_west",
                    gladeId = GladeId.HOLLOW,
                    name = "Standing Stone Lamp",
                    position = Offset(335f, 515f),
                    bloomRadius = 280f,
                    dewCost = 4,
                    startsLit = false,
                    style = LanternStyle.IRON_HOOK,
                    connectedToIds = listOf("ho_arch", "ho_treehouse")
                ),
                LanternNode(
                    id = "ho_treehouse",
                    gladeId = GladeId.HOLLOW,
                    name = "Observatory Lantern",
                    position = Offset(745f, 405f),
                    bloomRadius = 300f,
                    dewCost = 5,
                    startsLit = false,
                    style = LanternStyle.WILLOW_HANGING,
                    connectedToIds = listOf("ho_moonwell", "ho_west", "ho_east")
                ),
                LanternNode(
                    id = "ho_east",
                    gladeId = GladeId.HOLLOW,
                    name = "Starlight Shrine",
                    position = Offset(875f, 775f),
                    bloomRadius = 290f,
                    dewCost = 5,
                    startsLit = false,
                    style = LanternStyle.STONE_CAIRN,
                    connectedToIds = listOf("ho_moonwell", "ho_treehouse")
                )
            ),
            props = listOf(
                WorldProp("ho_pond", PropType.MOONWELL_POND, Offset(600f, 710f), 1.15f, "Tap for moonlit ripples"),
                WorldProp("ho_obs", PropType.OBSERVATORY_TREE, Offset(770f, 300f), 1.08f, "Tap to chime the brass scope"),
                WorldProp("ho_rocks1", PropType.MOSS_STONE_CLUSTER, Offset(320f, 455f), 1.1f),
                WorldProp("ho_rocks2", PropType.MOSS_STONE_CLUSTER, Offset(885f, 860f), 1.05f),
                WorldProp("ho_willow1", PropType.WILLOW_TREE, Offset(335f, 945f), 1.0f, "Tap to sway willow fronds"),
                WorldProp("ho_bench", PropType.COZY_BENCH, Offset(475f, 910f), 1.0f, "Gaze at the moonwell"),
                WorldProp("ho_flowers1", PropType.WILDFLOWERS, Offset(725f, 650f), 1.05f),
                WorldProp("ho_flowers2", PropType.WILDFLOWERS, Offset(455f, 575f), 1.0f),
                WorldProp("ho_cypress1", PropType.CYPRESS_TREE, Offset(970f, 480f), 0.95f),
                WorldProp("ho_cypress2", PropType.CYPRESS_TREE, Offset(250f, 290f), 0.95f)
            ),
            spirit = SpiritNpc(
                id = "spirit_owl",
                name = "Luma the Starlight Owl",
                species = SpiritSpecies.STAR_OWL,
                position = Offset(720f, 465f),
                requiredLanternId = "ho_treehouse",
                greetings = listOf(
                    "Every constellation in this hollow was once a quiet lantern lit by kind hands.",
                    "Rest your eyes on the water—when the grove is whole, even silence shines.",
                    "Accept these starlit Dewdrops with the gratitude of the woods."
                ),
                giftDew = 5
            )
        )
    )

    val keepsakes: List<KeepsakeItem> = listOf(
        KeepsakeItem(
            id = "ks_tea_tray",
            title = "Chamomile Tea Tray",
            description = "A warm ceramic pot and two cups placed by the Posthouse bench.",
            cost = 4,
            gladeId = GladeId.POSTHOUSE,
            position = Offset(510f, 755f)
        ),
        KeepsakeItem(
            id = "ks_flower_garland",
            title = "Pressed Blossom Garland",
            description = "Cheerful pink blooms tucked beside the Orchard Gate.",
            cost = 5,
            gladeId = GladeId.ORCHARD,
            position = Offset(495f, 725f)
        ),
        KeepsakeItem(
            id = "ks_stone_frog",
            title = "Mossy Stone Frog",
            description = "A tiny smiling frog perched quietly near the footbridge.",
            cost = 5,
            gladeId = GladeId.BROOK,
            position = Offset(665f, 640f)
        ),
        KeepsakeItem(
            id = "ks_star_cushion",
            title = "Embroidered Cushion",
            description = "A soft crescent-stitched cushion for stargazing beside the Moonwell.",
            cost = 6,
            gladeId = GladeId.HOLLOW,
            position = Offset(510f, 880f)
        )
    )

    fun gladeById(id: GladeId): GladeDefinition =
        glades.firstOrNull { it.id == id } ?: glades.first()

    val defaultLitLanternIds: Set<String> =
        glades.flatMap { it.lanterns }.filter { it.startsLit }.map { it.id }.toSet()

    val totalLanternCount: Int =
        glades.sumOf { it.lanterns.size }
}
