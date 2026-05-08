package com.lichso.app.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object PrayerIcons {
    val Outlined: ImageVector
        get() {
            if (_outlined != null) return _outlined!!
            _outlined = ImageVector.Builder(
                name = "PrayerScrollOutlined",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 1.8f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(4f, 4f)
                    horizontalLineTo(16f)
                    curveTo(17.66f, 4f, 19f, 5.34f, 19f, 7f)
                    verticalLineTo(17f)
                    curveTo(19f, 18.66f, 20.34f, 20f, 22f, 20f)
                    horizontalLineTo(8f)
                    curveTo(6.34f, 20f, 5f, 18.66f, 5f, 17f)
                    verticalLineTo(7f)
                    curveTo(5f, 5.7f, 4.6f, 4.7f, 4f, 4f)
                    close()
                }
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 1.6f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(8f, 8f)
                    horizontalLineTo(15f)
                    moveTo(8f, 11f)
                    horizontalLineTo(14f)
                    moveTo(8f, 14f)
                    horizontalLineTo(12.6f)

                    moveTo(18.2f, 14.8f)
                    verticalLineTo(19.1f)
                    moveTo(16.2f, 20.2f)
                    curveTo(16.8f, 19.5f, 17.8f, 19.1f, 18.8f, 19.1f)
                    curveTo(19.8f, 19.1f, 20.8f, 19.5f, 21.4f, 20.2f)
                    moveTo(20.6f, 12.2f)
                    curveTo(21.4f, 12.8f, 21.4f, 13.8f, 20.6f, 14.4f)
                    curveTo(19.9f, 14.9f, 19.9f, 15.8f, 20.6f, 16.3f)
                }
            }.build()
            return _outlined!!
        }

    val Filled: ImageVector
        get() {
            if (_filled != null) return _filled!!
            _filled = ImageVector.Builder(
                name = "PrayerScrollFilled",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(4f, 4f)
                    horizontalLineTo(16f)
                    curveTo(17.66f, 4f, 19f, 5.34f, 19f, 7f)
                    verticalLineTo(17f)
                    curveTo(19f, 18.66f, 20.34f, 20f, 22f, 20f)
                    horizontalLineTo(8f)
                    curveTo(6.34f, 20f, 5f, 18.66f, 5f, 17f)
                    verticalLineTo(7f)
                    curveTo(5f, 5.7f, 4.6f, 4.7f, 4f, 4f)
                    close()
                }
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 0.42f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(8f, 8f)
                    horizontalLineTo(15f)
                    verticalLineTo(9.2f)
                    horizontalLineTo(8f)
                    close()

                    moveTo(8f, 11f)
                    horizontalLineTo(14f)
                    verticalLineTo(12.2f)
                    horizontalLineTo(8f)
                    close()

                    moveTo(8f, 14f)
                    horizontalLineTo(12.6f)
                    verticalLineTo(15.2f)
                    horizontalLineTo(8f)
                    close()
                }
                path(
                    fill = SolidColor(Color.Black),
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(17.6f, 14.6f)
                    horizontalLineTo(18.8f)
                    verticalLineTo(19.1f)
                    horizontalLineTo(17.6f)
                    close()

                    moveTo(16.2f, 20.2f)
                    curveTo(16.8f, 19.5f, 17.8f, 19.1f, 18.8f, 19.1f)
                    curveTo(19.8f, 19.1f, 20.8f, 19.5f, 21.4f, 20.2f)
                    verticalLineTo(21.2f)
                    horizontalLineTo(16.2f)
                    close()
                }
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 1.45f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    strokeAlpha = 0.55f
                ) {
                    moveTo(20.6f, 12.2f)
                    curveTo(21.4f, 12.8f, 21.4f, 13.8f, 20.6f, 14.4f)
                    curveTo(19.9f, 14.9f, 19.9f, 15.8f, 20.6f, 16.3f)
                }
            }.build()
            return _filled!!
        }
}

private var _outlined: ImageVector? = null
private var _filled: ImageVector? = null
