package com.example.ui.canvas

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import com.example.model.GladeDefinition
import com.example.model.LanternNode
import com.example.model.TimeOfDayMood
import com.example.ui.theme.BrookDeep
import com.example.ui.theme.BrookWater
import com.example.ui.theme.CanopyDeep
import com.example.ui.theme.CozyCream
import com.example.ui.theme.InkEspresso
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternGlow
import com.example.ui.theme.MeadowMoss
import com.example.ui.theme.MeadowSage
import com.example.ui.theme.MeadowSun
import com.example.ui.theme.ParchmentBase
import com.example.ui.theme.ParchmentLight
import com.example.ui.theme.ParchmentShadow
import com.example.ui.theme.ParchmentWarm
import com.example.ui.theme.SandstoneBrick
import com.example.ui.theme.SandstoneGrout
import com.example.ui.theme.SketchInkMuted
import com.example.ui.theme.SketchPaper
import com.example.ui.theme.TwilightViolet
import com.example.ui.theme.WildflowerPink
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Computes how strongly a world position is illuminated by nearby lit lanterns (0f = sketch, 1f = full watercolor).
 */
fun computeLitFactor(
    point: Offset,
    lanterns: List<LanternNode>,
    animatedRadii: Map<String, Float>
): Float {
    var strongest = 0f
    for (lantern in lanterns) {
        val radius = animatedRadii[lantern.id] ?: 0f
        if (radius <= 1f) continue
        val d = hypot(point.x - lantern.position.x, point.y - lantern.position.y)
        val inner = radius * 0.72f
        val factor = when {
            d <= inner -> 1f
            d >= radius * 1.12f -> 0f
            else -> 1f - ((d - inner) / (radius * 1.12f - inner))
        }
        if (factor > strongest) strongest = factor
    }
    return strongest.coerceIn(0f, 1f)
}

/**
 * Blends between uncolored sketch tones and vivid watercolor tones based on [litFactor].
 */
fun watercolorLerp(sketchColor: Color, litColor: Color, litFactor: Float): Color =
    lerp(sketchColor, litColor, litFactor.coerceIn(0f, 1f))

/**
 * Renders the parchment vellum base, organic multi-layered watercolor meadow washes,
 * textured sandstone brick paths, lilypad-dotted brook, and botanical grass/clover details.
 */
