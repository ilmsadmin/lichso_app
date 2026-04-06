package com.lichso.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.dp
import com.lichso.app.ui.theme.LichSoThemeColors
import kotlin.math.*
import kotlin.random.Random

// ══════════════════════════════════════════════════════════
// CalendarPatternBackground
// Hoa văn trang trí tờ lịch — Ornamental calendar artwork
// Mỗi ngày hiển thị bộ artwork khác nhau
// ══════════════════════════════════════════════════════════

/**
 * 8 bộ artwork trang trí tờ lịch — chọn ngẫu nhiên theo ngày
 * Bao gồm: viền tờ lịch, góc hoa văn, họa tiết nền, đường trang trí
 */
@Composable
fun CalendarPatternBackground(
    day: Int,
    month: Int,
    year: Int,
    modifier: Modifier = Modifier
) {
    val c = LichSoThemeColors.current
    val isDark = c.isDark

    val seed = remember(day, month, year) { day + month * 31 + year * 400 }
    val artworkIndex = remember(seed) { Random(seed).nextInt(8) }

    // ── Color palette for ornaments ──
    val ornamentPrimary = if (isDark) c.gold.copy(alpha = 0.12f) else c.primary.copy(alpha = 0.08f)
    val ornamentSecondary = if (isDark) c.gold.copy(alpha = 0.07f) else c.gold.copy(alpha = 0.10f)
    val ornamentTertiary = if (isDark) c.teal.copy(alpha = 0.06f) else c.primary.copy(alpha = 0.05f)
    val borderPrimary = if (isDark) c.gold.copy(alpha = 0.14f) else c.primary.copy(alpha = 0.10f)
    val borderSecondary = if (isDark) c.gold.copy(alpha = 0.08f) else c.gold.copy(alpha = 0.12f)
    val fillLight = if (isDark) c.gold.copy(alpha = 0.03f) else c.primary.copy(alpha = 0.02f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val m = 6.dp.toPx() // outer margin

        when (artworkIndex) {
            0 -> drawArtworkClassicFrame(w, h, m, borderPrimary, borderSecondary, ornamentPrimary, ornamentSecondary, fillLight, seed)
            1 -> drawArtworkLotusVine(w, h, m, borderPrimary, borderSecondary, ornamentPrimary, ornamentSecondary, ornamentTertiary, seed)
            2 -> drawArtworkCloudWave(w, h, m, borderPrimary, borderSecondary, ornamentPrimary, ornamentSecondary, fillLight, seed)
            3 -> drawArtworkGeometricMosaic(w, h, m, borderPrimary, borderSecondary, ornamentPrimary, ornamentSecondary, fillLight, seed)
            4 -> drawArtworkBambooBreeze(w, h, m, borderPrimary, borderSecondary, ornamentPrimary, ornamentSecondary, ornamentTertiary, seed)
            5 -> drawArtworkPhoenixFeather(w, h, m, borderPrimary, borderSecondary, ornamentPrimary, ornamentSecondary, fillLight, seed)
            6 -> drawArtworkDragonScale(w, h, m, borderPrimary, borderSecondary, ornamentPrimary, ornamentSecondary, ornamentTertiary, seed)
            7 -> drawArtworkLanternFestival(w, h, m, borderPrimary, borderSecondary, ornamentPrimary, ornamentSecondary, fillLight, seed)
        }
    }
}

// ══════════════════════════════════════════════════════════
// ARTWORK 0: CLASSIC FRAME — Khung lịch cổ điển
// Viền kép, góc cuộn, hoa tiết dọc viền
// ══════════════════════════════════════════════════════════

private fun DrawScope.drawArtworkClassicFrame(
    w: Float, h: Float, m: Float,
    bp: Color, bs: Color, op: Color, os: Color, fl: Color, seed: Int
) {
    // Double border
    drawRoundRect(bp, Offset(m, m), Size(w - m * 2, h - m * 2), CornerRadius(14.dp.toPx()), style = Stroke(1.8.dp.toPx()))
    val m2 = m + 6.dp.toPx()
    drawRoundRect(bs, Offset(m2, m2), Size(w - m2 * 2, h - m2 * 2), CornerRadius(10.dp.toPx()), style = Stroke(0.8.dp.toPx()))

    // Corner scrollwork — 4 corners
    val cs = 32.dp.toPx()
    for (q in 0..3) {
        val cx = if (q % 2 == 0) m + 3.dp.toPx() + cs / 2 else w - m - 3.dp.toPx() - cs / 2
        val cy = if (q < 2) m + 3.dp.toPx() + cs / 2 else h - m - 3.dp.toPx() - cs / 2
        drawScrollCorner(Offset(cx, cy), cs, op, q)
    }

    // Top & bottom center ornament — decorative divider
    drawCenterOrnament(Offset(w / 2, m + 1.dp.toPx()), w * 0.35f, bp, bs)
    drawCenterOrnament(Offset(w / 2, h - m - 1.dp.toPx()), w * 0.35f, bp, bs)

    // Side tick marks along border
    val rng = Random(seed)
    val tickCount = 6 + rng.nextInt(4)
    val tickLen = 4.dp.toPx()
    for (i in 1 until tickCount) {
        val ty = m + cs + i * ((h - m * 2 - cs * 2) / tickCount)
        // Left ticks
        drawLine(bs, Offset(m, ty), Offset(m + tickLen, ty), 0.7.dp.toPx())
        // Right ticks
        drawLine(bs, Offset(w - m, ty), Offset(w - m - tickLen, ty), 0.7.dp.toPx())
    }

    // Scatter faint watermark petals
    for (i in 0..3) {
        val px = w * 0.2f + rng.nextFloat() * w * 0.6f
        val py = h * 0.25f + rng.nextFloat() * h * 0.5f
        drawLotusPetal(Offset(px, py), 20.dp.toPx() + rng.nextFloat() * 12.dp.toPx(), fl, rng.nextFloat() * 360f)
    }
}

