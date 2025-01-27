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

public val RingCore.IcFaq: ImageVector
    get() {
        if (_icFaq != null) {
            return _icFaq!!
        }
        _icFaq =
            Builder(
                name = "IcFaq",
                defaultWidth = 20.0.dp,
                defaultHeight = 20.0.dp,
                viewportWidth = 20.0f,
                viewportHeight = 20.0f,
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
                    moveTo(9.0f, 16.0f)
                    horizontalLineTo(11.0f)
                    verticalLineTo(14.0f)
                    horizontalLineTo(9.0f)
                    verticalLineTo(16.0f)
                    close()
                    moveTo(10.0f, 0.0f)
                    curveTo(8.687f, 0.0f, 7.386f, 0.259f, 6.173f, 0.761f)
                    curveTo(4.96f, 1.264f, 3.858f, 2.0f, 2.929f, 2.929f)
                    curveTo(1.054f, 4.804f, 0.0f, 7.348f, 0.0f, 10.0f)
                    curveTo(0.0f, 12.652f, 1.054f, 15.196f, 2.929f, 17.071f)
                    curveTo(3.858f, 18.0f, 4.96f, 18.736f, 6.173f, 19.239f)
                    curveTo(7.386f, 19.741f, 8.687f, 20.0f, 10.0f, 20.0f)
                    curveTo(12.652f, 20.0f, 15.196f, 18.946f, 17.071f, 17.071f)
                    curveTo(18.946f, 15.196f, 20.0f, 12.652f, 20.0f, 10.0f)
                    curveTo(20.0f, 8.687f, 19.741f, 7.386f, 19.239f, 6.173f)
                    curveTo(18.736f, 4.96f, 18.0f, 3.858f, 17.071f, 2.929f)
                    curveTo(16.142f, 2.0f, 15.04f, 1.264f, 13.827f, 0.761f)
                    curveTo(12.614f, 0.259f, 11.313f, 0.0f, 10.0f, 0.0f)
                    close()
                    moveTo(10.0f, 18.0f)
                    curveTo(5.59f, 18.0f, 2.0f, 14.41f, 2.0f, 10.0f)
                    curveTo(2.0f, 5.59f, 5.59f, 2.0f, 10.0f, 2.0f)
                    curveTo(14.41f, 2.0f, 18.0f, 5.59f, 18.0f, 10.0f)
                    curveTo(18.0f, 14.41f, 14.41f, 18.0f, 10.0f, 18.0f)
                    close()
                    moveTo(10.0f, 4.0f)
                    curveTo(8.939f, 4.0f, 7.922f, 4.421f, 7.172f, 5.172f)
                    curveTo(6.421f, 5.922f, 6.0f, 6.939f, 6.0f, 8.0f)
                    horizontalLineTo(8.0f)
                    curveTo(8.0f, 7.47f, 8.211f, 6.961f, 8.586f, 6.586f)
                    curveTo(8.961f, 6.211f, 9.47f, 6.0f, 10.0f, 6.0f)
                    curveTo(10.53f, 6.0f, 11.039f, 6.211f, 11.414f, 6.586f)
                    curveTo(11.789f, 6.961f, 12.0f, 7.47f, 12.0f, 8.0f)
                    curveTo(12.0f, 10.0f, 9.0f, 9.75f, 9.0f, 13.0f)
                    horizontalLineTo(11.0f)
                    curveTo(11.0f, 10.75f, 14.0f, 10.5f, 14.0f, 8.0f)
                    curveTo(14.0f, 6.939f, 13.579f, 5.922f, 12.828f, 5.172f)
                    curveTo(12.078f, 4.421f, 11.061f, 4.0f, 10.0f, 4.0f)
                    close()
                }
            }
                .build()
        return _icFaq!!
    }

private var _icFaq: ImageVector? = null

@Preview
@Composable
private fun Preview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = RingCore.IcFaq, contentDescription = "")
    }
}