fun DrawScope.drawGroveGround(
    glade: GladeDefinition,
    animatedRadii: Map<String, Float>,
    mood: TimeOfDayMood,
    ambientTime: Float
) {
    // 1. Base parchment vellum canvas with gentle corner warmth
    val baseBg = when (mood) {
        TimeOfDayMood.MORNING_MIST -> ParchmentLight
        TimeOfDayMood.GOLDEN_AFTERNOON -> ParchmentBase
        TimeOfDayMood.TWILIGHT_GLOW -> lerp(ParchmentBase, TwilightViolet, 0.14f)
    }
    drawRect(color = baseBg, size = Size(glade.worldWidth, glade.worldHeight))

    // 2. Subtle misty watercolor paper blotches & faint sketch rings in unlit regions
    val mistTint = ParchmentShadow.copy(alpha = 0.26f)
    for (i in 0 until 18) {
        val bx = ((i * 217 + 45) % 1060) + 70f
        val by = ((i * 313 + 75) % 1220) + 90f
        val br = 95f + (i % 4) * 44f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(mistTint, Color.Transparent),
                center = Offset(bx, by),
                radius = br
            ),
            radius = br,
            center = Offset(bx, by)
        )
    }

    // Subtle paper grain speckles across the vellum
    val grainColor = SketchInkMuted.copy(alpha = 0.08f)
    for (i in 0 until 45) {
        val gx = ((i * 193 + 31) % 1120) + 40f
        val gy = ((i * 277 + 53) % 1300) + 50f
        drawCircle(grainColor, radius = 1.6f + (i % 3) * 0.7f, center = Offset(gx, gy))
    }

    // 3. Watercolor meadow bloom washes around each lit lantern
    for (lantern in glade.lanterns) {
        val radius = animatedRadii[lantern.id] ?: 0f
        if (radius <= 4f) continue

        val pulse = 1f + sin(ambientTime * 1.15f + lantern.position.x * 0.01f) * 0.018f
        val effectiveRadius = radius * pulse

        // Outer feathered watercolor edge halo
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    MeadowSun.copy(alpha = 0.88f),
                    MeadowSage.copy(alpha = 0.68f),
                    MeadowMoss.copy(alpha = 0.28f),
                    Color.Transparent
                ),
                center = lantern.position,
                radius = effectiveRadius * 1.10f
            ),
            radius = effectiveRadius * 1.10f,
            center = lantern.position
        )

        // Subtle watercolor pigment edge ring (characteristic wet-on-paper bloom rim)
        drawCircle(
            color = CanopyDeep.copy(alpha = 0.07f),
            radius = effectiveRadius * 0.94f,
            center = lantern.position,
            style = Stroke(width = 10f)
        )

        // Offset organic blotches for hand-painted watercolor irregularity
        val blotchOffsets = listOf(
            Offset(-effectiveRadius * 0.25f, effectiveRadius * 0.17f),
            Offset(effectiveRadius * 0.26f, -effectiveRadius * 0.14f),
            Offset(effectiveRadius * 0.10f, effectiveRadius * 0.28f),
            Offset(-effectiveRadius * 0.15f, -effectiveRadius * 0.24f)
        )
        blotchOffsets.forEachIndexed { idx, delta ->
            val center = lantern.position + delta
            val r = effectiveRadius * (0.48f + (idx % 3) * 0.09f)
            val blotchColor = if (idx % 2 == 0) MeadowSun else MeadowSage
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        blotchColor.copy(alpha = 0.24f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = r
                ),
                radius = r,
                center = center
            )
        }

        // Warm golden candlelight pool right around the lantern
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    LanternGlow.copy(alpha = 0.46f),
                    LanternAmber.copy(alpha = 0.14f),
                    Color.Transparent
                ),
                center = lantern.position,
                radius = effectiveRadius * 0.44f
            ),
            radius = effectiveRadius * 0.44f,
            center = lantern.position
        )
    }

    // 4. Sandstone brick paths with warm bevels & moss tufts
    drawSandstonePaths(glade, animatedRadii)

    // 5. Curving brook with lilypads & water shimmer (Whispering Brook)
    if (glade.brookPolyline.size >= 2) {
        drawWhisperingBrook(glade, animatedRadii, ambientTime)
    }

    // 6. Delicate grass tufts, clovers, and tiny meadow pebbles
    drawBotanicalDetails(glade, animatedRadii, ambientTime)
}

