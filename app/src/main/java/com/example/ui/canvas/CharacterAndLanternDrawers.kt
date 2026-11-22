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
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.model.FireflyMote
import com.example.model.GladeDefinition
import com.example.model.GlowParticle
import com.example.model.KeepsakeItem
import com.example.model.LanternNode
import com.example.model.LanternStyle
import com.example.model.ParticleKind
import com.example.model.SpiritNpc
import com.example.model.SpiritSpecies
import com.example.model.WatercolorRipple
import com.example.ui.theme.BerryNotice
import com.example.ui.theme.CozyCream
import com.example.ui.theme.CozyMint
import com.example.ui.theme.InkEspresso
import com.example.ui.theme.KeeperBeanie
import com.example.ui.theme.KeeperCloak
import com.example.ui.theme.KeeperHair
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternCore
import com.example.ui.theme.LanternGlow
import com.example.ui.theme.MeadowMoss
import com.example.ui.theme.MeadowSage
import com.example.ui.theme.ParchmentLight
import com.example.ui.theme.ParchmentShadow
import com.example.ui.theme.ParchmentWarm
import com.example.ui.theme.PeachCoral
import com.example.ui.theme.SandstoneBrick
import com.example.ui.theme.SketchInkMuted
import com.example.ui.theme.SketchPaper
import com.example.ui.theme.TimberDark
import com.example.ui.theme.TimberWarm
import com.example.ui.theme.TwilightViolet
import com.example.ui.theme.WildflowerPink
import kotlin.math.abs
import kotlin.math.sin

/**
 * Draws woven light threads between lit lanterns, drifting light beads along lit paths,
 * and gentle dotted invitation guides toward reachable dormant lanterns.
 */
fun DrawScope.drawLanternThreads(
    glade: GladeDefinition,
    litLanternIds: Set<String>,
    draggingFromLantern: LanternNode?,
    dragWorldPos: Offset?,
    ambientTime: Float
) {
    val byId = glade.lanterns.associateBy { it.id }
    val drawnPairs = HashSet<String>()

    for (lantern in glade.lanterns) {
        for (targetId in lantern.connectedToIds) {
            val target = byId[targetId] ?: continue
            val pairKey = if (lantern.id < target.id) "${lantern.id}_${target.id}" else "${target.id}_${lantern.id}"
            if (!drawnPairs.add(pairKey)) continue

            val bothLit = lantern.id in litLanternIds && target.id in litLanternIds
            val oneLit = (lantern.id in litLanternIds) xor (target.id in litLanternIds)
            val p0 = lantern.position - Offset(0f, 12f)
            val p2 = target.position - Offset(0f, 12f)
            val mid = Offset((p0.x + p2.x) * 0.5f, (p0.y + p2.y) * 0.5f + 22f)
            val threadPath = Path().apply {
                moveTo(p0.x, p0.y)
                quadraticTo(mid.x, mid.y, p2.x, p2.y)
            }

            if (bothLit) {
                drawPath(
                    path = threadPath,
                    color = LanternGlow.copy(alpha = 0.48f),
                    style = Stroke(width = 10f, cap = StrokeCap.Round)
                )
                drawPath(
                    path = threadPath,
                    color = LanternAmber.copy(alpha = 0.80f),
                    style = Stroke(width = 2.6f, cap = StrokeCap.Round)
                )
                // Gentle traveling golden light bead along the woven thread
                val t = (ambientTime * 0.22f + lantern.position.x * 0.003f) % 1f
                val oneMinusT = 1f - t
                val beadPos = Offset(
                    oneMinusT * oneMinusT * p0.x + 2f * oneMinusT * t * mid.x + t * t * p2.x,
                    oneMinusT * oneMinusT * p0.y + 2f * oneMinusT * t * mid.y + t * t * p2.y
                )
                drawCircle(LanternGlow.copy(alpha = 0.65f), 6.5f, beadPos)
                drawCircle(LanternCore, 3f, beadPos)
            } else if (oneLit) {
                val dashOffset = -(ambientTime * 22f) % 24f
                drawPath(
                    path = threadPath,
                    color = LanternAmber.copy(alpha = 0.45f),
                    style = Stroke(
                        width = 2.3f,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 16f), dashOffset)
                    )
                )
            }
        }
    }

    // Active drag golden ribbon from lit lantern to user's finger
    if (draggingFromLantern != null && dragWorldPos != null) {
        val start = draggingFromLantern.position - Offset(0f, 14f)
        val mid = Offset((start.x + dragWorldPos.x) * 0.5f, (start.y + dragWorldPos.y) * 0.5f + 24f)
        val dragPath = Path().apply {
            moveTo(start.x, start.y)
            quadraticTo(mid.x, mid.y, dragWorldPos.x, dragWorldPos.y)
        }
        drawPath(dragPath, LanternGlow.copy(alpha = 0.68f), style = Stroke(13f, cap = StrokeCap.Round))
        drawPath(dragPath, LanternAmber, style = Stroke(3.6f, cap = StrokeCap.Round))
        drawCircle(LanternGlow.copy(alpha = 0.60f), 24f, dragWorldPos)
        drawCircle(LanternCore, 9.5f, dragWorldPos)
    }
}

