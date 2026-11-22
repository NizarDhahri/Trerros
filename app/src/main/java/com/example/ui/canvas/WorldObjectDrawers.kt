package com.example.ui.canvas

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import com.example.model.PropType
import com.example.model.WorldProp
import com.example.ui.theme.BrookDeep
import com.example.ui.theme.BrookWater
import com.example.ui.theme.CanopyDeep
import com.example.ui.theme.CozyCream
import com.example.ui.theme.InkEspresso
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternCore
import com.example.ui.theme.LanternGlow
import com.example.ui.theme.MeadowMoss
import com.example.ui.theme.MeadowSage
import com.example.ui.theme.MeadowSun
import com.example.ui.theme.ParchmentLight
import com.example.ui.theme.ParchmentShadow
import com.example.ui.theme.ParchmentWarm
import com.example.ui.theme.PeachCoral
import com.example.ui.theme.SandstoneBrick
import com.example.ui.theme.SandstoneGrout
import com.example.ui.theme.SketchInkMuted
import com.example.ui.theme.SketchPaper
import com.example.ui.theme.TimberDark
import com.example.ui.theme.TimberWarm
import com.example.ui.theme.WildflowerPink
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws a handcrafted 2D world prop with layered shading, organic textures,
 * smooth monochrome-to-watercolor blending via [litFactor], gentle breeze sway,
 * and responsive tap bounce/shake.
 */
fun DrawScope.drawWorldProp(
    prop: WorldProp,
    litFactor: Float,
    ambientTime: Float,
    shakeElapsedMs: Long
) {
    val shakeDeg = if (shakeElapsedMs in 0..550L) {
        val decay = 1f - (shakeElapsedMs / 550f)
        sin(shakeElapsedMs * 0.045f) * 5.2f * decay
    } else 0f

    val breezeDeg = sin(ambientTime * 1.05f + prop.position.x * 0.01f) * 1.5f
    val ink = watercolorLerp(SketchInkMuted, InkEspresso, litFactor)

    scale(scale = prop.scale, pivot = prop.position) {
        when (prop.type) {
            PropType.POSTHOUSE_COTTAGE -> drawPosthouseCottage(prop.position, litFactor, ink, ambientTime)
            PropType.TEA_GAZEBO -> drawTeaGazebo(prop.position, litFactor, ink, ambientTime)
            PropType.OBSERVATORY_TREE -> drawObservatoryTree(prop.position, litFactor, ink, ambientTime)
            PropType.ORANGE_TREE -> rotate(breezeDeg + shakeDeg, prop.position + Offset(0f, 35f)) {
                drawFruitTree(prop.position, litFactor, ink, isPeach = false)
            }
            PropType.PEACH_TREE -> rotate(breezeDeg + shakeDeg, prop.position + Offset(0f, 35f)) {
                drawFruitTree(prop.position, litFactor, ink, isPeach = true)
            }
            PropType.CYPRESS_TREE -> rotate(breezeDeg * 0.55f, prop.position + Offset(0f, 50f)) {
                drawCypressTree(prop.position, litFactor, ink)
            }
            PropType.WILLOW_TREE -> rotate(breezeDeg * 0.75f + shakeDeg * 0.6f, prop.position + Offset(0f, 40f)) {
                drawWillowTree(prop.position, litFactor, ink, ambientTime)
            }
            PropType.COZY_BENCH -> drawCozyBench(prop.position, litFactor, ink)
            PropType.MOSS_STONE_CLUSTER -> drawMossStoneCluster(prop.position, litFactor, ink)
            PropType.PARCEL_STACK -> drawParcelStack(prop.position, litFactor, ink)
            PropType.WILDFLOWERS -> drawWildflowers(prop.position, litFactor, ink, ambientTime)
            PropType.WOODEN_BRIDGE -> drawWoodenBridge(prop.position, litFactor, ink)
            PropType.STEPPING_STONES -> drawSteppingStones(prop.position, litFactor, ink)
            PropType.MOONWELL_POND -> drawMoonwellPond(prop.position, litFactor, ink, ambientTime)
            PropType.BUSH_CLUSTER -> drawBushCluster(prop.position, litFactor, ink)
        }
    }
}

private fun DrawScope.drawSoftGroundShadow(center: Offset, rx: Float, ry: Float, litFactor: Float) {
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                watercolorLerp(ParchmentShadow.copy(alpha = 0.28f), InkEspresso.copy(alpha = 0.18f), litFactor),
                Color.Transparent
            ),
            center = center,
            radius = rx
        ),
        topLeft = center - Offset(rx, ry),
        size = Size(rx * 2f, ry * 2f)
    )
}