// ══════════════════════════════════════════════════════════
// ARTWORK 1: LOTUS VINE — Dây hoa sen
// Viền đơn, dây hoa sen uốn lượn hai bên
// ══════════════════════════════════════════════════════════

private fun DrawScope.drawArtworkLotusVine(
    w: Float, h: Float, m: Float,
    bp: Color, bs: Color, op: Color, os: Color, ot: Color, seed: Int
) {
    // Single elegant border
    drawRoundRect(bp, Offset(m, m), Size(w - m * 2, h - m * 2), CornerRadius(16.dp.toPx()), style = Stroke(1.5.dp.toPx()))

    // Vine along left side
    val vineStroke = 1.dp.toPx()
    val steps = 8
    val vineX = m + 10.dp.toPx()
    for (i in 0 until steps) {
        val y1 = m + 30.dp.toPx() + i * ((h - m * 2 - 60.dp.toPx()) / steps)
        val y2 = y1 + (h - m * 2 - 60.dp.toPx()) / steps
        val cx = vineX + (if (i % 2 == 0) 8.dp.toPx() else -4.dp.toPx())
        val path = Path().apply {
            moveTo(vineX, y1)
            quadraticBezierTo(cx, (y1 + y2) / 2, vineX, y2)
        }
        drawPath(path, os, style = Stroke(vineStroke, cap = StrokeCap.Round))

        // Small lotus at curve peak
        if (i % 2 == 0) {
            drawMiniLotus(Offset(cx + 2.dp.toPx(), (y1 + y2) / 2), 7.dp.toPx(), op)
        } else {
            // Leaf
            drawLeafShape(Offset(cx - 2.dp.toPx(), (y1 + y2) / 2), 5.dp.toPx(), ot, 45f)
        }
    }

    // Vine along right side (mirrored)
    val vineXr = w - m - 10.dp.toPx()
    for (i in 0 until steps) {
        val y1 = m + 30.dp.toPx() + i * ((h - m * 2 - 60.dp.toPx()) / steps)
        val y2 = y1 + (h - m * 2 - 60.dp.toPx()) / steps
        val cx = vineXr + (if (i % 2 == 0) -8.dp.toPx() else 4.dp.toPx())
        val path = Path().apply {
            moveTo(vineXr, y1)
            quadraticBezierTo(cx, (y1 + y2) / 2, vineXr, y2)
        }
        drawPath(path, os, style = Stroke(vineStroke, cap = StrokeCap.Round))

        if (i % 2 == 1) {
            drawMiniLotus(Offset(cx - 2.dp.toPx(), (y1 + y2) / 2), 7.dp.toPx(), op)
        } else {
            drawLeafShape(Offset(cx + 2.dp.toPx(), (y1 + y2) / 2), 5.dp.toPx(), ot, -45f)
        }
    }

    // Top lotus arrangement
    drawMiniLotus(Offset(w / 2, m + 14.dp.toPx()), 10.dp.toPx(), op)
    drawLeafShape(Offset(w / 2 - 16.dp.toPx(), m + 16.dp.toPx()), 6.dp.toPx(), ot, 30f)
    drawLeafShape(Offset(w / 2 + 16.dp.toPx(), m + 16.dp.toPx()), 6.dp.toPx(), ot, -30f)

    // Bottom lotus
    drawMiniLotus(Offset(w / 2, h - m - 14.dp.toPx()), 10.dp.toPx(), op)
}

// ══════════════════════════════════════════════════════════
// ARTWORK 2: CLOUD WAVE — Vân mây sóng nước
// ══════════════════════════════════════════════════════════

private fun DrawScope.drawArtworkCloudWave(
    w: Float, h: Float, m: Float,
    bp: Color, bs: Color, op: Color, os: Color, fl: Color, seed: Int
) {
    // Decorative border with wavy inner line
    drawRoundRect(bp, Offset(m, m), Size(w - m * 2, h - m * 2), CornerRadius(14.dp.toPx()), style = Stroke(1.5.dp.toPx()))

    // Top wave border ornament
    drawWaveLine(m + 20.dp.toPx(), w - m - 20.dp.toPx(), m + 8.dp.toPx(), 4.dp.toPx(), 20.dp.toPx(), bs)
    // Bottom wave
    drawWaveLine(m + 20.dp.toPx(), w - m - 20.dp.toPx(), h - m - 8.dp.toPx(), 4.dp.toPx(), 20.dp.toPx(), bs)

    // Cloud clusters in corners
    drawCloudCluster(Offset(m + 28.dp.toPx(), m + 28.dp.toPx()), 16.dp.toPx(), op)
    drawCloudCluster(Offset(w - m - 28.dp.toPx(), m + 28.dp.toPx()), 16.dp.toPx(), op)
    drawCloudCluster(Offset(m + 28.dp.toPx(), h - m - 28.dp.toPx()), 14.dp.toPx(), os)
    drawCloudCluster(Offset(w - m - 28.dp.toPx(), h - m - 28.dp.toPx()), 14.dp.toPx(), os)

    // Scattered small clouds in background
    val rng = Random(seed)
    for (i in 0..4) {
        val cx = w * 0.15f + rng.nextFloat() * w * 0.7f
        val cy = h * 0.2f + rng.nextFloat() * h * 0.6f
        drawCloudCluster(Offset(cx, cy), 10.dp.toPx() + rng.nextFloat() * 8.dp.toPx(), fl)
    }

    // Center top & bottom ornamental cloud
    drawStylizedCloud(Offset(w / 2, m + 4.dp.toPx()), 18.dp.toPx(), bp)
    drawStylizedCloud(Offset(w / 2, h - m - 4.dp.toPx()), 18.dp.toPx(), bp)
}