/**
 * Draws an individual handcrafted lantern post or stone cairn with glass cap details,
 * soft pulsing candlelight, and Dewlight cost pill when dormant & reachable.
 */
fun DrawScope.drawLanternNode(
    lantern: LanternNode,
    isLit: Boolean,
    isReachable: Boolean,
    canAfford: Boolean,
    ambientTime: Float,
    textMeasurer: TextMeasurer
) {
    val pos = lantern.position
    val ink = if (isLit) InkEspresso else SketchInkMuted
    val pulse = 0.92f + sin(ambientTime * 2.2f + pos.x * 0.02f) * 0.08f

    // Soft ground shadow
    drawOval(
        color = ink.copy(alpha = 0.14f),
        topLeft = pos + Offset(-16f, 14f),
        size = Size(32f, 10f)
    )

    if (isLit) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    LanternCore.copy(alpha = 0.70f),
                    LanternGlow.copy(alpha = 0.38f),
                    Color.Transparent
                ),
                center = pos - Offset(0f, 22f),
                radius = 56f * pulse
            ),
            radius = 56f * pulse,
            center = pos - Offset(0f, 22f)
        )
    } else if (isReachable) {
        val ringR = 31f + sin(ambientTime * 2.1f) * 3f
        drawCircle(
            color = if (canAfford) LanternAmber.copy(alpha = 0.68f) else SketchInkMuted.copy(alpha = 0.45f),
            radius = ringR,
            center = pos - Offset(0f, 16f),
            style = Stroke(
                width = 2.2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), ambientTime * 12f)
            )
        )
    }

    when (lantern.style) {
        LanternStyle.HEARTH_POST, LanternStyle.IRON_HOOK -> {
            val metal = if (isLit) TimberDark else SketchInkMuted
            // Pedestal foot + vertical post
            drawOval(metal, pos + Offset(-8f, 13f), Size(16f, 7f))
            drawLine(metal, pos + Offset(0f, 16f), pos + Offset(0f, -36f), strokeWidth = 4.6f, cap = StrokeCap.Round)
            drawArc(
                color = metal,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = pos + Offset(0f, -46f),
                size = Size(20f, 20f),
                style = Stroke(3.6f, cap = StrokeCap.Round)
            )
            // Lantern roof cap & glass housing
            val bulbCenter = pos + Offset(20f, -22f)
            drawLine(metal, bulbCenter - Offset(0f, 15f), bulbCenter - Offset(0f, 11f), 2f)
            val capPath = Path().apply {
                moveTo(bulbCenter.x - 11f, bulbCenter.y - 10f)
                lineTo(bulbCenter.x, bulbCenter.y - 17f)
                lineTo(bulbCenter.x + 11f, bulbCenter.y - 10f)
                close()
            }
            drawPath(capPath, metal)
            drawRoundRect(
                color = if (isLit) LanternCore else SketchPaper,
                topLeft = bulbCenter - Offset(9f, 10f),
                size = Size(18f, 21f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = ink,
                topLeft = bulbCenter - Offset(9f, 10f),
                size = Size(18f, 21f),
                cornerRadius = CornerRadius(4f, 4f),
                style = Stroke(2.2f)
            )
            drawLine(ink.copy(alpha = 0.45f), bulbCenter - Offset(0f, 9f), bulbCenter + Offset(0f, 10f), 1.3f)
            if (isLit) {
                drawCircle(LanternAmber, 4.8f * pulse, bulbCenter + Offset(0f, 1f))
                drawCircle(LanternCore, 2.2f * pulse, bulbCenter + Offset(0f, 2f))
            }
        }
        LanternStyle.STONE_CAIRN -> {
            val stone = if (isLit) SandstoneBrick else SketchPaper
            val moss = if (isLit) MeadowSage else ParchmentWarm
            drawOval(stone, pos + Offset(-19f, 2f), Size(38f, 18f))
            drawOval(ink, pos + Offset(-19f, 2f), Size(38f, 18f), style = Stroke(2f))
            drawRoundRect(stone, pos + Offset(-13f, -24f), Size(26f, 28f), CornerRadius(6f, 6f))
            drawRoundRect(ink, pos + Offset(-13f, -24f), Size(26f, 28f), CornerRadius(6f, 6f), style = Stroke(2f))
            // Stone roof slab + moss tuft
            val slab = Path().apply {
                moveTo(pos.x - 17f, pos.y - 23f)
                lineTo(pos.x, pos.y - 34f)
                lineTo(pos.x + 17f, pos.y - 23f)
                close()
            }
            drawPath(slab, stone)
            drawPath(slab, ink, style = Stroke(2f, join = StrokeJoin.Round))
            drawCircle(moss, 4.5f, pos + Offset(-11f, 6f))
            drawCircle(if (isLit) LanternGlow else ParchmentShadow, 7.2f, pos + Offset(0f, -10f))
            drawCircle(ink, 7.2f, pos + Offset(0f, -10f), style = Stroke(1.6f))
            if (isLit) drawCircle(LanternAmber, 3.6f * pulse, pos + Offset(0f, -10f))
        }
        LanternStyle.WILLOW_HANGING -> {
            val wood = if (isLit) TimberWarm else SketchInkMuted
            drawLine(wood, pos + Offset(-8f, 18f), pos + Offset(-8f, -38f), strokeWidth = 5.6f, cap = StrokeCap.Round)
            drawLine(wood, pos + Offset(-10f, -36f), pos + Offset(14f, -36f), strokeWidth = 4.6f, cap = StrokeCap.Round)
            drawLine(wood, pos + Offset(-8f, -24f), pos + Offset(4f, -36f), strokeWidth = 2.6f, cap = StrokeCap.Round)
            val bulb = pos + Offset(12f, -19f)
            drawLine(ink, pos + Offset(12f, -35f), bulb - Offset(0f, 9f), 1.8f)
            drawCircle(if (isLit) LanternCore else SketchPaper, 10.5f, bulb)
            drawCircle(ink, 10.5f, bulb, style = Stroke(2.1f))
            if (isLit) {
                drawCircle(LanternAmber, 4.8f * pulse, bulb)
                drawCircle(LanternCore, 2.2f * pulse, bulb)
            }
        }
    }

    // Dewlight cost pill on reachable dormant lanterns
    if (!isLit && isReachable) {
        val label = if (lantern.dewCost == 0) "Free" else "${lantern.dewCost}"
        val pillCenter = pos + Offset(0f, 36f)
        val bg = if (canAfford) CozyCream else ParchmentShadow
        drawRoundRect(
            color = InkEspresso.copy(alpha = 0.12f),
            topLeft = pillCenter - Offset(21f, 9.5f),
            size = Size(44f, 22f),
            cornerRadius = CornerRadius(11f, 11f)
        )
        drawRoundRect(
            color = bg,
            topLeft = pillCenter - Offset(22f, 11f),
            size = Size(44f, 22f),
            cornerRadius = CornerRadius(11f, 11f)
        )
        drawRoundRect(
            color = InkEspresso.copy(alpha = 0.72f),
            topLeft = pillCenter - Offset(22f, 11f),
            size = Size(44f, 22f),
            cornerRadius = CornerRadius(11f, 11f),
            style = Stroke(1.6f)
        )
        if (lantern.dewCost > 0) {
            drawCircle(LanternAmber, 4.2f, pillCenter + Offset(-9.5f, 0f))
            drawCircle(LanternCore, 1.8f, pillCenter + Offset(-10.5f, -1f))
        }
        val measured = textMeasurer.measure(
            text = label,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = InkEspresso
            )
        )
        val textOffsetX = if (lantern.dewCost > 0) 4.5f else 0f
        drawText(
            textLayoutResult = measured,
            topLeft = pillCenter + Offset(textOffsetX - measured.size.width / 2f, -measured.size.height / 2f)
        )
    }
}