private fun DrawScope.drawPosthouseCottage(
    pos: Offset,
    lit: Float,
    ink: Color,
    ambientTime: Float
) {
    drawSoftGroundShadow(pos + Offset(0f, 60f), 100f, 28f, lit)

    val wallColor = watercolorLerp(SketchPaper, SandstoneBrick, lit)
    val wallShade = watercolorLerp(ParchmentWarm, SandstoneGrout.copy(alpha = 0.75f), lit)
    val roofColor = watercolorLerp(ParchmentWarm, MeadowSage, lit)
    val roofDark = watercolorLerp(ParchmentShadow, CanopyDeep, lit)
    val woodColor = watercolorLerp(ParchmentShadow, TimberWarm, lit)
    val windowGlow = watercolorLerp(ParchmentLight, LanternGlow, lit)

    // Cottage base walls with soft side shadow
    val wallPath = Path().apply {
        moveTo(pos.x - 68f, pos.y - 10f)
        lineTo(pos.x + 68f, pos.y - 10f)
        lineTo(pos.x + 68f, pos.y + 52f)
        lineTo(pos.x - 68f, pos.y + 52f)
        close()
    }
    drawPath(wallPath, wallColor)
    // Right-side wall shading + eaves shadow
    drawRect(
        color = wallShade.copy(alpha = 0.35f),
        topLeft = pos + Offset(18f, -8f),
        size = Size(48f, 58f)
    )
    drawRect(
        color = ink.copy(alpha = 0.12f),
        topLeft = pos + Offset(-67f, -10f),
        size = Size(134f, 9f)
    )
    drawPath(wallPath, ink, style = Stroke(2.6f, join = StrokeJoin.Round))

    // Wooden porch deck with plank lines and step
    drawRoundRect(
        color = woodColor,
        topLeft = pos + Offset(-78f, 44f),
        size = Size(156f, 16f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = ink,
        topLeft = pos + Offset(-78f, 44f),
        size = Size(156f, 16f),
        cornerRadius = CornerRadius(6f, 6f),
        style = Stroke(2.2f)
    )
    drawRoundRect(
        color = wallShade,
        topLeft = pos + Offset(-22f, 58f),
        size = Size(44f, 8f),
        cornerRadius = CornerRadius(3f, 3f)
    )
    drawRoundRect(
        color = ink,
        topLeft = pos + Offset(-22f, 58f),
        size = Size(44f, 8f),
        cornerRadius = CornerRadius(3f, 3f),
        style = Stroke(1.8f)
    )

    // Stone chimney + brick accents + drifting smoke curls
    drawRoundRect(wallColor, pos + Offset(27f, -90f), Size(22f, 38f), CornerRadius(3f, 3f))
    drawRoundRect(woodColor, pos + Offset(25f, -93f), Size(26f, 6f), CornerRadius(2f, 2f))
    drawRoundRect(ink, pos + Offset(27f, -90f), Size(22f, 38f), CornerRadius(3f, 3f), style = Stroke(2.2f))
    drawLine(ink.copy(alpha = 0.45f), pos + Offset(28f, -74f), pos + Offset(40f, -74f), 1.4f)
    drawLine(ink.copy(alpha = 0.45f), pos + Offset(35f, -64f), pos + Offset(48f, -64f), 1.4f)
    if (lit > 0.35f) {
        for (s in 0..3) {
            val drift = ((ambientTime * 13f + s * 11f) % 44f)
            val alpha = (1f - drift / 44f) * 0.42f * lit
            drawCircle(
                color = CozyCream.copy(alpha = alpha),
                radius = 5.5f + drift * 0.16f,
                center = pos + Offset(38f + sin(ambientTime * 1.2f + s) * 7f, -98f - drift)
            )
        }
    }

    // Slanted pitched sage roof with layered scalloped shingle rows
    val roofPath = Path().apply {
        moveTo(pos.x - 86f, pos.y - 4f)
        quadraticTo(pos.x - 14f, pos.y - 78f, pos.x, pos.y - 82f)
        quadraticTo(pos.x + 14f, pos.y - 78f, pos.x + 86f, pos.y - 4f)
        lineTo(pos.x + 70f, pos.y + 1f)
        lineTo(pos.x - 70f, pos.y + 1f)
        close()
    }
    drawPath(roofPath, roofColor)
    drawPath(
        path = Path().apply {
            moveTo(pos.x, pos.y - 82f)
            quadraticTo(pos.x + 14f, pos.y - 78f, pos.x + 86f, pos.y - 4f)
            lineTo(pos.x + 70f, pos.y + 1f)
            lineTo(pos.x, pos.y - 58f)
            close()
        },
        color = roofDark.copy(alpha = 0.28f)
    )
    // Shingle scallop lines
    for (row in 0..2) {
        val ry = pos.y - 52f + row * 18f
        val halfSpan = 22f + row * 19f
        for (sx in -halfSpan.toInt()..halfSpan.toInt() step 16) {
            drawArc(
                color = ink.copy(alpha = 0.32f),
                startAngle = 15f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(pos.x + sx - 7f, ry),
                size = Size(14f, 8f),
                style = Stroke(1.3f, cap = StrokeCap.Round)
            )
        }
    }
    drawPath(roofPath, ink, style = Stroke(2.8f, join = StrokeJoin.Round, cap = StrokeCap.Round))

    // Envelope crest on the gable (Posthouse signature)
    drawRoundRect(CozyCream, pos + Offset(-16f, -45f), Size(32f, 21f), CornerRadius(4f, 4f))
    drawRoundRect(ink, pos + Offset(-16f, -45f), Size(32f, 21f), CornerRadius(4f, 4f), style = Stroke(2f))
    drawLine(ink, pos + Offset(-15f, -44f), pos + Offset(0f, -33f), strokeWidth = 1.8f)
    drawLine(ink, pos + Offset(15f, -44f), pos + Offset(0f, -33f), strokeWidth = 1.8f)
    drawCircle(watercolorLerp(ParchmentShadow, PeachCoral, lit), radius = 3.4f, center = pos + Offset(0f, -33f))

    // Arched wooden door with brass knob & plank lines
    val doorPath = Path().apply {
        moveTo(pos.x - 14f, pos.y + 44f)
        lineTo(pos.x - 14f, pos.y + 14f)
        quadraticTo(pos.x, pos.y + 1f, pos.x + 14f, pos.y + 14f)
        lineTo(pos.x + 14f, pos.y + 44f)
        close()
    }
    drawPath(doorPath, woodColor)
    drawLine(ink.copy(alpha = 0.35f), pos + Offset(-5f, 11f), pos + Offset(-5f, 43f), 1.3f)
    drawLine(ink.copy(alpha = 0.35f), pos + Offset(5f, 11f), pos + Offset(5f, 43f), 1.3f)
    drawPath(doorPath, ink, style = Stroke(2.2f))
    drawCircle(watercolorLerp(ParchmentLight, LanternAmber, lit), 2.6f, pos + Offset(8f, 29f))

    // Warm glowing arched windows + tiny flower window boxes
    for (wx in listOf(-41f, 41f)) {
        if (lit > 0.4f) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(LanternGlow.copy(alpha = 0.36f * lit), Color.Transparent),
                    center = pos + Offset(wx, 18f),
                    radius = 24f
                ),
                radius = 24f,
                center = pos + Offset(wx, 18f)
            )
        }
        val winPath = Path().apply {
            moveTo(pos.x + wx - 11f, pos.y + 28f)
            lineTo(pos.x + wx - 11f, pos.y + 12f)
            quadraticTo(pos.x + wx, pos.y + 1f, pos.x + wx + 11f, pos.y + 12f)
            lineTo(pos.x + wx + 11f, pos.y + 28f)
            close()
        }
        drawPath(winPath, windowGlow)
        // Window mullion cross
        drawLine(ink.copy(alpha = 0.55f), pos + Offset(wx, 7f), pos + Offset(wx, 28f), 1.4f)
        drawLine(ink.copy(alpha = 0.55f), pos + Offset(wx - 10f, 19f), pos + Offset(wx + 10f, 19f), 1.4f)
        drawPath(winPath, ink, style = Stroke(2f))

        // Flower box sill
        drawRoundRect(woodColor, pos + Offset(wx - 13f, 27f), Size(26f, 6f), CornerRadius(2f, 2f))
        drawRoundRect(ink, pos + Offset(wx - 13f, 27f), Size(26f, 6f), CornerRadius(2f, 2f), style = Stroke(1.5f))
        if (lit > 0.25f) {
            drawCircle(watercolorLerp(SketchPaper, WildflowerPink, lit), 2.8f, pos + Offset(wx - 6f, 26f))
            drawCircle(watercolorLerp(SketchPaper, MeadowSun, lit), 2.6f, pos + Offset(wx, 25.5f))
            drawCircle(watercolorLerp(SketchPaper, WildflowerPink, lit), 2.8f, pos + Offset(wx + 6f, 26f))
        }
    }
}

