package com.lichso.app.ui.screen.prayers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Custom SVG-style icons cho Van Khan.
 * Thiet ke toi gian, outline, phu hop giao dien app Lich So.
 * Tat ca icon su dung viewBox 24x24.
 */
object PrayerIcons {

    val All: ImageVector by lazy {
        ImageVector.Builder(
            name = "PrayerAll",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(9f, 6f); lineTo(20f, 6f)
                moveTo(9f, 12f); lineTo(20f, 12f)
                moveTo(9f, 18f); lineTo(20f, 18f)
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(6.2f, 6f)
                arcTo(1.2f, 1.2f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.8f, 6f)
                arcTo(1.2f, 1.2f, 0f, isMoreThanHalf = true, isPositiveArc = true, 6.2f, 6f)
                close()
                moveTo(6.2f, 12f)
                arcTo(1.2f, 1.2f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.8f, 12f)
                arcTo(1.2f, 1.2f, 0f, isMoreThanHalf = true, isPositiveArc = true, 6.2f, 12f)
                close()
                moveTo(6.2f, 18f)
                arcTo(1.2f, 1.2f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.8f, 18f)
                arcTo(1.2f, 1.2f, 0f, isMoreThanHalf = true, isPositiveArc = true, 6.2f, 18f)
                close()
            }
        }.build()
    }

    val Candle: ImageVector by lazy {
        ImageVector.Builder(
            name = "PrayerCandle",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black), pathFillType = PathFillType.EvenOdd) {
                moveTo(12f, 2f)
                curveTo(12f, 2f, 9.5f, 5f, 9.5f, 7f)
                curveTo(9.5f, 8.38f, 10.62f, 9.5f, 12f, 9.5f)
                curveTo(13.38f, 9.5f, 14.5f, 8.38f, 14.5f, 7f)
                curveTo(14.5f, 5f, 12f, 2f, 12f, 2f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(10f, 10f)
                lineTo(10f, 20f)
                lineTo(14f, 20f)
                lineTo(14f, 10f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(8f, 20f)
                lineTo(16f, 20f)
                curveTo(16.5f, 20f, 17f, 20.5f, 17f, 21f)
                lineTo(7f, 21f)
                curveTo(7f, 20.5f, 7.5f, 20f, 8f, 20f)
                close()
            }
        }.build()
    }

    val FullMoon: ImageVector by lazy {
        ImageVector.Builder(
            name = "PrayerFullMoon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f) {
                moveTo(20f, 12f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = true, isPositiveArc = true, 4f, 12f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = true, isPositiveArc = true, 20f, 12f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Round,
                strokeAlpha = 0.2f
            ) {
                moveTo(11.2f, 9f)
                arcTo(1.2f, 1.2f, 0f, isMoreThanHalf = true, isPositiveArc = true, 8.8f, 9f)
                arcTo(1.2f, 1.2f, 0f, isMoreThanHalf = true, isPositiveArc = true, 11.2f, 9f)
                moveTo(15f, 12f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, 13f, 12f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, 15f, 12f)
                moveTo(12f, 15f)
                arcTo(0.8f, 0.8f, 0f, isMoreThanHalf = true, isPositiveArc = true, 10.4f, 15f)
                arcTo(0.8f, 0.8f, 0f, isMoreThanHalf = true, isPositiveArc = true, 12f, 15f)
            }
            path(fill = SolidColor(Color.Black), fillAlpha = 0.35f) {
                moveTo(2.8f, 4.5f)
                arcTo(0.5f, 0.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 1.8f, 4.5f)
                arcTo(0.5f, 0.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 2.8f, 4.5f)
                close()
                moveTo(22f, 6.5f)
                arcTo(0.5f, 0.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 21f, 6.5f)
                arcTo(0.5f, 0.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 22f, 6.5f)
                close()
            }
        }.build()
    }

    val NewMoon: ImageVector by lazy {
        ImageVector.Builder(
            name = "PrayerNewMoon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black), pathFillType = PathFillType.EvenOdd) {
                moveTo(14f, 3f)
                curveTo(9f, 3f, 5f, 7.03f, 5f, 12f)
                curveTo(5f, 16.97f, 9f, 21f, 14f, 21f)
                curveTo(11.5f, 19f, 10f, 15.7f, 10f, 12f)
                curveTo(10f, 8.3f, 11.5f, 5f, 14f, 3f)
                close()
            }
            path(fill = SolidColor(Color.Black), fillAlpha = 0.5f) {
                moveTo(17.1f, 5.5f)
                arcTo(0.6f, 0.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 15.9f, 5.5f)
                arcTo(0.6f, 0.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 17.1f, 5.5f)
                close()
                moveTo(19f, 9.5f)
                arcTo(0.5f, 0.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 18f, 9.5f)
                arcTo(0.5f, 0.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 19f, 9.5f)
                close()
                moveTo(17.9f, 14.5f)
                arcTo(0.4f, 0.4f, 0f, isMoreThanHalf = true, isPositiveArc = true, 17.1f, 14.5f)
                arcTo(0.4f, 0.4f, 0f, isMoreThanHalf = true, isPositiveArc = true, 17.9f, 14.5f)
                close()
            }
        }.build()
    }

    val Lantern: ImageVector by lazy {
        ImageVector.Builder(
            name = "PrayerLantern",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Round) {
                moveTo(12f, 2f); lineTo(12f, 5f)
            }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round) {
                moveTo(9f, 5f); lineTo(15f, 5f)
            }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f, strokeLineJoin = StrokeJoin.Round) {
                moveTo(9f, 5f)
                curveTo(6f, 7f, 6f, 15f, 9f, 17f)
                lineTo(15f, 17f)
                curveTo(18f, 15f, 18f, 7f, 15f, 5f)
            }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round) {
                moveTo(9f, 17f); lineTo(15f, 17f)
            }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Round) {
                moveTo(12f, 17f); lineTo(12f, 19.5f)
                moveTo(10.5f, 19.5f); lineTo(13.5f, 19.5f)
                moveTo(11f, 19.5f); lineTo(11f, 22f)
                moveTo(12f, 19.5f); lineTo(12f, 22f)
                moveTo(13f, 19.5f); lineTo(13f, 22f)
            }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.2f, strokeLineCap = StrokeCap.Round) {
                moveTo(12f, 5f); lineTo(12f, 17f)
            }
        }.build()
    }

    val House: ImageVector by lazy {
        ImageVector.Builder(
            name = "PrayerHouse",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 11f); lineTo(12f, 4f); lineTo(21f, 11f)
                moveTo(5f, 10f); lineTo(5f, 20f); lineTo(19f, 20f); lineTo(19f, 10f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(10f, 20f); lineTo(10f, 15f); lineTo(14f, 15f); lineTo(14f, 20f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.2f,
                strokeLineCap = StrokeCap.Round,
                strokeAlpha = 0.5f
            ) {
                moveTo(12f, 4f); lineTo(12f, 2f)
            }
        }.build()
    }

    val Store: ImageVector by lazy {
        ImageVector.Builder(
            name = "PrayerStore",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 3f); lineTo(21f, 3f); lineTo(20f, 9f); lineTo(4f, 9f); close()
            }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Round) {
                moveTo(4f, 9f)
                curveTo(4f, 10.5f, 6f, 10.5f, 6f, 9f)
                curveTo(6f, 10.5f, 8f, 10.5f, 8f, 9f)
                curveTo(8f, 10.5f, 10f, 10.5f, 10f, 9f)
                curveTo(10f, 10.5f, 12f, 10.5f, 12f, 9f)
                curveTo(12f, 10.5f, 14f, 10.5f, 14f, 9f)
                curveTo(14f, 10.5f, 16f, 10.5f, 16f, 9f)
                curveTo(16f, 10.5f, 18f, 10.5f, 18f, 9f)
                curveTo(18f, 10.5f, 20f, 10.5f, 20f, 9f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4f, 10f); lineTo(4f, 21f); lineTo(20f, 21f); lineTo(20f, 10f)
                moveTo(9f, 21f); lineTo(9f, 15f); lineTo(15f, 15f); lineTo(15f, 21f)
            }
        }.build()
    }

    val Temple: ImageVector by lazy {
        ImageVector.Builder(
            name = "PrayerTemple",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Round) {
                moveTo(12f, 1.5f); lineTo(12f, 4f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(5.5f, 7f)
                curveTo(6f, 7.5f, 6.5f, 7.5f, 7f, 8f)
                lineTo(12f, 4f)
                lineTo(17f, 8f)
                curveTo(17.5f, 7.5f, 18f, 7.5f, 18.5f, 7f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(2.5f, 13f)
                curveTo(3f, 13.5f, 3.5f, 13.5f, 4f, 14f)
                lineTo(12f, 8f)
                lineTo(20f, 14f)
                curveTo(20.5f, 13.5f, 21f, 13.5f, 21.5f, 13f)
            }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round) {
                moveTo(7f, 14f); lineTo(7f, 21f)
                moveTo(17f, 14f); lineTo(17f, 21f)
            }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round) {
                moveTo(5f, 21f); lineTo(19f, 21f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(10f, 21f); lineTo(10f, 17f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 14f, 17f)
                lineTo(14f, 21f)
            }
        }.build()
    }

    val Car: ImageVector by lazy {
        ImageVector.Builder(
            name = "PrayerCar",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 14f); lineTo(3f, 17f); lineTo(21f, 17f); lineTo(21f, 14f)
                lineTo(18f, 8f); lineTo(6f, 8f); close()
            }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Round) {
                moveTo(7f, 8f); lineTo(8.5f, 11f); lineTo(15.5f, 11f); lineTo(17f, 8f)
            }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f) {
                moveTo(9f, 17f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = true, isPositiveArc = true, 5f, 17f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = true, isPositiveArc = true, 9f, 17f)
            }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f) {
                moveTo(19f, 17f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = true, isPositiveArc = true, 15f, 17f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = true, isPositiveArc = true, 19f, 17f)
            }
        }.build()
    }

    val Baby: ImageVector by lazy {
        ImageVector.Builder(
            name = "PrayerBaby",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f) {
                moveTo(17f, 10f)
                arcTo(5f, 5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 7f, 10f)
                arcTo(5f, 5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 17f, 10f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(8f, 14.5f)
                curveTo(8f, 18f, 9f, 22f, 12f, 22f)
                curveTo(15f, 22f, 16f, 18f, 16f, 14.5f)
            }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Round) {
                moveTo(10f, 9.5f); lineTo(10.5f, 10f)
                moveTo(14f, 9.5f); lineTo(13.5f, 10f)
            }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.2f, strokeLineCap = StrokeCap.Round) {
                moveTo(11f, 11.5f)
                curveTo(11.3f, 12f, 12.7f, 12f, 13f, 11.5f)
            }
        }.build()
    }

    val Rice: ImageVector by lazy {
        ImageVector.Builder(
            name = "PrayerRice",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round) {
                moveTo(12f, 22f)
                curveTo(12f, 18f, 12f, 14f, 10f, 8f)
            }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Round) {
                moveTo(10f, 8f); curveTo(8f, 6f, 6f, 5f, 5f, 3f)
                moveTo(10f, 10f); curveTo(8f, 9f, 6f, 8f, 4f, 7f)
                moveTo(10.5f, 12f); curveTo(8.5f, 11.5f, 7f, 11f, 5f, 10.5f)
            }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Round) {
                moveTo(11f, 9f); curveTo(13f, 7f, 15f, 5f, 16f, 3f)
                moveTo(11.5f, 11f); curveTo(13.5f, 10f, 15.5f, 8.5f, 17f, 7f)
                moveTo(11.5f, 13f); curveTo(13.5f, 12f, 15.5f, 11f, 17.5f, 10f)
            }
            path(fill = SolidColor(Color.Black), fillAlpha = 0.5f) {
                moveTo(5.7f, 3.2f)
                arcTo(0.6f, 0.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 4.5f, 3.2f)
                arcTo(0.6f, 0.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 5.7f, 3.2f)
                close()
                moveTo(16.8f, 3.2f)
                arcTo(0.6f, 0.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 15.6f, 3.2f)
                arcTo(0.6f, 0.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 16.8f, 3.2f)
                close()
                moveTo(4.8f, 7.2f)
                arcTo(0.5f, 0.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.8f, 7.2f)
                arcTo(0.5f, 0.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 4.8f, 7.2f)
                close()
                moveTo(17.7f, 7.2f)
                arcTo(0.5f, 0.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 16.7f, 7.2f)
                arcTo(0.5f, 0.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 17.7f, 7.2f)
                close()
            }
        }.build()
    }

    val Pray: ImageVector by lazy {
        ImageVector.Builder(
            name = "PrayerPray",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(8f, 20f)
                curveTo(8f, 16f, 7f, 13f, 8f, 10f)
                curveTo(8.5f, 8f, 10f, 5f, 12f, 3f)
                curveTo(14f, 5f, 15.5f, 8f, 16f, 10f)
                curveTo(17f, 13f, 16f, 16f, 16f, 20f)
            }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.2f, strokeLineCap = StrokeCap.Round) {
                moveTo(12f, 3f); lineTo(12f, 14f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Round,
                strokeAlpha = 0.3f
            ) {
                moveTo(12f, 1f); lineTo(12f, 2.5f)
                moveTo(9f, 2f); lineTo(10f, 3f)
                moveTo(15f, 2f); lineTo(14f, 3f)
            }
        }.build()
    }

    val Wedding: ImageVector by lazy {
        ImageVector.Builder(
            name = "PrayerWedding",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 21f)
                curveTo(12f, 21f, 3f, 15f, 3f, 9f)
                curveTo(3f, 6f, 5f, 4f, 7.5f, 4f)
                curveTo(9.5f, 4f, 11f, 5.5f, 12f, 7f)
                curveTo(13f, 5.5f, 14.5f, 4f, 16.5f, 4f)
                curveTo(19f, 4f, 21f, 6f, 21f, 9f)
                curveTo(21f, 15f, 12f, 21f, 12f, 21f)
                close()
            }
        }.build()
    }

    val Ribbon: ImageVector by lazy {
        ImageVector.Builder(
            name = "PrayerRibbon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 10f)
                curveTo(10f, 8f, 4f, 6f, 4f, 9f)
                curveTo(4f, 12f, 10f, 12f, 12f, 10f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 10f)
                curveTo(14f, 8f, 20f, 6f, 20f, 9f)
                curveTo(20f, 12f, 14f, 12f, 12f, 10f)
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(13.2f, 10f)
                arcTo(1.2f, 1.2f, 0f, isMoreThanHalf = true, isPositiveArc = true, 10.8f, 10f)
                arcTo(1.2f, 1.2f, 0f, isMoreThanHalf = true, isPositiveArc = true, 13.2f, 10f)
                close()
            }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round) {
                moveTo(11f, 11f); lineTo(8f, 19f)
                moveTo(13f, 11f); lineTo(16f, 19f)
            }
        }.build()
    }

    val Pregnant: ImageVector by lazy {
        ImageVector.Builder(
            name = "PrayerPregnant",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f) {
                moveTo(14f, 5f)
                arcTo(2.5f, 2.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 9f, 5f)
                arcTo(2.5f, 2.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 14f, 5f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(11.5f, 7.5f); lineTo(11.5f, 10f)
                curveTo(11.5f, 12f, 14f, 14f, 15f, 16f)
                curveTo(15.5f, 17f, 15f, 18f, 14f, 18.5f)
                lineTo(9f, 18.5f)
                curveTo(8f, 18f, 7.5f, 17f, 8f, 16f)
                curveTo(8.5f, 14f, 9.5f, 12f, 10f, 10f)
            }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round) {
                moveTo(10f, 18.5f); lineTo(9.5f, 22f)
                moveTo(13f, 18.5f); lineTo(13.5f, 22f)
            }
        }.build()
    }

    val Beads: ImageVector by lazy {
        ImageVector.Builder(
            name = "PrayerBeads",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                // Top: (12, 4)
                moveTo(13f, 4f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, 11f, 4f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, 13f, 4f)
                close()
                // Top-right: (17.1, 5.5)
                moveTo(18.1f, 5.5f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, 16.1f, 5.5f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, 18.1f, 5.5f)
                close()
                // Right: (19.5, 10)
                moveTo(20.5f, 10f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, 18.5f, 10f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, 20.5f, 10f)
                close()
                // Right-bottom: (19.5, 14)
                moveTo(20.5f, 14f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, 18.5f, 14f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, 20.5f, 14f)
                close()
                // Bottom-right: (17.1, 18.5)
                moveTo(18.1f, 18.5f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, 16.1f, 18.5f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, 18.1f, 18.5f)
                close()
                // Bottom: (12, 20)
                moveTo(13f, 20f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, 11f, 20f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, 13f, 20f)
                close()
                // Bottom-left: (6.9, 18.5)
                moveTo(7.9f, 18.5f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, 5.9f, 18.5f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, 7.9f, 18.5f)
                close()
                // Left-bottom: (4.5, 14)
                moveTo(5.5f, 14f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.5f, 14f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, 5.5f, 14f)
                close()
                // Left: (4.5, 10)
                moveTo(5.5f, 10f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.5f, 10f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, 5.5f, 10f)
                close()
                // Top-left: (6.9, 5.5)
                moveTo(7.9f, 5.5f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, 5.9f, 5.5f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, 7.9f, 5.5f)
                close()
            }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Round) {
                moveTo(12f, 21f); lineTo(12f, 23f)
            }
        }.build()
    }

    val Village: ImageVector by lazy {
        ImageVector.Builder(
            name = "PrayerVillage",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(2f, 12f); lineTo(8f, 7f); lineTo(14f, 12f)
                moveTo(4f, 11f); lineTo(4f, 19f); lineTo(12f, 19f); lineTo(12f, 11f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.3f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(7f, 19f); lineTo(7f, 16f); lineTo(9f, 16f); lineTo(9f, 19f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(18f, 19f); lineTo(18f, 13f)
                moveTo(18f, 13f)
                curveTo(15f, 13f, 14f, 9f, 18f, 5f)
                curveTo(22f, 9f, 21f, 13f, 18f, 13f)
            }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Round) {
                moveTo(1f, 19f); lineTo(23f, 19f)
            }
        }.build()
    }

    val Hammer: ImageVector by lazy {
        ImageVector.Builder(
            name = "PrayerHammer",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6f, 3f); lineTo(14f, 3f); lineTo(14f, 8f); lineTo(6f, 8f); close()
            }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round) {
                moveTo(10f, 8f); lineTo(10f, 21f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.2f,
                strokeLineCap = StrokeCap.Round,
                strokeAlpha = 0.35f
            ) {
                moveTo(3f, 10f); lineTo(5f, 9f)
                moveTo(15f, 9f); lineTo(17f, 10f)
            }
        }.build()
    }

    val Tree: ImageVector by lazy {
        ImageVector.Builder(
            name = "PrayerTree",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round) {
                moveTo(12f, 22f); lineTo(12f, 12f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(5f, 14f); lineTo(12f, 8f); lineTo(19f, 14f); close()
                moveTo(7f, 10f); lineTo(12f, 5f); lineTo(17f, 10f); close()
                moveTo(9f, 7f); lineTo(12f, 3f); lineTo(15f, 7f); close()
            }
        }.build()
    }

    val Flag: ImageVector by lazy {
        ImageVector.Builder(
            name = "PrayerFlag",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round) {
                moveTo(6f, 2f); lineTo(6f, 22f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6f, 3f); lineTo(20f, 3f); lineTo(17f, 8f); lineTo(20f, 13f); lineTo(6f, 13f)
            }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Round) {
                moveTo(3f, 22f); lineTo(9f, 22f)
            }
        }.build()
    }

    val Incense: ImageVector by lazy {
        ImageVector.Builder(
            name = "PrayerIncense",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Round) {
                moveTo(10f, 12f); lineTo(10f, 20f)
                moveTo(12f, 12f); lineTo(12f, 20f)
                moveTo(14f, 12f); lineTo(14f, 20f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.2f,
                strokeLineCap = StrokeCap.Round,
                strokeAlpha = 0.5f
            ) {
                moveTo(10f, 12f); curveTo(9f, 9f, 8f, 7f, 9f, 4f)
                moveTo(12f, 12f); curveTo(12.5f, 9f, 11.5f, 6f, 12f, 2f)
                moveTo(14f, 12f); curveTo(15f, 9f, 16f, 7f, 15f, 4f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(7f, 20f); lineTo(17f, 20f); lineTo(16f, 22f); lineTo(8f, 22f); close()
            }
        }.build()
    }

    fun fromEmoji(emoji: String, emojiStyle: String = ""): ImageVector = when {
        emoji == "📋" -> All
        emoji == "🕯️" || emoji == "🕯" -> Candle
        emoji == "🌕" -> FullMoon
        emoji == "🌑" -> NewMoon
        emoji == "🏮" -> Lantern
        emoji == "🏠" -> House
        emoji == "🏪" -> Store
        emoji == "⛩️" || emoji == "⛩" -> Temple
        emoji == "🚗" -> Car
        emoji == "👶" -> Baby
        emoji == "🌾" -> Rice
        emoji == "🙏" -> Pray
        emoji == "💒" -> Wedding
        emoji == "🎀" -> Ribbon
        emoji == "🤰" -> Pregnant
        emoji == "��" -> Beads
        emoji == "🏡" -> Village
        emoji == "🔨" -> Hammer
        emoji == "🌳" -> Tree
        emoji == "⛳" -> Flag
        else -> Pray
    }
}
