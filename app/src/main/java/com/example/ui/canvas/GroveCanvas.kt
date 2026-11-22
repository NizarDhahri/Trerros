package com.example.ui.canvas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import com.example.model.GladeCatalog
import com.example.model.GladeDefinition
import com.example.model.GroveUiState
import com.example.model.LanternNode
import kotlin.math.hypot

/**
 * Interactive hand-painted watercolor game surface supporting:
 * - Dragging warm light threads between lit and dormant lanterns
 * - Tapping/sweeping fireflies to gather Dewlight
 * - Tapping trees, cottages, benches, and spirits
 * - Smooth clamped camera panning across the glade
 */
@Composable
fun GroveCanvas(
    glade: GladeDefinition,
    uiState: GroveUiState,
    recenterTrigger: Int,
    onWeaveLantern: (fromId: String, toId: String) -> Boolean,
    onTapLantern: (LanternNode) -> Unit,
    onGatherFireflies: (Offset) -> Int,
    onTapProp: (com.example.model.WorldProp) -> Unit,
    onTapSpirit: () -> Unit,
    onWalkKeeper: (Offset) -> Unit,
    onThreadShimmer: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current.density

    val infiniteTransition = rememberInfiniteTransition(label = "grove_ambient")
    val ambientTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 62.8318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 36000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambient_phase"
    )

    // Spring-animated watercolor bloom radius for every lantern in the active glade
    val animatedRadii = HashMap<String, Float>(glade.lanterns.size)
    for (lantern in glade.lanterns) {
        val target = if (lantern.id in uiState.litLanternIds) lantern.bloomRadius else 0f
        val animated by animateFloatAsState(
            targetValue = target,
            animationSpec = spring(dampingRatio = 0.78f, stiffness = 85f),
            label = "bloom_${lantern.id}"
        )
        animatedRadii[lantern.id] = animated
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val viewWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val viewHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)

        // Responsive zoom scale tuned for spacious cozy framing across phones and tablets
        val worldScale = remember(viewWidthPx, viewHeightPx, density) {
            (minOf(viewWidthPx, viewHeightPx) / 860f).coerceIn(0.78f, 1.32f)
        }

        val initialCamera = remember(glade.id, viewWidthPx, viewHeightPx, worldScale) {
            clampCamera(
                Offset(
                    glade.hearthCenter.x - (viewWidthPx / (2f * worldScale)),
                    glade.hearthCenter.y - (viewHeightPx / (2f * worldScale))
                ),
                glade,
                viewWidthPx,
                viewHeightPx,
                worldScale
            )
        }

        val cameraAnim = remember(glade.id) { Animatable(initialCamera, Offset.VectorConverter) }
        val cameraScope = rememberCoroutineScope()

        LaunchedEffect(recenterTrigger) {
            if (recenterTrigger > 0) {
                val target = clampCamera(
                    Offset(
                        glade.hearthCenter.x - (viewWidthPx / (2f * worldScale)),
                        glade.hearthCenter.y - (viewHeightPx / (2f * worldScale))
                    ),
                    glade,
                    viewWidthPx,
                    viewHeightPx,
                    worldScale
                )
                cameraAnim.animateTo(
                    targetValue = target,
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = 180f)
                )
            }
        }

        LaunchedEffect(viewWidthPx, viewHeightPx, worldScale) {
            cameraAnim.snapTo(
                clampCamera(cameraAnim.value, glade, viewWidthPx, viewHeightPx, worldScale)
            )
        }

        var draggingFromLantern by remember(glade.id) { mutableStateOf<LanternNode?>(null) }
        var dragWorldPos by remember(glade.id) { mutableStateOf<Offset?>(null) }

        val activeKeepsakes = remember(glade.id, uiState.activeKeepsakeIds) {
            GladeCatalog.keepsakes.filter {
                it.gladeId == glade.id && it.id in uiState.activeKeepsakeIds
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    contentDescription = "${glade.title} watercolor grove canvas"
                }
                .pointerInput(glade.id, worldScale, uiState.litLanternIds) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startWorld = screenToWorld(down.position, cameraAnim.value, worldScale)
                        var totalDragPx = 0f

                        // Check if gesture begins on a lit lantern to weave a light thread
                        val startLantern = glade.lanterns.firstOrNull { lantern ->
                            lantern.id in uiState.litLanternIds &&
                                hypot(
                                    startWorld.x - lantern.position.x,
                                    startWorld.y - lantern.position.y
                                ) <= 64f
                        }

                        if (startLantern != null) {
                            draggingFromLantern = startLantern
                            dragWorldPos = startWorld
                        }

                        var currentScreenPos = down.position
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            if (!change.pressed) {
                                // Gesture ended
                                val endWorld = screenToWorld(change.position, cameraAnim.value, worldScale)
                                val sourceLantern = draggingFromLantern
                                if (sourceLantern != null && totalDragPx > 18f) {
                                    val target = glade.lanterns.firstOrNull { candidate ->
                                        candidate.id != sourceLantern.id &&
                                            candidate.id !in uiState.litLanternIds &&
                                            hypot(
                                                endWorld.x - candidate.position.x,
                                                endWorld.y - candidate.position.y
                                            ) <= 80f
                                    }
                                    if (target != null) {
                                        onWeaveLantern(sourceLantern.id, target.id)
                                    }
                                } else if (totalDragPx <= 16f) {
                                    // Treat as gentle tap!
                                    handleWorldTap(
                                        worldPos = endWorld,
                                        glade = glade,
                                        onGatherFireflies = onGatherFireflies,
                                        onTapLantern = onTapLantern,
                                        onTapSpirit = onTapSpirit,
                                        onTapProp = onTapProp,
                                        onWalkKeeper = onWalkKeeper
                                    )
                                }
                                draggingFromLantern = null
                                dragWorldPos = null
                                change.consume()
                                break
                            }

                            val delta = change.positionChange()
                            if (delta != Offset.Zero) {
                                totalDragPx += hypot(delta.x, delta.y)
                                currentScreenPos = change.position
                                val curWorld = screenToWorld(currentScreenPos, cameraAnim.value, worldScale)

                                if (draggingFromLantern != null) {
                                    dragWorldPos = curWorld
                                    onThreadShimmer(curWorld)
                                    onGatherFireflies(curWorld)
                                } else {
                                    val nextCam = clampCamera(
                                        cameraAnim.value - (delta / worldScale),
                                        glade,
                                        viewWidthPx,
                                        viewHeightPx,
                                        worldScale
                                    )
                                    cameraScope.launch { cameraAnim.snapTo(nextCam) }
                                    onGatherFireflies(curWorld)
                                }
                                change.consume()
                            }
                        }
                    }
                }
        ) {
            val nowMs = System.currentTimeMillis()
            val cam = cameraAnim.value
            withTransform({
                scale(scaleX = worldScale, scaleY = worldScale, pivot = Offset.Zero)
                translate(left = -cam.x, top = -cam.y)
            }) {
                // 1. Parchment base, watercolor meadow washes, paths, brook
                drawGroveGround(glade, animatedRadii, uiState.mood, ambientTime)

                // 2. Woven lantern light threads & active drag ribbon
                drawLanternThreads(
                    glade = glade,
                    litLanternIds = uiState.litLanternIds,
                    draggingFromLantern = draggingFromLantern,
                    dragWorldPos = dragWorldPos,
                    ambientTime = ambientTime
                )

                // 3. Depth-sorted world elements (props, lanterns, spirit, keepsakes, keeper)
                val keeperMoving = hypot(
                    uiState.keeperTarget.x - uiState.keeperPosition.x,
                    uiState.keeperTarget.y - uiState.keeperPosition.y
                ) > 6f

                val drawItems = buildList<Pair<Float, () -> Unit>> {
                    for (prop in glade.props) {
                        val lit = computeLitFactor(prop.position, glade.lanterns, animatedRadii)
                        val shakeAge = uiState.propShakeTimes[prop.id]?.let { nowMs - it } ?: -1L
                        add(prop.position.y to {
                            drawWorldProp(prop, lit, ambientTime, shakeAge)
                        })
                    }
                    for (lantern in glade.lanterns) {
                        val isLit = lantern.id in uiState.litLanternIds
                        val isReachable = !isLit && (
                            lantern.connectedToIds.any { it in uiState.litLanternIds } ||
                                glade.lanterns.any { it.id in uiState.litLanternIds && lantern.id in it.connectedToIds }
                            )
                        val canAfford = uiState.dewlight >= lantern.dewCost
                        add(lantern.position.y to {
                            drawLanternNode(lantern, isLit, isReachable, canAfford, ambientTime, textMeasurer)
                        })
                    }
                    val spiritAwake = glade.spirit.requiredLanternId in uiState.litLanternIds
                    val giftWaiting = glade.spirit.id !in uiState.claimedSpiritGiftIds
                    add(glade.spirit.position.y to {
                        drawSpiritNpc(glade.spirit, spiritAwake, giftWaiting, ambientTime)
                    })
                    for (ks in activeKeepsakes) {
                        add(ks.position.y to {
                            drawKeepsakeOrnament(ks, ambientTime)
                        })
                    }
                    add(uiState.keeperPosition.y to {
                        drawKeeperCharacter(
                            pos = uiState.keeperPosition,
                            isMoving = keeperMoving,
                            isSitting = uiState.keeperSitting && !keeperMoving,
                            facingRight = uiState.keeperFacingRight,
                            ambientTime = ambientTime
                        )
                    })
                }.sortedBy { it.first }

                for ((_, renderFn) in drawItems) {
                    renderFn()
                }

                // 4. Floating fireflies, burst particles, and ripples
                drawFirefliesParticlesAndRipples(
                    fireflies = uiState.fireflies,
                    particles = uiState.particles,
                    ripples = uiState.ripples
                )
            }

            // 5. Screen-space atmospheric mood wash
            drawMoodAtmosphere(uiState.mood, size)
        }
    }
}