// ══════════════════════════════════════════════════════════
// ARTWORK 3: GEOMETRIC MOSAIC — Hoa văn hình học
// ══════════════════════════════════════════════════════════

private fun DrawScope.drawArtworkGeometricMosaic(
    w: Float, h: Float, m: Float,
    bp: Color, bs: Color, op: Color, os: Color, fl: Color, seed: Int
) {
    // Triple line border — top & bottom only (open sides feel)
    for (i in 0..2) {
        val offset = m + i * 3.dp.toPx()
        val alpha = 1f - i * 0.3f
        drawLine(bp.copy(alpha = bp.alpha * alpha), Offset(m + 16.dp.toPx(), offset), Offset(w - m - 16.dp.toPx(), offset), 0.8.dp.toPx())
        drawLine(bp.copy(alpha = bp.alpha * alpha), Offset(m + 16.dp.toPx(), h - offset), Offset(w - m - 16.dp.toPx(), h - offset), 0.8.dp.toPx())
    }

    // Corner geometric patterns — interlocking diamond grid
    val gridSize = 8.dp.toPx()
    val cornerGridCount = 3
    for (q in 0..3) {
        val startX = if (q % 2 == 0) m + 4.dp.toPx() else w - m - 4.dp.toPx() - gridSize * cornerGridCount
        val startY = if (q < 2) m + 10.dp.toPx() else h - m - 10.dp.toPx() - gridSize * cornerGridCount
        for (gx in 0 until cornerGridCount) {
            for (gy in 0 until cornerGridCount) {
                val cx = startX + gx * gridSize + gridSize / 2
                val cy = startY + gy * gridSize + gridSize / 2
                if ((gx + gy) % 2 == 0) {
                    drawDiamond(Offset(cx, cy), gridSize * 0.35f, op)
                } else {
                    drawCircle(os, gridSize * 0.2f, Offset(cx, cy))
                }
            }
        }
    }

    // Repeating diamond chain along top
    val chainY = m + 4.dp.toPx()
    val diamondSpacing = 16.dp.toPx()
    val chainStart = w * 0.25f
    val chainEnd = w * 0.75f
    var cx = chainStart
    while (cx <= chainEnd) {
        drawDiamond(Offset(cx, chainY), 3.dp.toPx(), bs)
        cx += diamondSpacing
    }
    // Bottom chain
    cx = chainStart
    while (cx <= chainEnd) {
        drawDiamond(Offset(cx, h - chainY), 3.dp.toPx(), bs)
        cx += diamondSpacing
    }

    // Faint interlocking circles in center area
    val rng = Random(seed)
    for (i in 0..2) {
        val px = w * 0.3f + rng.nextFloat() * w * 0.4f
        val py = h * 0.3f + rng.nextFloat() * h * 0.4f
        val r = 14.dp.toPx() + rng.nextFloat() * 10.dp.toPx()
        drawCircle(fl, r, Offset(px, py), style = Stroke(0.8.dp.toPx()))
        drawCircle(fl, r * 0.6f, Offset(px, py), style = Stroke(0.5.dp.toPx()))
    }

    // Side bracket decorations
    val bracketH = 24.dp.toPx()
    drawLine(bp, Offset(m, h / 2 - bracketH), Offset(m, h / 2 + bracketH), 1.dp.toPx())
    drawLine(bp, Offset(m, h / 2 - bracketH), Offset(m + 5.dp.toPx(), h / 2 - bracketH), 1.dp.toPx())
    drawLine(bp, Offset(m, h / 2 + bracketH), Offset(m + 5.dp.toPx(), h / 2 + bracketH), 1.dp.toPx())
    drawLine(bp, Offset(w - m, h / 2 - bracketH), Offset(w - m, h / 2 + bracketH), 1.dp.toPx())
    drawLine(bp, Offset(w - m, h / 2 - bracketH), Offset(w - m - 5.dp.toPx(), h / 2 - bracketH), 1.dp.toPx())
    drawLine(bp, Offset(w - m, h / 2 + bracketH), Offset(w - m - 5.dp.toPx(), h / 2 + bracketH), 1.dp.toPx())
}

// ══════════════════════════════════════════════════════════
// ARTWORK 4: BAMBOO BREEZE — Trúc lâm thanh
// ══════════════════════════════════════════════════════════