private fun DrawScope.drawTeaGazebo(pos: Offset, lit: Float, ink: Color, ambientTime: Float) {
    drawSoftGroundShadow(pos + Offset(0f, 54f), 84f, 24f, lit)
    val timber = watercolorLerp(ParchmentShadow, TimberWarm, lit)
    val canopy = watercolorLerp(SketchPaper, WildflowerPink.copy(alpha = 0.88f), lit)
    val canopyTrim = watercolorLerp(ParchmentWarm, PeachCoral, lit)
    val platform = watercolorLerp(ParchmentWarm, SandstoneBrick, lit)

    // Raised stone platform with step
    drawRoundRect(platform, pos + Offset(-66f, 36f), Size(132f, 17f), CornerRadius(8f, 8f))
    drawRoundRect(ink, pos + Offset(-66f, 36f), Size(132f, 17f), CornerRadius(8f, 8f), style = Stroke(2.3f))
    drawRoundRect(platform, pos + Offset(-26f, 50f), Size(52f, 7f), CornerRadius(3f, 3f))
    drawRoundRect(ink, pos + Offset(-26f, 50f), Size(52f, 7f), CornerRadius(3f, 3f), style = Stroke(1.8f))

    // Carved wooden posts + decorative corner braces
    for (px in listOf(-46f, 46f)) {
        drawLine(timber, pos + Offset(px, -24f), pos + Offset(px, 38f), strokeWidth = 7.5f, cap = StrokeCap.Round)
        drawLine(ink, pos + Offset(px, -24f), pos + Offset(px, 38f), strokeWidth = 2.2f, cap = StrokeCap.Round)
        val braceDir = if (px < 0) 1f else -1f
        drawLine(timber, pos + Offset(px, -4f), pos + Offset(px + braceDir * 16f, -20f), strokeWidth = 4f, cap = StrokeCap.Round)
    }

    // Center hanging paper lantern gently swaying
    val sway = sin(ambientTime * 1.4f) * 2.5f
    drawLine(ink, pos + Offset(0f, -22f), pos + Offset(sway, -8f), 1.6f)
    drawCircle(watercolorLerp(ParchmentLight, LanternGlow, lit), 7.5f, pos + Offset(sway, -3f))
    drawCircle(ink, 7.5f, pos + Offset(sway, -3f), style = Stroke(1.6f))

    // Scalloped octagonal canopy roof + top finial
    val roof = Path().apply {
        moveTo(pos.x - 76f, pos.y - 19f)
        quadraticTo(pos.x - 24f, pos.y - 68f, pos.x, pos.y - 78f)
        quadraticTo(pos.x + 24f, pos.y - 68f, pos.x + 76f, pos.y - 19f)
        close()
    }
    drawPath(roof, canopy)
    drawLine(canopyTrim.copy(alpha = 0.45f), pos + Offset(0f, -77f), pos + Offset(-30f, -20f), 2.5f)
    drawLine(canopyTrim.copy(alpha = 0.45f), pos + Offset(0f, -77f), pos + Offset(30f, -20f), 2.5f)
    drawPath(roof, ink, style = Stroke(2.6f, join = StrokeJoin.Round))
    drawCircle(watercolorLerp(ParchmentShadow, LanternAmber, lit), 5f, pos + Offset(0f, -81f))
    drawCircle(ink, 5f, pos + Offset(0f, -81f), style = Stroke(1.8f))

    // Low tea table with cloth, ceramic teapot, and two teacups
    drawRoundRect(timber, pos + Offset(-22f, 24f), Size(44f, 12f), CornerRadius(4f, 4f))
    drawRoundRect(ink, pos + Offset(-22f, 24f), Size(44f, 12f), CornerRadius(4f, 4f), style = Stroke(1.6f))
    drawCircle(watercolorLerp(SketchPaper, CozyCream, lit), 8.5f, pos + Offset(-2f, 18f))
    drawCircle(ink, 8.5f, pos + Offset(-2f, 18f), style = Stroke(1.8f))
    drawRoundRect(watercolorLerp(SketchPaper, CozyCream, lit), pos + Offset(10f, 18f), Size(6f, 6f), CornerRadius(2f, 2f))
    drawRoundRect(ink, pos + Offset(10f, 18f), Size(6f, 6f), CornerRadius(2f, 2f), style = Stroke(1.4f))
    if (lit > 0.4f) {
        val wisp = (ambientTime * 15f) % 24f
        drawCircle(
            CozyCream.copy(alpha = (1f - wisp / 24f) * 0.52f),
            radius = 4f,
            center = pos + Offset(sin(ambientTime * 2f) * 3f - 2f, 6f - wisp)
        )
    }
}