/**
 * Draws the cute hooded Keeper traveler with knit beanie ribbing, scarf collar,
 * tiny leather satchel, and gentle stride animation.
 */
fun DrawScope.drawKeeperCharacter(
    pos: Offset,
    isMoving: Boolean,
    isSitting: Boolean,
    facingRight: Boolean,
    ambientTime: Float
) {
    val bob = if (isMoving) abs(sin(ambientTime * 11f)) * 4.2f else sin(ambientTime * 2f) * 1.1f
    val stride = if (isMoving) sin(ambientTime * 11f) * 4.5f else 0f
    val drawPos = pos - Offset(0f, bob)

    drawOval(
        color = InkEspresso.copy(alpha = 0.18f),
        topLeft = pos + Offset(-16f, 12f),
        size = Size(32f, 10f)
    )

    val dir = if (facingRight) 1f else -1f
    scale(scaleX = dir, scaleY = 1f, pivot = drawPos) {
        if (!isSitting) {
            drawLine(InkEspresso, drawPos + Offset(-5f, 4f), drawPos + Offset(-5f - stride, 14f), 3.6f, StrokeCap.Round)
            drawLine(InkEspresso, drawPos + Offset(5f, 4f), drawPos + Offset(5f + stride, 14f), 3.6f, StrokeCap.Round)
        }

        // Cozy sage cloak + warm cream collar scarf
        val cloak = Path().apply {
            moveTo(drawPos.x - 14f, drawPos.y + 8f)
            quadraticTo(drawPos.x - 11f, drawPos.y - 10f, drawPos.x, drawPos.y - 11f)
            quadraticTo(drawPos.x + 11f, drawPos.y - 10f, drawPos.x + 14f, drawPos.y + 8f)
            close()
        }
        drawPath(cloak, KeeperCloak)
        drawPath(cloak, InkEspresso, style = Stroke(2.1f, join = StrokeJoin.Round))

        // Tiny side satchel
        drawRoundRect(TimberWarm, drawPos + Offset(-13f, -2f), Size(7f, 8f), CornerRadius(2f, 2f))
        drawRoundRect(InkEspresso, drawPos + Offset(-13f, -2f), Size(7f, 8f), CornerRadius(2f, 2f), style = Stroke(1.4f))

        // Scarf
        drawRoundRect(CozyCream, drawPos + Offset(-8f, -12f), Size(16f, 5f), CornerRadius(2.5f, 2.5f))
        drawRoundRect(InkEspresso, drawPos + Offset(-8f, -12f), Size(16f, 5f), CornerRadius(2.5f, 2.5f), style = Stroke(1.6f))

        // Ginger hair curls
        drawCircle(KeeperHair, 7.2f, drawPos + Offset(-10f, -17f))
        drawCircle(KeeperHair, 7.2f, drawPos + Offset(10f, -17f))

        // Round rosy face
        drawCircle(CozyCream, 12.5f, drawPos + Offset(0f, -21f))
        drawCircle(InkEspresso, 12.5f, drawPos + Offset(0f, -21f), style = Stroke(2.1f))
        drawCircle(WildflowerPink.copy(alpha = 0.58f), 3.1f, drawPos + Offset(-6f, -18f))
        drawCircle(WildflowerPink.copy(alpha = 0.58f), 3.1f, drawPos + Offset(6f, -18f))
        drawCircle(InkEspresso, 1.9f, drawPos + Offset(-4f, -21f))
        drawCircle(InkEspresso, 1.9f, drawPos + Offset(4f, -21f))

        // Cozy cream knit beanie with ribbed cuff & pom-pom
        drawArc(
            color = KeeperBeanie,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = drawPos + Offset(-13.5f, -39f),
            size = Size(27f, 24f)
        )
        drawArc(
            color = InkEspresso,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = drawPos + Offset(-13.5f, -39f),
            size = Size(27f, 24f),
            style = Stroke(2.1f)
        )
        drawRoundRect(KeeperBeanie, drawPos + Offset(-14f, -29f), Size(28f, 5f), CornerRadius(2.5f, 2.5f))
        drawRoundRect(InkEspresso, drawPos + Offset(-14f, -29f), Size(28f, 5f), CornerRadius(2.5f, 2.5f), style = Stroke(1.7f))
        drawCircle(KeeperBeanie, 4f, drawPos + Offset(0f, -41f))
        drawCircle(InkEspresso, 4f, drawPos + Offset(0f, -41f), style = Stroke(1.6f))
    }
}