private fun DrawScope.drawArtworkBambooBreeze(
    w: Float, h: Float, m: Float,
    bp: Color, bs: Color, op: Color, os: Color, ot: Color, seed: Int
) {
    // Thin elegant border
    drawRoundRect(bp, Offset(m, m), Size(w - m * 2, h - m * 2), CornerRadius(12.dp.toPx()), style = Stroke(1.dp.toPx()))

    // Left bamboo stalks
    val rng = Random(seed)
    val stalkCount = 2 + rng.nextInt(2)
    for (s in 0 until stalkCount) {
        val sx = m + 8.dp.toPx() + s * 8.dp.toPx()
        drawBambooStalk(sx, m + 20.dp.toPx(), h - m - 20.dp.toPx(), op, os, rng)
    }

    // Right bamboo stalks
    for (s in 0 until stalkCount) {
        val sx = w - m - 8.dp.toPx() - s * 8.dp.toPx()
        drawBambooStalk(sx, m + 20.dp.toPx(), h - m - 20.dp.toPx(), op, os, rng)
    }

    // Floating bamboo leaves scattered
    for (i in 0..5) {
        val lx = w * 0.15f + rng.nextFloat() * w * 0.7f
        val ly = h * 0.15f + rng.nextFloat() * h * 0.7f
        val angle = rng.nextFloat() * 360f
        drawLeafShape(Offset(lx, ly), 8.dp.toPx() + rng.nextFloat() * 6.dp.toPx(), ot, angle)
    }

    // Top center bamboo knot ornament
    drawBambooKnot(Offset(w / 2, m + 6.dp.toPx()), 12.dp.toPx(), bp)
    drawBambooKnot(Offset(w / 2, h - m - 6.dp.toPx()), 12.dp.toPx(), bp)
}

// ══════════════════════════════════════════════════════════
// ARTWORK 5: PHOENIX FEATHER — Phượng vũ
// ══════════════════════════════════════════════════════════

private fun DrawScope.drawArtworkPhoenixFeather(
    w: Float, h: Float, m: Float,
    bp: Color, bs: Color, op: Color, os: Color, fl: Color, seed: Int
) {
    // Ornamental border with feather motif
    drawRoundRect(bp, Offset(m, m), Size(w - m * 2, h - m * 2), CornerRadius(16.dp.toPx()), style = Stroke(1.2.dp.toPx()))

    // Feather fans in corners
    for (q in 0..3) {
        val cx = if (q % 2 == 0) m + 6.dp.toPx() else w - m - 6.dp.toPx()
        val cy = if (q < 2) m + 6.dp.toPx() else h - m - 6.dp.toPx()
        val baseAngle = when (q) { 0 -> 0f; 1 -> 90f; 2 -> 270f; else -> 180f }
        drawFeatherFan(Offset(cx, cy), 28.dp.toPx(), baseAngle, op, os, 5)
    }

    // Elegant S-curves along sides
    drawSCurveOrnament(Offset(m + 2.dp.toPx(), h * 0.3f), Offset(m + 2.dp.toPx(), h * 0.7f), 6.dp.toPx(), bs)
    drawSCurveOrnament(Offset(w - m - 2.dp.toPx(), h * 0.3f), Offset(w - m - 2.dp.toPx(), h * 0.7f), -6.dp.toPx(), bs)

    // Feather-like swirl scatter
    val rng = Random(seed)
    for (i in 0..3) {
        val px = w * 0.2f + rng.nextFloat() * w * 0.6f
        val py = h * 0.2f + rng.nextFloat() * h * 0.6f
        drawFeatherSwirl(Offset(px, py), 14.dp.toPx() + rng.nextFloat() * 8.dp.toPx(), rng.nextFloat() * 360f, fl)
    }

    // Center diamond ornament on top/bottom
    drawOrnamentalDiamond(Offset(w / 2, m + 2.dp.toPx()), 8.dp.toPx(), bp, bs)
    drawOrnamentalDiamond(Offset(w / 2, h - m - 2.dp.toPx()), 8.dp.toPx(), bp, bs)
}

// ══════════════════════════════════════════════════════════
// ARTWORK 6: DRAGON SCALE — Vảy rồng
// ══════════════════════════════════════════════════════════