private fun DrawScope.drawObservatoryTree(pos: Offset, lit: Float, ink: Color, ambientTime: Float) {
    drawSoftGroundShadow(pos + Offset(0f, 62f), 80f, 25f, lit)
    val bark = watercolorLerp(ParchmentShadow, TimberWarm, lit)
    val barkDark = watercolorLerp(SketchInkMuted, TimberDark, lit)
    val foliage = watercolorLerp(SketchPaper, MeadowMoss, lit)
    val foliageTop = watercolorLerp(ParchmentLight, MeadowSage, lit)
    val brass = watercolorLerp(ParchmentWarm, LanternAmber, lit)

    // Rooted ancient oak trunk + grain
    val trunk = Path().apply {
        moveTo(pos.x - 34f, pos.y + 58f)
        quadraticTo(pos.x - 22f, pos.y + 20f, pos.x - 25f, pos.y - 34f)
        lineTo(pos.x + 25f, pos.y - 34f)
        quadraticTo(pos.x + 22f, pos.y + 20f, pos.x + 34f, pos.y + 58f)
        close()
    }
    drawPath(trunk, bark)
    drawLine(barkDark.copy(alpha = 0.45f), pos + Offset(-10f, 52f), pos + Offset(-8f, 4f), 2f, StrokeCap.Round)
    drawLine(barkDark.copy(alpha = 0.45f), pos + Offset(11f, 48f), pos + Offset(9f, 12f), 2f, StrokeCap.Round)
    drawPath(trunk, ink, style = Stroke(2.5f))

    // Glowing round hollow window in trunk
    drawCircle(watercolorLerp(ParchmentLight, LanternGlow, lit), 9f, pos + Offset(0f, 24f))
    drawCircle(ink, 9f, pos + Offset(0f, 24f), style = Stroke(2f))

    // Layered leafy canopy
    val clouds = listOf(
        Offset(-36f, -44f) to 36f,
        Offset(36f, -40f) to 35f,
        Offset(0f, -68f) to 46f
    )
    clouds.forEach { (off, r) ->
        drawCircle(foliage, r, pos + off)
        drawCircle(foliageTop.copy(alpha = 0.55f), r * 0.72f, pos + off + Offset(-4f, -6f))
        drawCircle(ink, r, pos + off, style = Stroke(2.2f))
    }

    // Wooden observation deck + railing posts + brass telescope
    drawRoundRect(bark, pos + Offset(-38f, -12f), Size(76f, 12f), CornerRadius(4f, 4f))
    drawRoundRect(ink, pos + Offset(-38f, -12f), Size(76f, 12f), CornerRadius(4f, 4f), style = Stroke(2f))
    for (rx in listOf(-32f, -12f, 12f, 32f)) {
        drawLine(ink, pos + Offset(rx, -12f), pos + Offset(rx, -22f), 2.2f, StrokeCap.Round)
    }
    drawLine(bark, pos + Offset(-34f, -22f), pos + Offset(34f, -22f), 3.5f, StrokeCap.Round)
    drawLine(ink, pos + Offset(-34f, -22f), pos + Offset(34f, -22f), 1.6f, StrokeCap.Round)

    rotate(-28f + sin(ambientTime * 0.65f) * 2.2f, pos + Offset(10f, -24f)) {
        drawRoundRect(brass, pos + Offset(2f, -30f), Size(34f, 10f), CornerRadius(4f, 4f))
        drawRoundRect(ink, pos + Offset(2f, -30f), Size(34f, 10f), CornerRadius(4f, 4f), style = Stroke(1.9f))
        drawCircle(CozyCream, 3.2f, pos + Offset(33f, -25f))
    }
}