/**
 * Draws each glade's friendly forest spirit NPC with distinct handmade silhouette details.
 */
fun DrawScope.drawSpiritNpc(
    spirit: SpiritNpc,
    isAwakened: Boolean,
    hasGiftWaiting: Boolean,
    ambientTime: Float
) {
    val pos = spirit.position
    val lit = if (isAwakened) 1f else 0.25f
    val ink = watercolorLerp(SketchInkMuted, InkEspresso, lit)
    val floatY = if (isAwakened) sin(ambientTime * 2.1f) * 2.8f else 0f
    val drawPos = pos + Offset(0f, floatY)

    drawOval(ink.copy(alpha = 0.15f), pos + Offset(-18f, 12f), Size(36f, 10f))

    when (spirit.species) {
        SpiritSpecies.POST_CROW -> {
            drawRect(watercolorLerp(SketchPaper, SandstoneBrick, lit), pos + Offset(-13f, 2f), Size(26f, 14f))
            drawRect(ink, pos + Offset(-13f, 2f), Size(26f, 14f), style = Stroke(2f))
            val body = watercolorLerp(ParchmentShadow, Color(0xFF4D5461), lit)
            // Tail & body
            drawOval(body, drawPos + Offset(-18f, -8f), Size(14f, 8f))
            drawOval(body, drawPos + Offset(-11f, -21f), Size(22f, 25f))
            drawOval(ink, drawPos + Offset(-11f, -21f), Size(22f, 25f), style = Stroke(2f))
            // Wing & mini mailbag
            drawArc(Color(0xFF39404A), 20f, 160f, true, drawPos + Offset(-10f, -15f), Size(14f, 16f))
            drawRoundRect(watercolorLerp(SketchPaper, TimberWarm, lit), drawPos + Offset(-4f, -8f), Size(8f, 7f), CornerRadius(2f, 2f))
            drawCircle(CozyCream, 2.3f, drawPos + Offset(3f, -14f))
            val beak = Path().apply {
                moveTo(drawPos.x + 10f, drawPos.y - 15f)
                lineTo(drawPos.x + 19f, drawPos.y - 12f)
                lineTo(drawPos.x + 10f, drawPos.y - 9f)
                close()
            }
            drawPath(beak, watercolorLerp(SketchPaper, LanternAmber, lit))
            drawPath(beak, ink, style = Stroke(1.6f))
        }
        SpiritSpecies.DEW_FOX -> {
            val fur = watercolorLerp(SketchPaper, PeachCoral, lit)
            val cream = watercolorLerp(ParchmentLight, CozyCream, lit)
            // Fluffy tail with cream tip
            drawOval(fur, drawPos + Offset(-24f, -10f), Size(22f, 12f))
            drawOval(cream, drawPos + Offset(-24f, -9f), Size(8f, 9f))
            drawOval(ink, drawPos + Offset(-24f, -10f), Size(22f, 12f), style = Stroke(1.8f))
            // Body & head + pointed ears
            drawOval(fur, drawPos + Offset(-10f, -14f), Size(22f, 24f))
            drawOval(ink, drawPos + Offset(-10f, -14f), Size(22f, 24f), style = Stroke(2f))
            val earL = Path().apply {
                moveTo(drawPos.x - 6f, drawPos.y - 26f)
                lineTo(drawPos.x - 9f, drawPos.y - 36f)
                lineTo(drawPos.x, drawPos.y - 30f)
                close()
            }
            val earR = Path().apply {
                moveTo(drawPos.x + 4f, drawPos.y - 30f)
                lineTo(drawPos.x + 11f, drawPos.y - 36f)
                lineTo(drawPos.x + 10f, drawPos.y - 25f)
                close()
            }
            drawPath(earL, fur); drawPath(earL, ink, style = Stroke(1.7f))
            drawPath(earR, fur); drawPath(earR, ink, style = Stroke(1.7f))
            drawCircle(fur, 11f, drawPos + Offset(2f, -20f))
            drawCircle(ink, 11f, drawPos + Offset(2f, -20f), style = Stroke(2f))
            drawCircle(ink, 1.9f, drawPos + Offset(5f, -21f))
        }
        SpiritSpecies.MOSS_TURTLE -> {
            val shell = watercolorLerp(SketchPaper, MeadowMoss, lit)
            val shellRim = watercolorLerp(ParchmentWarm, MeadowSage, lit)
            val skin = watercolorLerp(ParchmentShadow, MeadowSage, lit)
            drawArc(shell, 180f, 180f, true, drawPos + Offset(-19f, -19f), Size(38f, 32f))
            drawArc(shellRim, 195f, 150f, false, drawPos + Offset(-16f, -16f), Size(32f, 24f), style = Stroke(4f, cap = StrokeCap.Round))
            drawArc(ink, 180f, 180f, true, drawPos + Offset(-19f, -19f), Size(38f, 32f), style = Stroke(2.1f))
            // Tiny leaf sprout on shell
            drawLine(ink, drawPos + Offset(-2f, -19f), drawPos + Offset(-2f, -26f), 1.6f)
            drawCircle(shellRim, 3.2f, drawPos + Offset(1f, -26f))
            drawCircle(skin, 7.5f, drawPos + Offset(18f, -5f))
            drawCircle(ink, 7.5f, drawPos + Offset(18f, -5f), style = Stroke(1.8f))
            drawCircle(ink, 1.7f, drawPos + Offset(21f, -7f))
        }
        SpiritSpecies.STAR_OWL -> {
            val feather = watercolorLerp(SketchPaper, TwilightViolet.copy(alpha = 0.88f), lit)
            val belly = watercolorLerp(ParchmentLight, CozyCream.copy(alpha = 0.85f), lit)
            drawOval(feather, drawPos + Offset(-13f, -28f), Size(26f, 34f))
            drawOval(belly, drawPos + Offset(-8f, -12f), Size(16f, 16f))
            drawOval(ink, drawPos + Offset(-13f, -28f), Size(26f, 34f), style = Stroke(2f))
            drawCircle(CozyCream, 5.8f, drawPos + Offset(-5f, -17f))
            drawCircle(CozyCream, 5.8f, drawPos + Offset(5f, -17f))
            drawCircle(ink, 2.1f, drawPos + Offset(-5f, -17f))
            drawCircle(ink, 2.1f, drawPos + Offset(5f, -17f))
            drawCircle(LanternAmber, 2f, drawPos + Offset(0f, -13f))
        }
    }

    if (isAwakened) {
        val bubbleCenter = drawPos + Offset(0f, -45f)
        val badgeColor = if (hasGiftWaiting) BerryNotice else CozyMint
        drawCircle(InkEspresso.copy(alpha = 0.14f), 11.5f, bubbleCenter + Offset(0f, 2f))
        drawCircle(badgeColor, 11.5f, bubbleCenter)
        drawCircle(CozyCream, 11.5f, bubbleCenter, style = Stroke(2f))
        if (hasGiftWaiting) {
            drawLine(CozyCream, bubbleCenter + Offset(0f, -5f), bubbleCenter + Offset(0f, 1f), 2.5f, StrokeCap.Round)
            drawCircle(CozyCream, 1.7f, bubbleCenter + Offset(0f, 4.8f))
        } else {
            drawCircle(CozyCream, 2.8f, bubbleCenter)
        }
    }
}