private fun DrawScope.drawArtworkDragonScale(
    w: Float, h: Float, m: Float,
    bp: Color, bs: Color, op: Color, os: Color, ot: Color, seed: Int
) {
    // Bold double border
    drawRoundRect(bp, Offset(m, m), Size(w - m * 2, h - m * 2), CornerRadius(14.dp.toPx()), style = Stroke(2.dp.toPx()))
    val m2 = m + 5.dp.toPx()
    drawRoundRect(bs, Offset(m2, m2), Size(w - m2 * 2, h - m2 * 2), CornerRadius(10.dp.toPx()), style = Stroke(0.7.dp.toPx()))

    // Scale pattern along top and bottom borders
    val scaleSize = 10.dp.toPx()
    val scaleY = m + 4.dp.toPx()
    var sx = m + 20.dp.toPx()
    while (sx < w - m - 20.dp.toPx()) {
        drawScaleArc(Offset(sx, scaleY), scaleSize, os, false)
        sx += scaleSize * 1.2f
    }
    sx = m + 20.dp.toPx()
    while (sx < w - m - 20.dp.toPx()) {
        drawScaleArc(Offset(sx, h - scaleY), scaleSize, os, true)
        sx += scaleSize * 1.2f
    }

    // Dragon claw corners
    for (q in 0..3) {
        val cx = if (q % 2 == 0) m + 16.dp.toPx() else w - m - 16.dp.toPx()
        val cy = if (q < 2) m + 16.dp.toPx() else h - m - 16.dp.toPx()
        drawDragonClaw(Offset(cx, cy), 18.dp.toPx(), q, op)
    }

    // Side scale strips
    val sideScaleSize = 8.dp.toPx()
    var sy = m + 36.dp.toPx()
    while (sy < h - m - 36.dp.toPx()) {
        drawScaleArc(Offset(m + 4.dp.toPx(), sy), sideScaleSize, ot, false, horizontal = false)
        drawScaleArc(Offset(w - m - 4.dp.toPx(), sy), sideScaleSize, ot, true, horizontal = false)
        sy += sideScaleSize * 1.3f
    }

    // Center subtle dragon pearl
    val rng = Random(seed)
    for (i in 0..1) {
        val px = w * 0.3f + rng.nextFloat() * w * 0.4f
        val py = h * 0.35f + rng.nextFloat() * h * 0.3f
        drawDragonPearl(Offset(px, py), 12.dp.toPx() + rng.nextFloat() * 6.dp.toPx(), op.copy(alpha = op.alpha * 0.5f))
    }
}

// ══════════════════════════════════════════════════════════
// ARTWORK 7: LANTERN FESTIVAL — Lễ hội đèn lồng
// ══════════════════════════════════════════════════════════

private fun DrawScope.drawArtworkLanternFestival(
    w: Float, h: Float, m: Float,
    bp: Color, bs: Color, op: Color, os: Color, fl: Color, seed: Int
) {
    // Festive scalloped border
    drawRoundRect(bp, Offset(m, m), Size(w - m * 2, h - m * 2), CornerRadius(14.dp.toPx()), style = Stroke(1.2.dp.toPx()))

    // Scallop pattern along top edge
    val scallops = 12
    val scW = (w - m * 2 - 20.dp.toPx()) / scallops
    for (i in 0 until scallops) {
        val cx = m + 10.dp.toPx() + i * scW + scW / 2
        drawArc(bs, 0f, 180f, false, Offset(cx - scW / 2, m - scW * 0.15f), Size(scW, scW * 0.35f), style = Stroke(0.8.dp.toPx()))
    }
    // Bottom scallops
    for (i in 0 until scallops) {
        val cx = m + 10.dp.toPx() + i * scW + scW / 2
        drawArc(bs, 180f, 180f, false, Offset(cx - scW / 2, h - m - scW * 0.2f), Size(scW, scW * 0.35f), style = Stroke(0.8.dp.toPx()))
    }

    // Lanterns hanging from top
    val rng = Random(seed)
    val lanternCount = 3 + rng.nextInt(3)
    for (i in 0 until lanternCount) {
        val lx = w * 0.12f + i * ((w * 0.76f) / (lanternCount - 1).coerceAtLeast(1))
        val stringLen = 12.dp.toPx() + rng.nextFloat() * 10.dp.toPx()
        drawLantern(Offset(lx, m + stringLen), 8.dp.toPx() + rng.nextFloat() * 4.dp.toPx(), stringLen, op, os)
    }

    // Stars/sparkles scattered
    for (i in 0..5) {
        val sx = w * 0.1f + rng.nextFloat() * w * 0.8f
        val sy = h * 0.25f + rng.nextFloat() * h * 0.5f
        drawSparkle(Offset(sx, sy), 4.dp.toPx() + rng.nextFloat() * 5.dp.toPx(), fl)
    }

    // Festive corner tassels
    for (q in 0..3) {
        val cx = if (q % 2 == 0) m + 12.dp.toPx() else w - m - 12.dp.toPx()
        val cy = if (q < 2) m + 12.dp.toPx() else h - m - 12.dp.toPx()
        drawTassel(Offset(cx, cy), 16.dp.toPx(), q, op, bs)
    }
}

// ══════════════════════════════════════════════════════════
// SHARED DRAWING PRIMITIVES
// ══════════════════════════════════════════════════════════

// ── Scroll corner (classic frame) ──
private fun DrawScope.drawScrollCorner(center: Offset, size: Float, color: Color, quadrant: Int) {
    val sx = if (quadrant % 2 == 0) 1f else -1f
    val sy = if (quadrant < 2) 1f else -1f
    val r = size * 0.4f

    // L bracket
    val path = Path().apply {
        moveTo(center.x + r * sx, center.y - r * sy)
        lineTo(center.x - r * sx, center.y - r * sy)
        quadraticBezierTo(center.x - r * sx * 1.1f, center.y - r * sy * 0.3f, center.x - r * sx, center.y + r * sy * 0.2f)
    }
    drawPath(path, color, style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round))

    val path2 = Path().apply {
        moveTo(center.x - r * sx, center.y + r * sy)
        lineTo(center.x - r * sx, center.y - r * sy)
    }
    drawPath(path2, color, style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round))

    // Curl at corner
    drawCircle(color, 2.5.dp.toPx(), Offset(center.x - r * sx, center.y - r * sy))
    // Small inner curl
    val curlPath = Path().apply {
        val ccx = center.x - r * sx * 0.6f
        val ccy = center.y - r * sy * 0.6f
        moveTo(ccx, ccy)
        quadraticBezierTo(ccx + 4.dp.toPx() * sx, ccy, ccx + 4.dp.toPx() * sx, ccy + 4.dp.toPx() * sy)
    }
    drawPath(curlPath, color.copy(alpha = color.alpha * 0.6f), style = Stroke(0.8.dp.toPx(), cap = StrokeCap.Round))
}

