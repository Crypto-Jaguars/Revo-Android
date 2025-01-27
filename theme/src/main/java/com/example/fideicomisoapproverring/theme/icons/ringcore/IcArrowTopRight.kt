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

public val RingCore.IcArrowTopRight: ImageVector
    get() {
        if (_icArrowTopRight != null) {
            return _icArrowTopRight!!
        }
        _icArrowTopRight = Builder(name = "IcArrowTopRight", defaultWidth = 16.0.dp, defaultHeight =
                16.0.dp, viewportWidth = 16.0f, viewportHeight = 16.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(3.333f, 11.727f)
                lineTo(10.393f, 4.667f)
                horizontalLineTo(6.0f)
                verticalLineTo(3.333f)
                horizontalLineTo(12.667f)
                verticalLineTo(10.0f)
                horizontalLineTo(11.333f)
                verticalLineTo(5.607f)
                lineTo(4.273f, 12.667f)
                lineTo(3.333f, 11.727f)
                close()
            }
        }
        .build()
        return _icArrowTopRight!!
    }

private var _icArrowTopRight: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = RingCore.IcArrowTopRight, contentDescription = "")
    }
}