private fun DrawScope.drawFruitTree(pos: Offset, lit: Float, ink: Color, isPeach: Boolean) {
    drawSoftGroundShadow(pos + Offset(0f, 52f), 50f, 17f, lit)
    val bark = watercolorLerp(ParchmentShadow, TimberWarm, lit)
    val barkDark = watercolorLerp(SketchInkMuted, TimberDark, lit)
    val canopy = watercolorLerp(SketchPaper, if (isPeach) MeadowSun else MeadowSage, lit)
    val canopyShade = watercolorLerp(ParchmentWarm, MeadowMoss, lit)
    val canopyLight = watercolorLerp(ParchmentLight, MeadowSun, lit)
    val fruitColor = watercolorLerp(ParchmentWarm, if (isPeach) WildflowerPink else LanternAmber, lit)

    // Rooted trunk with bark line
    val trunk = Path().apply {
        moveTo(pos.x - 14f, pos.y + 50f)
        quadraticTo(pos.x - 7f, pos.y + 20f, pos.x - 8f, pos.y - 6f)
        lineTo(pos.x + 8f, pos.y - 6f)
        quadraticTo(pos.x + 7f, pos.y + 20f, pos.x + 14f, pos.y + 50f)
        close()
    }
    drawPath(trunk, bark)
    drawLine(barkDark.copy(alpha = 0.45f), pos + Offset(-2f, 42f), pos + Offset(-2f, 10f), 1.6f, StrokeCap.Round)
    drawPath(trunk, ink, style = Stroke(2.3f, join = StrokeJoin.Round))

    // Layered leafy canopy with sunlit top-left crown & deeper underside
    val puffs = listOf(
        Offset(-27f, -22f) to 26f,
        Offset(27f, -20f) to 25f,
        Offset(-14f, -45f) to 28f,
        Offset(16f, -43f) to 27f,
        Offset(0f, -29f) to 33f
    )
    puffs.forEach { (off, r) ->
        drawCircle(canopyShade, r, pos + off + Offset(2f, 4f))
        drawCircle(canopy, r, pos + off)
    }
    drawCircle(canopyLight.copy(alpha = 0.45f), 22f, pos + Offset(-12f, -40f))

    // Scalloped botanical ink arcs around crown instead of single flat circle
    puffs.take(4).forEach { (off, r) ->
        drawCircle(ink.copy(alpha = 0.85f), r, pos + off, style = Stroke(2.1f))
    }

    // Cozy hanging oranges or peaches with tiny leaf stems & specular gloss
    val fruitPositions = listOf(
        Offset(-22f, -18f),
        Offset(18f, -14f),
        Offset(-4f, -34f),
        Offset(24f, -34f),
        Offset(-19f, -40f)
    )
    fruitPositions.forEach { fOff ->
        val fp = pos + fOff
        drawCircle(fruitColor, 7.5f, fp)
        drawCircle(ink, 7.5f, fp, style = Stroke(1.8f))
        drawCircle(Color.White.copy(alpha = 0.5f * lit), 2.2f, fp + Offset(-2.2f, -2.2f))
        drawLine(ink, fp + Offset(0f, -7f), fp + Offset(2f, -10f), 1.5f, StrokeCap.Round)
    }
}

private fun DrawScope.drawCypressTree(pos: Offset, lit: Float, ink: Color) {
    drawSoftGroundShadow(pos + Offset(0f, 54f), 30f, 12f, lit)
    val bark = watercolorLerp(ParchmentShadow, TimberWarm, lit)
    val leaf = watercolorLerp(ParchmentLight, MeadowSage, lit)
    val leafShade = watercolorLerp(ParchmentWarm, MeadowMoss, lit)

    drawRect(bark, pos + Offset(-6f, 24f), Size(12f, 28f))
    drawRect(ink, pos + Offset(-6f, 24f), Size(12f, 28f), style = Stroke(2f))

    val cone = Path().apply {
        moveTo(pos.x, pos.y - 70f)
        quadraticTo(pos.x + 29f, pos.y - 10f, pos.x + 24f, pos.y + 26f)
        quadraticTo(pos.x, pos.y + 34f, pos.x - 24f, pos.y + 26f)
        quadraticTo(pos.x - 29f, pos.y - 10f, pos.x, pos.y - 70f)
        close()
    }
    drawPath(cone, leaf)
    // Right-half soft watercolor shading
    drawPath(
        path = Path().apply {
            moveTo(pos.x, pos.y - 70f)
            quadraticTo(pos.x + 29f, pos.y - 10f, pos.x + 24f, pos.y + 26f)
            quadraticTo(pos.x + 8f, pos.y + 32f, pos.x, pos.y + 30f)
            close()
        },
        color = leafShade.copy(alpha = 0.32f)
    )
    drawPath(cone, ink, style = Stroke(2.3f, join = StrokeJoin.Round))

    // Delicate hand-drawn needle texture arcs
    for (row in 0..5) {
        val y = pos.y - 38f + row * 11f
        val span = 6f + row * 2.5f
        drawLine(ink.copy(alpha = 0.42f), pos + Offset(-span, y), pos + Offset(-span + 4f, y + 3f), 1.4f, StrokeCap.Round)
        drawLine(ink.copy(alpha = 0.42f), pos + Offset(span - 4f, y + 3f), pos + Offset(span, y), 1.4f, StrokeCap.Round)
    }
}

