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
import kotlin.Unit

public val RingCore.IcAbout: ImageVector
    get() {
        if (_icAbout != null) {
            return _icAbout!!
        }
        _icAbout = Builder(name = "IcAbout", defaultWidth = 20.0.dp, defaultHeight = 20.0.dp,
                viewportWidth = 20.0f, viewportHeight = 20.0f).apply {
            path(fill = SolidColor(Color(0xFF141414)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(9.0f, 7.0f)
                horizontalLineTo(11.0f)
                verticalLineTo(5.0f)
                horizontalLineTo(9.0f)
                moveTo(10.0f, 18.0f)
                curveTo(5.59f, 18.0f, 2.0f, 14.41f, 2.0f, 10.0f)
                curveTo(2.0f, 5.59f, 5.59f, 2.0f, 10.0f, 2.0f)
                curveTo(14.41f, 2.0f, 18.0f, 5.59f, 18.0f, 10.0f)
                curveTo(18.0f, 14.41f, 14.41f, 18.0f, 10.0f, 18.0f)
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
                moveTo(9.0f, 15.0f)
                horizontalLineTo(11.0f)
                verticalLineTo(9.0f)
                horizontalLineTo(9.0f)
                verticalLineTo(15.0f)
                close()
            }
        }
        .build()
        return _icAbout!!
    }

private var _icAbout: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = RingCore.IcAbout, contentDescription = "")
    }
}