/**
 * Draws an unlocked & active keepsake ornament in the glade.
 */
fun DrawScope.drawKeepsakeOrnament(item: KeepsakeItem, ambientTime: Float) {
    val p = item.position
    when (item.id) {
        "ks_tea_tray" -> {
            drawOval(InkEspresso.copy(alpha = 0.14f), p - Offset(15f, 5f), Size(30f, 16f))
            drawOval(TimberWarm, p - Offset(15f, 7f), Size(30f, 15f))
            drawOval(InkEspresso, p - Offset(15f, 7f), Size(30f, 15f), style = Stroke(1.6f))
            drawCircle(CozyCream, 5.2f, p + Offset(-4f, -3f))
            drawCircle(InkEspresso, 5.2f, p + Offset(-4f, -3f), style = Stroke(1.4f))
            drawCircle(CozyCream, 3.2f, p + Offset(6f, -1f))
            drawCircle(InkEspresso, 3.2f, p + Offset(6f, -1f), style = Stroke(1.2f))
        }
        "ks_flower_garland" -> {
            val colors = listOf(WildflowerPink, LanternGlow, CozyMint)
            drawLine(MeadowMoss, p + Offset(-20f, 0f), p + Offset(20f, 0f), 2.2f, StrokeCap.Round)
            for (i in -2..2) {
                val c = colors[abs(i) % colors.size]
                val fp = p + Offset(i * 9f, sin(i * 0.9f) * 4f)
                drawCircle(c, 5.2f, fp)
                drawCircle(InkEspresso, 5.2f, fp, style = Stroke(1.3f))
                drawCircle(CozyCream, 1.5f, fp)
            }
        }
        "ks_stone_frog" -> {
            drawOval(InkEspresso.copy(alpha = 0.14f), p - Offset(10f, 6f), Size(20f, 16f))
            drawOval(MeadowSage, p - Offset(10f, 8f), Size(20f, 16f))
            drawOval(InkEspresso, p - Offset(10f, 8f), Size(20f, 16f), style = Stroke(1.7f))
            drawCircle(CozyCream, 3.2f, p + Offset(-4f, -8f))
            drawCircle(CozyCream, 3.2f, p + Offset(4f, -8f))
            drawCircle(InkEspresso, 1.5f, p + Offset(-4f, -8f))
            drawCircle(InkEspresso, 1.5f, p + Offset(4f, -8f))
            // Tiny lucky leaf crown
            drawCircle(MeadowMoss, 3.5f, p + Offset(0f, -11f))
        }
        else -> {
            drawOval(InkEspresso.copy(alpha = 0.14f), p - Offset(14f, 7f), Size(28f, 18f))
            drawRoundRect(WildflowerPink, p - Offset(14f, 9f), Size(28f, 18f), CornerRadius(8f, 8f))
            drawRoundRect(InkEspresso, p - Offset(14f, 9f), Size(28f, 18f), CornerRadius(8f, 8f), style = Stroke(1.7f))
            drawCircle(LanternGlow, 4f + sin(ambientTime * 2f) * 0.5f, p)
        }
    }
}

