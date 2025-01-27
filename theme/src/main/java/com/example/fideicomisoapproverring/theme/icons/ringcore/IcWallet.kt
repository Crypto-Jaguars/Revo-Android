package com.example.fideicomisoapproverring.theme.icons.ringcore

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fideicomisoapproverring.theme.icons.RingCore

public val RingCore.IcWallet: ImageVector
    get() {
        if (_icWallet != null) {
            return _icWallet!!
        }
        _icWallet =
            Builder(
                name = "IcWallet",
                defaultWidth = 19.0.dp,
                defaultHeight = 18.0.dp,
                viewportWidth = 19.0f,
                viewportHeight = 18.0f,
            ).apply {
                path(
                    fill = SolidColor(Color(0xFF141414)),
                    stroke = null,
                    strokeLineWidth = 0.0f,
                    strokeLineCap = Butt,
                    strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f,
                    pathFillType = NonZero,
                ) {
                    moveTo(2.0f, 0.0f)
                    curveTo(0.89f, 0.0f, 0.0f, 0.9f, 0.0f, 2.0f)
                    verticalLineTo(16.0f)
                    curveTo(0.0f, 16.53f, 0.211f, 17.039f, 0.586f, 17.414f)
                    curveTo(0.961f, 17.789f, 1.47f, 18.0f, 2.0f, 18.0f)
                    horizontalLineTo(16.0f)
                    curveTo(16.53f, 18.0f, 17.039f, 17.789f, 17.414f, 17.414f)
                    curveTo(17.789f, 17.039f, 18.0f, 16.53f, 18.0f, 16.0f)
                    verticalLineTo(13.72f)
                    curveTo(18.59f, 13.37f, 19.0f, 12.74f, 19.0f, 12.0f)
                    verticalLineTo(6.0f)
                    curveTo(19.0f, 5.26f, 18.59f, 4.63f, 18.0f, 4.28f)
                    verticalLineTo(2.0f)
                    curveTo(18.0f, 1.47f, 17.789f, 0.961f, 17.414f, 0.586f)
                    curveTo(17.039f, 0.211f, 16.53f, 0.0f, 16.0f, 0.0f)
                    horizontalLineTo(2.0f)
                    close()
                    moveTo(2.0f, 2.0f)
                    horizontalLineTo(16.0f)
                    verticalLineTo(4.0f)
                    horizontalLineTo(10.0f)
                    curveTo(9.47f, 4.0f, 8.961f, 4.211f, 8.586f, 4.586f)
                    curveTo(8.211f, 4.961f, 8.0f, 5.47f, 8.0f, 6.0f)
                    verticalLineTo(12.0f)
                    curveTo(8.0f, 12.53f, 8.211f, 13.039f, 8.586f, 13.414f)
                    curveTo(8.961f, 13.789f, 9.47f, 14.0f, 10.0f, 14.0f)
                    horizontalLineTo(16.0f)
                    verticalLineTo(16.0f)
                    horizontalLineTo(2.0f)
                    verticalLineTo(2.0f)
                    close()
                    moveTo(10.0f, 6.0f)
                    horizontalLineTo(17.0f)
                    verticalLineTo(12.0f)
                    horizontalLineTo(10.0f)
                    verticalLineTo(6.0f)
                    close()
                    moveTo(13.0f, 7.5f)
                    curveTo(12.602f, 7.5f, 12.221f, 7.658f, 11.939f, 7.939f)
                    curveTo(11.658f, 8.221f, 11.5f, 8.602f, 11.5f, 9.0f)
                    curveTo(11.5f, 9.398f, 11.658f, 9.779f, 11.939f, 10.061f)
                    curveTo(12.221f, 10.342f, 12.602f, 10.5f, 13.0f, 10.5f)
                    curveTo(13.398f, 10.5f, 13.779f, 10.342f, 14.061f, 10.061f)
                    curveTo(14.342f, 9.779f, 14.5f, 9.398f, 14.5f, 9.0f)
                    curveTo(14.5f, 8.602f, 14.342f, 8.221f, 14.061f, 7.939f)
                    curveTo(13.779f, 7.658f, 13.398f, 7.5f, 13.0f, 7.5f)
                    close()
                }
            }
                .build()
        return _icWallet!!
    }

private var _icWallet: ImageVector? = null

@Preview
@Composable
private fun Preview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = RingCore.IcWallet, contentDescription = "")
    }
}