// ── Center ornament (line + diamond + dots) ──
private fun DrawScope.drawCenterOrnament(center: Offset, width: Float, color1: Color, color2: Color) {
    val hw = width / 2
    drawLine(color2, Offset(center.x - hw, center.y), Offset(center.x - 8.dp.toPx(), center.y), 1.dp.toPx())
    drawLine(color2, Offset(center.x + 8.dp.toPx(), center.y), Offset(center.x + hw, center.y), 1.dp.toPx())
    drawDiamond(center, 4.dp.toPx(), color1)
    drawCircle(color2, 1.5.dp.toPx(), Offset(center.x - 12.dp.toPx(), center.y))
    drawCircle(color2, 1.5.dp.toPx(), Offset(center.x + 12.dp.toPx(), center.y))
}

// ── Lotus petal watermark ──
private fun DrawScope.drawLotusPetal(center: Offset, size: Float, color: Color, rotation: Float) {
    val petalCount = 6
    for (i in 0 until petalCount) {
        val angle = (i * 60f + rotation) * PI.toFloat() / 180f
        val px = center.x + cos(angle) * size * 0.45f
        val py = center.y + sin(angle) * size * 0.45f
        val path = Path().apply {
            moveTo(center.x, center.y)
            quadraticBezierTo(
                center.x + cos(angle + 0.3f) * size * 0.3f,
                center.y + sin(angle + 0.3f) * size * 0.3f,
                px, py
            )
            quadraticBezierTo(
                center.x + cos(angle - 0.3f) * size * 0.3f,
                center.y + sin(angle - 0.3f) * size * 0.3f,
                center.x, center.y
            )
        }
        drawPath(path, color)
    }
    drawCircle(color, size * 0.08f, center)
}

// ── Mini lotus (for vine) ──
private fun DrawScope.drawMiniLotus(center: Offset, size: Float, color: Color) {
    val petals = 5
    for (i in 0 until petals) {
        val angle = (i * 72f - 90f) * PI.toFloat() / 180f
        val px = center.x + cos(angle) * size * 0.5f
        val py = center.y + sin(angle) * size * 0.5f
        val path = Path().apply {
            moveTo(center.x, center.y)
            quadraticBezierTo(
                center.x + cos(angle + 0.4f) * size * 0.35f,
                center.y + sin(angle + 0.4f) * size * 0.35f,
                px, py
            )
            quadraticBezierTo(
                center.x + cos(angle - 0.4f) * size * 0.35f,
                center.y + sin(angle - 0.4f) * size * 0.35f,
                center.x, center.y
            )
        }
        drawPath(path, color)
    }
    drawCircle(color, size * 0.12f, center)
}

// ── Leaf shape ──
private fun DrawScope.drawLeafShape(center: Offset, size: Float, color: Color, angleDeg: Float) {
    val angle = angleDeg * PI.toFloat() / 180f
    val tipX = center.x + cos(angle) * size
    val tipY = center.y + sin(angle) * size
    val path = Path().apply {
        moveTo(center.x, center.y)
        quadraticBezierTo(
            center.x + cos(angle + 0.6f) * size * 0.6f,
            center.y + sin(angle + 0.6f) * size * 0.6f,
            tipX, tipY
        )
        quadraticBezierTo(
            center.x + cos(angle - 0.6f) * size * 0.6f,
            center.y + sin(angle - 0.6f) * size * 0.6f,
            center.x, center.y
        )
    }
    drawPath(path, color)
}

// ── Diamond ──
private fun DrawScope.drawDiamond(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        lineTo(center.x + size * 0.6f, center.y)
        lineTo(center.x, center.y + size)
        lineTo(center.x - size * 0.6f, center.y)
        close()
    }
    drawPath(path, color)
}

// ── Ornamental diamond (with inner detail) ──
private fun DrawScope.drawOrnamentalDiamond(center: Offset, size: Float, color1: Color, color2: Color) {
    drawDiamond(center, size, color1)
    drawDiamond(center, size * 0.5f, color2)
    drawCircle(color1, size * 0.15f, center)
}

// ── Wave line ──
private fun DrawScope.drawWaveLine(x1: Float, x2: Float, y: Float, amp: Float, wavelength: Float, color: Color) {
    val path = Path().apply {
        moveTo(x1, y)
        var x = x1
        while (x < x2) {
            val nextX = (x + wavelength / 2).coerceAtMost(x2)
            val midX = (x + nextX) / 2
            val dir = if (((x - x1) / (wavelength / 2)).toInt() % 2 == 0) -1f else 1f
            quadraticBezierTo(midX, y + amp * dir, nextX, y)
            x = nextX
        }
    }
    drawPath(path, color, style = Stroke(0.8.dp.toPx(), cap = StrokeCap.Round))
}