fun DrawScope.drawFirefliesParticlesAndRipples(
    fireflies: List<FireflyMote>,
    particles: List<GlowParticle>,
    ripples: List<WatercolorRipple>
) {
    for (r in ripples) {
        val alpha = (1f - r.progress) * 0.52f
        val curRadius = 14f + r.maxRadius * r.progress
        val color = if (r.isWarmBloom) LanternGlow.copy(alpha = alpha) else CozyCream.copy(alpha = alpha)
        drawCircle(color, curRadius, r.center, style = Stroke(3.2f * (1f - r.progress * 0.6f)))
    }

    for (mote in fireflies) {
        val pulse = 0.85f + sin(mote.phase * 1.8f) * 0.2f
        val haloColor = if (mote.isGoldenFruit) PeachCoral else LanternGlow
        val coreColor = if (mote.isGoldenFruit) LanternAmber else LanternCore
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(haloColor.copy(alpha = 0.76f), Color.Transparent),
                center = mote.position,
                radius = mote.radius * 2.6f * pulse
            ),
            radius = mote.radius * 2.6f * pulse,
            center = mote.position
        )
        drawCircle(coreColor, mote.radius * 0.6f * pulse, mote.position)
        drawCircle(InkEspresso.copy(alpha = 0.4f), mote.radius * 0.6f * pulse, mote.position, style = Stroke(1f))
    }

    for (p in particles) {
        val alpha = (1f - (p.life / p.maxLife)).coerceIn(0f, 1f)
        val color = when (p.kind) {
            ParticleKind.SPARKLE -> LanternCore.copy(alpha = alpha)
            ParticleKind.PETAL -> WildflowerPink.copy(alpha = alpha)
            ParticleKind.LEAF -> MeadowSage.copy(alpha = alpha)
            ParticleKind.EMBER -> LanternAmber.copy(alpha = alpha)
        }
        drawCircle(color, p.size * (0.5f + 0.5f * alpha), p.position)
    }
}