private fun screenToWorld(screenPos: Offset, cameraOffset: Offset, scale: Float): Offset =
    Offset(screenPos.x / scale + cameraOffset.x, screenPos.y / scale + cameraOffset.y)

private fun clampCamera(
    raw: Offset,
    glade: GladeDefinition,
    viewWidthPx: Float,
    viewHeightPx: Float,
    scale: Float
): Offset {
    val visibleW = viewWidthPx / scale
    val visibleH = viewHeightPx / scale
    val maxX = (glade.worldWidth - visibleW).coerceAtLeast(0f)
    val maxY = (glade.worldHeight - visibleH).coerceAtLeast(0f)
    return Offset(
        x = if (visibleW >= glade.worldWidth) -(visibleW - glade.worldWidth) * 0.5f else raw.x.coerceIn(0f, maxX),
        y = if (visibleH >= glade.worldHeight) -(visibleH - glade.worldHeight) * 0.5f else raw.y.coerceIn(0f, maxY)
    )
}

private fun handleWorldTap(
    worldPos: Offset,
    glade: GladeDefinition,
    onGatherFireflies: (Offset) -> Int,
    onTapLantern: (LanternNode) -> Unit,
    onTapSpirit: () -> Unit,
    onTapProp: (com.example.model.WorldProp) -> Unit,
    onWalkKeeper: (Offset) -> Unit
) {
    // 1. Collect fireflies if tapped directly
    if (onGatherFireflies(worldPos) > 0) return

    // 2. Tap a lantern post
    val tappedLantern = glade.lanterns.firstOrNull {
        hypot(worldPos.x - it.position.x, worldPos.y - it.position.y) <= 56f
    }
    if (tappedLantern != null) {
        onTapLantern(tappedLantern)
        return
    }

    // 3. Tap the glade's forest spirit
    if (hypot(worldPos.x - glade.spirit.position.x, worldPos.y - glade.spirit.position.y) <= 52f) {
        onTapSpirit()
        return
    }

    // 4. Tap an interactive world prop
    val tappedProp = glade.props.firstOrNull {
        hypot(worldPos.x - it.position.x, worldPos.y - it.position.y) <= 64f * it.scale
    }
    if (tappedProp != null) {
        onTapProp(tappedProp)
        return
    }

    // 5. Walk Keeper peacefully to the tapped meadow spot
    onWalkKeeper(worldPos)
}