private fun DrawScope.drawWillowTree(pos: Offset, lit: Float, ink: Color, ambientTime: Float) {
    drawSoftGroundShadow(pos + Offset(0f, 56f), 58f, 19f, lit)
    val bark = watercolorLerp(ParchmentShadow, TimberWarm, lit)
    val canopy = watercolorLerp(SketchPaper, MeadowSage, lit)
    val canopyTop = watercolorLerp(ParchmentLight, MeadowSun, lit)

    val trunk = Path().apply {
        moveTo(pos.x - 16f, pos.y + 52f)
        quadraticTo(pos.x - 6f, pos.y + 10f, pos.x - 10f, pos.y - 14f)
        lineTo(pos.x + 10f, pos.y - 14f)
        quadraticTo(pos.x + 8f, pos.y + 10f, pos.x + 16f, pos.y + 52f)
        close()
    }
    drawPath(trunk, bark)
    drawPath(trunk, ink, style = Stroke(2.4f))

    drawOval(canopy, pos + Offset(-52f, -68f), Size(104f, 66f))
    drawOval(canopyTop.copy(alpha = 0.4f), pos + Offset(-40f, -64f), Size(68f, 36f))
    drawOval(ink, pos + Offset(-52f, -68f), Size(104f, 66f), style = Stroke(2.3f))

    // Swaying layered willow curtains
    for (i in -3..3) {
        val sx = pos.x + i * 13f
        val sway = sin(ambientTime * 1.35f + i * 0.7f) * 5.5f
        val strand = Path().apply {
            moveTo(sx, pos.y - 26f)
            quadraticTo(sx + sway * 0.5f, pos.y + 4f, sx + sway, pos.y + 30f)
        }
        drawPath(
            strand,
            watercolorLerp(SketchInkMuted, CanopyDeep, lit),
            style = Stroke(3f, cap = StrokeCap.Round)
        )
        drawCircle(
            watercolorLerp(ParchmentLight, MeadowSun, lit),
            2.4f,
            Offset(sx + sway, pos.y + 31f)
        )
    }
}