private fun DrawScope.drawSandstonePaths(
    glade: GladeDefinition,
    animatedRadii: Map<String, Float>
) {
    for (polyline in glade.pathPolylines) {
        for (i in 0 until polyline.size - 1) {
            val a = polyline[i]
            val b = polyline[i + 1]
            val dist = hypot(b.x - a.x, b.y - a.y)
            val steps = (dist / 26f).toInt().coerceAtLeast(1)
            val angleDeg = Math.toDegrees(
                kotlin.math.atan2((b.y - a.y).toDouble(), (b.x - a.x).toDouble())
            ).toFloat()

            for (s in 0..steps) {
                val t = s.toFloat() / steps
                val center = Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)
                val lit = computeLitFactor(center, glade.lanterns, animatedRadii)

                val brickFill = watercolorLerp(ParchmentWarm.copy(alpha = 0.55f), SandstoneBrick, lit)
                val brickHighlight = watercolorLerp(ParchmentLight.copy(alpha = 0.4f), CozyCream.copy(alpha = 0.5f), lit)
                val brickBorder = watercolorLerp(SketchInkMuted.copy(alpha = 0.48f), SandstoneGrout, lit)

                rotate(degrees = angleDeg + ((s % 3) - 1) * 4f, pivot = center) {
                    val jitterY = ((s % 2) * 10f) - 5f
                    val topPos = center + Offset(-12f, -11f + jitterY)
                    drawRoundRect(
                        color = brickFill,
                        topLeft = topPos,
                        size = Size(24f, 15f),
                        cornerRadius = CornerRadius(4.5f, 4.5f)
                    )
                    // Subtle warm stone bevel highlight
                    drawLine(
                        color = brickHighlight,
                        start = topPos + Offset(3f, 2.5f),
                        end = topPos + Offset(20f, 2.5f),
                        strokeWidth = 1.6f,
                        cap = StrokeCap.Round
                    )
                    drawRoundRect(
                        color = brickBorder,
                        topLeft = topPos,
                        size = Size(24f, 15f),
                        cornerRadius = CornerRadius(4.5f, 4.5f),
                        style = Stroke(width = 1.4f)
                    )

                    val botPos = center + Offset(-10f, 6f - jitterY * 0.5f)
                    drawRoundRect(
                        color = brickFill.copy(alpha = 0.92f),
                        topLeft = botPos,
                        size = Size(21f, 13f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    drawRoundRect(
                        color = brickBorder,
                        topLeft = botPos,
                        size = Size(21f, 13f),
                        cornerRadius = CornerRadius(4f, 4f),
                        style = Stroke(width = 1.3f)
                    )

                    // Occasional tiny moss dot tucked between bricks
                    if (s % 4 == 1 && lit > 0.25f) {
                        drawCircle(
                            color = MeadowMoss.copy(alpha = 0.65f * lit),
                            radius = 2.6f,
                            center = center + Offset(12f, 3f)
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawWhisperingBrook(
    glade: GladeDefinition,
    animatedRadii: Map<String, Float>,
    ambientTime: Float
) {
    val pts = glade.brookPolyline
    val path = Path().apply {
        moveTo(pts.first().x, pts.first().y)
        for (i in 1 until pts.size) {
            val prev = pts[i - 1]
            val cur = pts[i]
            val mid = Offset((prev.x + cur.x) * 0.5f, (prev.y + cur.y) * 0.5f)
            quadraticTo(prev.x, prev.y, mid.x, mid.y)
        }
        lineTo(pts.last().x, pts.last().y)
    }

    val centerLit = computeLitFactor(glade.hearthCenter, glade.lanterns, animatedRadii).coerceAtLeast(0.55f)
    val bankColor = watercolorLerp(SketchPaper, SandstoneBrick.copy(alpha = 0.72f), centerLit)
    val waterColor = watercolorLerp(ParchmentShadow, BrookWater, centerLit)
    val deepColor = watercolorLerp(SketchInkMuted.copy(alpha = 0.45f), BrookDeep, centerLit)

    drawPath(
        path = path,
        color = bankColor,
        style = Stroke(width = 80f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    drawPath(
        path = path,
        color = deepColor,
        style = Stroke(width = 64f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    drawPath(
        path = path,
        color = waterColor,
        style = Stroke(width = 56f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    // Inner translucent current band
    drawPath(
        path = path,
        color = CozyCream.copy(alpha = 0.16f * centerLit),
        style = Stroke(width = 24f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Shimmering gentle water ripples
    val shimmerOffset = (ambientTime * 18f) % 44f
    drawPath(
        path = path,
        color = Color.White.copy(alpha = 0.38f * centerLit),
        style = Stroke(
            width = 2.8f,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 32f), shimmerOffset)
        )
    )

    // Floating cozy lilypads & lotus buds in calm bends
    val lilySpots = listOf(Offset(598f, 440f), Offset(545f, 845f), Offset(572f, 1080f))
    lilySpots.forEachIndexed { idx, spot ->
        val bob = sin(ambientTime * 1.4f + idx * 1.7f) * 2.2f
        val padPos = spot + Offset(bob, 0f)
        val lit = computeLitFactor(padPos, glade.lanterns, animatedRadii)
        val padColor = watercolorLerp(SketchPaper, MeadowSage, lit)
        val ink = watercolorLerp(SketchInkMuted, InkEspresso, lit)
        drawArc(
            color = padColor,
            startAngle = 25f + idx * 45f,
            sweepAngle = 310f,
            useCenter = true,
            topLeft = padPos - Offset(11f, 9f),
            size = Size(22f, 18f)
        )
        drawArc(
            color = ink.copy(alpha = 0.7f),
            startAngle = 25f + idx * 45f,
            sweepAngle = 310f,
            useCenter = true,
            topLeft = padPos - Offset(11f, 9f),
            size = Size(22f, 18f),
            style = Stroke(1.4f)
        )
        if (idx % 2 == 0) {
            drawCircle(
                color = watercolorLerp(ParchmentLight, WildflowerPink, lit),
                radius = 4f,
                center = padPos + Offset(2f, -2f)
            )
        }
    }
}

private fun DrawScope.drawBotanicalDetails(
    glade: GladeDefinition,
    animatedRadii: Map<String, Float>,
    ambientTime: Float
) {
    for (i in 0 until 32) {
        val x = ((i * 173 + 60) % (glade.worldWidth.toInt() - 140)) + 70f
        val y = ((i * 251 + 90) % (glade.worldHeight.toInt() - 180)) + 90f
        val pos = Offset(x, y)
        val lit = computeLitFactor(pos, glade.lanterns, animatedRadii)
        val stroke = watercolorLerp(SketchInkMuted.copy(alpha = 0.42f), InkEspresso.copy(alpha = 0.55f), lit)
        val sway = sin(ambientTime * 1.3f + i * 0.7f) * 1.4f * lit

        drawLine(stroke, pos, pos + Offset(-5f + sway, -9f), strokeWidth = 1.6f, cap = StrokeCap.Round)
        drawLine(stroke, pos, pos + Offset(1f + sway, -11f), strokeWidth = 1.6f, cap = StrokeCap.Round)
        drawLine(stroke, pos, pos + Offset(6f + sway, -8f), strokeWidth = 1.6f, cap = StrokeCap.Round)

        // Occasional tiny clover trio or smooth pebble
        if (i % 5 == 0 && lit > 0.35f) {
            val clover = MeadowMoss.copy(alpha = 0.45f * lit)
            drawCircle(clover, 2.5f, pos + Offset(12f, 2f))
            drawCircle(clover, 2.5f, pos + Offset(16f, 4f))
            drawCircle(clover, 2.5f, pos + Offset(14f, 0f))
        } else if (i % 7 == 0) {
            drawOval(
                color = watercolorLerp(ParchmentShadow.copy(alpha = 0.5f), SandstoneGrout.copy(alpha = 0.55f), lit),
                topLeft = pos + Offset(-14f, 4f),
                size = Size(8f, 5f)
            )
        }
    }
}

/**
 * Draws the subtle atmospheric veil & vignette for each Time-of-Day mood.
 */
fun DrawScope.drawMoodAtmosphere(
    mood: TimeOfDayMood,
    canvasSize: Size
) {
    when (mood) {
        TimeOfDayMood.MORNING_MIST -> {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.16f),
                        Color.Transparent,
                        Color(0xFFDCE8E6).copy(alpha = 0.14f)
                    )
                ),
                size = canvasSize
            )
        }
        TimeOfDayMood.GOLDEN_AFTERNOON -> {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        LanternGlow.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(canvasSize.width * 0.35f, canvasSize.height * 0.25f),
                    radius = canvasSize.maxDimension * 0.85f
                ),
                size = canvasSize
            )
        }
        TimeOfDayMood.TWILIGHT_GLOW -> {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        TwilightViolet.copy(alpha = 0.24f),
                        TwilightViolet.copy(alpha = 0.12f),
                        InkEspresso.copy(alpha = 0.20f)
                    )
                ),
                size = canvasSize
            )
        }
    }

    // Gentle storybook paper edge vignette
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                Color.Transparent,
                InkEspresso.copy(alpha = 0.08f)
            ),
            center = Offset(canvasSize.width * 0.5f, canvasSize.height * 0.5f),
            radius = canvasSize.maxDimension * 0.72f
        ),
        size = canvasSize
    )
}