// ── Cloud cluster ──
private fun DrawScope.drawCloudCluster(center: Offset, size: Float, color: Color) {
    val r = size * 0.35f
    drawCircle(color, r, center)
    drawCircle(color, r * 0.8f, Offset(center.x - r * 0.9f, center.y + r * 0.15f))
    drawCircle(color, r * 0.7f, Offset(center.x + r * 1.0f, center.y + r * 0.1f))
    drawCircle(color, r * 0.6f, Offset(center.x - r * 0.3f, center.y - r * 0.7f))
    drawCircle(color, r * 0.55f, Offset(center.x + r * 0.4f, center.y - r * 0.6f))
}

// ── Stylized cloud (single ornament) ──
private fun DrawScope.drawStylizedCloud(center: Offset, size: Float, color: Color) {
    val r = size * 0.25f
    val path = Path().apply {
        moveTo(center.x - size * 0.4f, center.y)
        quadraticBezierTo(center.x - size * 0.3f, center.y - r, center.x - size * 0.1f, center.y - r * 0.8f)
        quadraticBezierTo(center.x, center.y - r * 1.3f, center.x + size * 0.1f, center.y - r * 0.8f)
        quadraticBezierTo(center.x + size * 0.3f, center.y - r, center.x + size * 0.4f, center.y)
    }
    drawPath(path, color, style = Stroke(1.dp.toPx(), cap = StrokeCap.Round))
}

// ── Bamboo stalk ──
private fun DrawScope.drawBambooStalk(x: Float, yTop: Float, yBottom: Float, color: Color, nodeColor: Color, rng: Random) {
    drawLine(color, Offset(x, yTop), Offset(x, yBottom), 1.2.dp.toPx())
    // Nodes
    val nodeCount = 4 + rng.nextInt(3)
    for (i in 1..nodeCount) {
        val ny = yTop + i * ((yBottom - yTop) / (nodeCount + 1))
        drawLine(nodeColor, Offset(x - 3.dp.toPx(), ny), Offset(x + 3.dp.toPx(), ny), 1.5.dp.toPx(), cap = StrokeCap.Round)
    }
}

// ── Bamboo knot ornament ──
private fun DrawScope.drawBambooKnot(center: Offset, size: Float, color: Color) {
    drawLine(color, Offset(center.x - size, center.y), Offset(center.x + size, center.y), 1.dp.toPx())
    drawCircle(color, 2.dp.toPx(), Offset(center.x - size, center.y))
    drawCircle(color, 2.dp.toPx(), Offset(center.x + size, center.y))
    drawCircle(color, size * 0.2f, center, style = Stroke(0.8.dp.toPx()))
}

// ── Feather fan ──
private fun DrawScope.drawFeatherFan(center: Offset, size: Float, baseAngle: Float, color1: Color, color2: Color, count: Int) {
    for (i in 0 until count) {
        val angle = (baseAngle + i * 15f - (count - 1) * 7.5f) * PI.toFloat() / 180f
        val tipX = center.x + cos(angle) * size
        val tipY = center.y + sin(angle) * size
        val path = Path().apply {
            moveTo(center.x, center.y)
            quadraticBezierTo(
                center.x + cos(angle + 0.15f) * size * 0.7f,
                center.y + sin(angle + 0.15f) * size * 0.7f,
                tipX, tipY
            )
        }
        drawPath(path, if (i % 2 == 0) color1 else color2, style = Stroke(0.8.dp.toPx(), cap = StrokeCap.Round))
    }
    drawCircle(color1, 2.dp.toPx(), center)
}

// ── S-Curve ornament ──
private fun DrawScope.drawSCurveOrnament(start: Offset, end: Offset, amplitude: Float, color: Color) {
    val midY = (start.y + end.y) / 2
    val path = Path().apply {
        moveTo(start.x, start.y)
        cubicTo(start.x + amplitude, start.y + (midY - start.y) * 0.5f, start.x - amplitude, midY - (midY - start.y) * 0.2f, start.x, midY)
        cubicTo(start.x - amplitude, midY + (end.y - midY) * 0.2f, start.x + amplitude, end.y - (end.y - midY) * 0.5f, end.x, end.y)
    }
    drawPath(path, color, style = Stroke(0.8.dp.toPx(), cap = StrokeCap.Round))
}

// ── Feather swirl ──
private fun DrawScope.drawFeatherSwirl(center: Offset, size: Float, angleDeg: Float, color: Color) {
    val angle = angleDeg * PI.toFloat() / 180f
    val path = Path().apply {
        moveTo(center.x, center.y)
        cubicTo(
            center.x + cos(angle) * size * 0.3f, center.y + sin(angle) * size * 0.3f,
            center.x + cos(angle + 1f) * size * 0.6f, center.y + sin(angle + 1f) * size * 0.6f,
            center.x + cos(angle + 0.5f) * size, center.y + sin(angle + 0.5f) * size
        )
    }
    drawPath(path, color, style = Stroke(0.7.dp.toPx(), cap = StrokeCap.Round))
}