private fun DrawScope.drawCozyBench(pos: Offset, lit: Float, ink: Color) {
    drawSoftGroundShadow(pos + Offset(0f, 19f), 44f, 13f, lit)
    val wood = watercolorLerp(ParchmentWarm, TimberWarm, lit)
    val woodLight = watercolorLerp(ParchmentLight, SandstoneBrick, lit)
    val iron = watercolorLerp(SketchInkMuted, TimberDark, lit)

    // Backrest slats with warm top bevel
    for (dy in listOf(-15f, -7f)) {
        drawRoundRect(wood, pos + Offset(-32f, dy), Size(64f, 6f), CornerRadius(3f, 3f))
        drawLine(woodLight.copy(alpha = 0.55f), pos + Offset(-30f, dy + 1.5f), pos + Offset(30f, dy + 1.5f), 1.4f)
        drawRoundRect(ink, pos + Offset(-32f, dy), Size(64f, 6f), CornerRadius(3f, 3f), style = Stroke(1.7f))
    }
    // Seat
    drawRoundRect(wood, pos + Offset(-34f, 2f), Size(68f, 9f), CornerRadius(3f, 3f))
    drawRoundRect(ink, pos + Offset(-34f, 2f), Size(68f, 9f), CornerRadius(3f, 3f), style = Stroke(1.8f))

    // Ornate scrollwork wrought-iron armrests & legs (direct nod to reference bench!)
    for (lx in listOf(-32f, 28f)) {
        drawLine(iron, pos + Offset(lx, -16f), pos + Offset(lx, 18f), strokeWidth = 3.2f, cap = StrokeCap.Round)
        drawCircle(iron, 4.5f, pos + Offset(lx + 2f, -17f), style = Stroke(2.1f))
    }
    // Right backrest wrought-iron vine flourish
    drawArc(
        color = iron,
        startAngle = 190f,
        sweepAngle = 210f,
        useCenter = false,
        topLeft = pos + Offset(22f, -24f),
        size = Size(14f, 14f),
        style = Stroke(2.2f, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawMossStoneCluster(pos: Offset, lit: Float, ink: Color) {
    drawSoftGroundShadow(pos + Offset(0f, 27f), 58f, 17f, lit)
    val stone = watercolorLerp(SketchPaper, Color(0xFFD5D0C5), lit)
    val stoneShade = watercolorLerp(ParchmentShadow, Color(0xFFB8B2A6), lit)
    val moss = watercolorLerp(ParchmentWarm, MeadowSage, lit)
    val mossDark = watercolorLerp(SketchInkMuted, MeadowMoss, lit)

    // Large center stone with subtle carved spirit face & lush moss cap
    drawOval(stone, pos + Offset(-22f, -35f), Size(44f, 57f))
    drawOval(stoneShade.copy(alpha = 0.45f), pos + Offset(0f, -28f), Size(20f, 44f))
    drawOval(ink, pos + Offset(-22f, -35f), Size(44f, 57f), style = Stroke(2.2f))
    drawArc(mossDark, 195f, 150f, false, pos + Offset(-20f, -35f), Size(40f, 24f), style = Stroke(8f, cap = StrokeCap.Round))
    drawArc(moss, 200f, 140f, false, pos + Offset(-19f, -36f), Size(38f, 22f), style = Stroke(5f, cap = StrokeCap.Round))

    // Expressive peaceful carved stone eyes & smile
    drawCircle(ink, 2.3f, pos + Offset(-6f, -12f))
    drawCircle(ink, 2.3f, pos + Offset(6f, -12f))
    drawArc(ink, 15f, 150f, false, pos + Offset(-5f, -7f), Size(10f, 6f), style = Stroke(1.6f, cap = StrokeCap.Round))

    // Flanking smaller stones with tiny moss cap & sprout
    drawOval(stone, pos + Offset(-46f, -8f), Size(28f, 30f))
    drawOval(ink, pos + Offset(-46f, -8f), Size(28f, 30f), style = Stroke(2f))
    drawOval(stone, pos + Offset(18f, -4f), Size(30f, 28f))
    drawOval(ink, pos + Offset(18f, -4f), Size(30f, 28f), style = Stroke(2f))
    if (lit > 0.3f) {
        drawCircle(WildflowerPink.copy(alpha = lit), 3f, pos + Offset(-36f, 16f))
        drawCircle(MeadowMoss.copy(alpha = lit), 3.5f, pos + Offset(32f, -6f))
    }
}

private fun DrawScope.drawParcelStack(pos: Offset, lit: Float, ink: Color) {
    drawSoftGroundShadow(pos + Offset(-4f, 20f), 34f, 10f, lit)
    val box = watercolorLerp(ParchmentWarm, Color(0xFFD6A873), lit)
    val boxDark = watercolorLerp(ParchmentShadow, Color(0xFFB58652), lit)
    val twine = watercolorLerp(SketchInkMuted, CozyCream, lit)
    val seal = watercolorLerp(ParchmentShadow, PeachCoral, lit)

    for ((idx, rect) in listOf(
        listOf(-22f, 0f, 24f, 20f),
        listOf(4f, 2f, 22f, 18f),
        listOf(-10f, -18f, 20f, 18f)
    ).withIndex()) {
        val (dx, dy, w, h) = rect
        drawRoundRect(box, pos + Offset(dx, dy), Size(w, h), CornerRadius(3f, 3f))
        drawRect(boxDark.copy(alpha = 0.35f), pos + Offset(dx + w * 0.65f, dy + 1f), Size(w * 0.32f, h - 2f))
        drawRoundRect(ink, pos + Offset(dx, dy), Size(w, h), CornerRadius(3f, 3f), style = Stroke(1.8f))
        drawLine(twine, pos + Offset(dx + w / 2f, dy), pos + Offset(dx + w / 2f, dy + h), strokeWidth = 1.8f)
        drawLine(twine, pos + Offset(dx, dy + h / 2f), pos + Offset(dx + w, dy + h / 2f), strokeWidth = 1.4f)
        if (idx == 2) {
            drawCircle(seal, 2.8f, pos + Offset(dx + w / 2f, dy + h / 2f))
        }
    }
}

private fun DrawScope.drawWildflowers(pos: Offset, lit: Float, ink: Color, ambientTime: Float) {
    val petalColor = watercolorLerp(ParchmentLight, WildflowerPink, lit)
    val stemColor = watercolorLerp(SketchInkMuted, MeadowMoss, lit)
    val offsets = listOf(Offset(-18f, 4f), Offset(0f, -6f), Offset(16f, 6f), Offset(-6f, 12f))

    // Leafy base
    drawOval(stemColor.copy(alpha = 0.38f), pos + Offset(-22f, 4f), Size(44f, 16f))

    offsets.forEachIndexed { idx, off ->
        val sway = sin(ambientTime * 1.7f + idx) * 2.2f
        val p = pos + off
        drawLine(stemColor, p + Offset(0f, 10f), p + Offset(sway, 0f), strokeWidth = 2f, cap = StrokeCap.Round)
        val head = p + Offset(sway, 0f)
        for (a in 0 until 5) {
            val ang = a * (6.283f / 5f)
            drawCircle(petalColor, 4.3f, head + Offset(cos(ang) * 4.5f, sin(ang) * 4.5f))
        }
        drawCircle(watercolorLerp(ParchmentShadow, LanternGlow, lit), 3.1f, head)
        drawCircle(ink.copy(alpha = 0.65f), 6.6f, head, style = Stroke(1.1f))
    }
}

private fun DrawScope.drawWoodenBridge(pos: Offset, lit: Float, ink: Color) {
    drawSoftGroundShadow(pos + Offset(0f, 24f), 68f, 16f, lit)
    val wood = watercolorLerp(ParchmentWarm, TimberWarm, lit)
    val darkWood = watercolorLerp(SketchInkMuted, TimberDark, lit)

    drawRoundRect(wood, pos + Offset(-64f, -24f), Size(128f, 48f), CornerRadius(8f, 8f))
    drawRoundRect(ink, pos + Offset(-64f, -24f), Size(128f, 48f), CornerRadius(8f, 8f), style = Stroke(2.4f))

    for (px in -48..48 step 16) {
        drawLine(ink.copy(alpha = 0.55f), pos + Offset(px.toFloat(), -22f), pos + Offset(px.toFloat(), 22f), 1.6f)
        drawCircle(darkWood.copy(alpha = 0.6f), 1.5f, pos + Offset(px.toFloat() - 6f, -16f))
        drawCircle(darkWood.copy(alpha = 0.6f), 1.5f, pos + Offset(px.toFloat() - 6f, 16f))
    }
    // Handrails & corner posts
    drawLine(darkWood, pos + Offset(-66f, -26f), pos + Offset(66f, -26f), strokeWidth = 5.5f, cap = StrokeCap.Round)
    drawLine(darkWood, pos + Offset(-66f, 26f), pos + Offset(66f, 26f), strokeWidth = 5.5f, cap = StrokeCap.Round)
    for (postX in listOf(-64f, 64f)) {
        drawCircle(wood, 5f, pos + Offset(postX, -26f))
        drawCircle(ink, 5f, pos + Offset(postX, -26f), style = Stroke(1.8f))
        drawCircle(wood, 5f, pos + Offset(postX, 26f))
        drawCircle(ink, 5f, pos + Offset(postX, 26f), style = Stroke(1.8f))
    }
}

private fun DrawScope.drawSteppingStones(pos: Offset, lit: Float, ink: Color) {
    val stone = watercolorLerp(ParchmentWarm, SandstoneBrick, lit)
    listOf(Offset(-28f, 8f) to 14f, Offset(-2f, -4f) to 16f, Offset(26f, 6f) to 13f).forEach { (off, r) ->
        drawOval(stone, pos + off - Offset(r, r * 0.65f), Size(r * 2f, r * 1.3f))
        drawOval(ink, pos + off - Offset(r, r * 0.65f), Size(r * 2f, r * 1.3f), style = Stroke(1.8f))
    }
}

private fun DrawScope.drawMoonwellPond(pos: Offset, lit: Float, ink: Color, ambientTime: Float) {
    val rim = watercolorLerp(ParchmentWarm, SandstoneBrick, lit)
    val water = watercolorLerp(ParchmentShadow, BrookWater, lit)
    val deep = watercolorLerp(SketchInkMuted, BrookDeep, lit)

    drawOval(rim, pos - Offset(78f, 50f), Size(156f, 100f))
    drawOval(ink, pos - Offset(78f, 50f), Size(156f, 100f), style = Stroke(2.4f))
    drawOval(
        brush = Brush.radialGradient(listOf(water, deep)),
        topLeft = pos - Offset(63f, 39f),
        size = Size(126f, 78f)
    )
    // Rim boulders
    for (a in 0 until 8) {
        val ang = a * (6.283f / 8f) + 0.3f
        val rx = pos.x + cos(ang) * 70f
        val ry = pos.y + sin(ang) * 44f
        drawCircle(rim, 8f, Offset(rx, ry))
        drawCircle(ink, 8f, Offset(rx, ry), style = Stroke(1.6f))
    }
    // Gentle moonwell lotus & ripple ring
    val rScale = 0.42f + ((ambientTime * 0.28f) % 0.58f)
    drawOval(
        color = Color.White.copy(alpha = (1f - rScale) * 0.55f * lit),
        topLeft = pos - Offset(56f * rScale, 32f * rScale),
        size = Size(112f * rScale, 64f * rScale),
        style = Stroke(2f)
    )
    drawCircle(watercolorLerp(ParchmentLight, MeadowSage, lit), 8f, pos + Offset(18f, 8f))
    drawCircle(watercolorLerp(ParchmentLight, WildflowerPink, lit), 4.5f, pos + Offset(18f, 6f))
}

private fun DrawScope.drawBushCluster(pos: Offset, lit: Float, ink: Color) {
    drawSoftGroundShadow(pos + Offset(0f, 16f), 44f, 13f, lit)
    val fill = watercolorLerp(SketchPaper, MeadowSage, lit)
    val dark = watercolorLerp(ParchmentShadow, MeadowMoss, lit)
    val light = watercolorLerp(ParchmentLight, MeadowSun, lit)
    listOf(Offset(-20f, 2f) to 20f, Offset(20f, 4f) to 19f, Offset(0f, -6f) to 24f).forEach { (off, r) ->
        drawCircle(dark, r, pos + off + Offset(2f, 3f))
        drawCircle(fill, r, pos + off)
        drawCircle(light.copy(alpha = 0.35f), r * 0.6f, pos + off + Offset(-4f, -5f))
        drawCircle(ink, r, pos + off, style = Stroke(2f))
    }
    if (lit > 0.35f) {
        drawCircle(WildflowerPink.copy(alpha = lit), 3.2f, pos + Offset(-12f, -4f))
        drawCircle(WildflowerPink.copy(alpha = lit), 3f, pos + Offset(14f, -2f))
    }
}