// ── Scale arc (dragon) ──
private fun DrawScope.drawScaleArc(center: Offset, size: Float, color: Color, flip: Boolean, horizontal: Boolean = true) {
    if (horizontal) {
        val startAngle = if (flip) 0f else 180f
        drawArc(color, startAngle, 180f, false, Offset(center.x - size / 2, center.y - size * 0.3f), Size(size, size * 0.6f), style = Stroke(0.8.dp.toPx()))
    } else {
        val startAngle = if (flip) 90f else 270f
        drawArc(color, startAngle, 180f, false, Offset(center.x - size * 0.3f, center.y - size / 2), Size(size * 0.6f, size), style = Stroke(0.8.dp.toPx()))
    }
}

// ── Dragon claw corner ──
private fun DrawScope.drawDragonClaw(center: Offset, size: Float, quadrant: Int, color: Color) {
    val sx = if (quadrant % 2 == 0) 1f else -1f
    val sy = if (quadrant < 2) 1f else -1f
    for (i in 0..2) {
        val angle = (if (quadrant == 0) 135f else if (quadrant == 1) 225f else if (quadrant == 2) 45f else 315f) + (i - 1) * 20f
        val rad = angle * PI.toFloat() / 180f
        val path = Path().apply {
            moveTo(center.x, center.y)
            quadraticBezierTo(
                center.x + cos(rad) * size * 0.5f, center.y + sin(rad) * size * 0.5f,
                center.x + cos(rad) * size * 0.8f, center.y + sin(rad) * size * 0.8f
            )
        }
        drawPath(path, color, style = Stroke(1.dp.toPx(), cap = StrokeCap.Round))
    }
    drawCircle(color, 2.dp.toPx(), center)
}

// ── Dragon pearl ──
private fun DrawScope.drawDragonPearl(center: Offset, size: Float, color: Color) {
    drawCircle(color, size, center, style = Stroke(1.dp.toPx()))
    drawCircle(color, size * 0.65f, center, style = Stroke(0.6.dp.toPx()))
    drawCircle(color, size * 0.2f, center)
    // Flame wisps
    for (i in 0..3) {
        val angle = (i * 90f + 45f) * PI.toFloat() / 180f
        val start = Offset(center.x + cos(angle) * size, center.y + sin(angle) * size)
        val end = Offset(center.x + cos(angle) * size * 1.4f, center.y + sin(angle) * size * 1.3f)
        val ctrl = Offset(center.x + cos(angle + 0.3f) * size * 1.25f, center.y + sin(angle + 0.3f) * size * 1.2f)
        val path = Path().apply {
            moveTo(start.x, start.y)
            quadraticBezierTo(ctrl.x, ctrl.y, end.x, end.y)
        }
        drawPath(path, color, style = Stroke(0.6.dp.toPx(), cap = StrokeCap.Round))
    }
}

// ── Lantern ──
private fun DrawScope.drawLantern(center: Offset, size: Float, stringLen: Float, color1: Color, color2: Color) {
    // String
    drawLine(color2, Offset(center.x, center.y - stringLen), Offset(center.x, center.y - size * 0.6f), 0.5.dp.toPx())
    // Body oval
    val path = Path().apply {
        moveTo(center.x, center.y - size * 0.8f)
        cubicTo(center.x + size, center.y - size * 0.4f, center.x + size, center.y + size * 0.4f, center.x, center.y + size * 0.8f)
        cubicTo(center.x - size, center.y + size * 0.4f, center.x - size, center.y - size * 0.4f, center.x, center.y - size * 0.8f)
    }
    drawPath(path, color1, style = Stroke(1.dp.toPx()))
    // Top & bottom caps
    drawLine(color1, Offset(center.x - size * 0.4f, center.y - size * 0.7f), Offset(center.x + size * 0.4f, center.y - size * 0.7f), 1.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color1, Offset(center.x - size * 0.3f, center.y + size * 0.7f), Offset(center.x + size * 0.3f, center.y + size * 0.7f), 1.dp.toPx(), cap = StrokeCap.Round)
    // Tassel
    drawLine(color2, Offset(center.x, center.y + size * 0.8f), Offset(center.x, center.y + size * 1.2f), 0.5.dp.toPx())
}

// ── Sparkle ──
private fun DrawScope.drawSparkle(center: Offset, size: Float, color: Color) {
    val arms = 4
    for (i in 0 until arms) {
        val angle = (i * 90f + 45f) * PI.toFloat() / 180f
        drawLine(color, center, Offset(center.x + cos(angle) * size, center.y + sin(angle) * size), 0.6.dp.toPx(), cap = StrokeCap.Round)
    }
    drawCircle(color, size * 0.15f, center)
}

// ── Tassel corner ──
private fun DrawScope.drawTassel(center: Offset, size: Float, quadrant: Int, color1: Color, color2: Color) {
    val sx = if (quadrant % 2 == 0) 1f else -1f
    val sy = if (quadrant < 2) 1f else -1f
    // Knot
    drawCircle(color1, 3.dp.toPx(), center)
    // Tassel strings
    for (i in 0..2) {
        val endX = center.x + (4.dp.toPx() + i * 3.dp.toPx()) * sx
        val endY = center.y + (8.dp.toPx() + i * 2.dp.toPx()) * sy
        val path = Path().apply {
            moveTo(center.x, center.y)
            quadraticBezierTo(center.x + 2.dp.toPx() * sx, (center.y + endY) / 2, endX, endY)
        }
        drawPath(path, color2, style = Stroke(0.6.dp.toPx(), cap = StrokeCap.Round))
    }
}
