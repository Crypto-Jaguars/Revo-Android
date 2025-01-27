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

public val RingCore.IcUpwardTrend: ImageVector
    get() {
        if (_icUpwardTrend != null) {
            return _icUpwardTrend!!
        }
        _icUpwardTrend = Builder(name = "IcUpwardTrend", defaultWidth = 20.0.dp, defaultHeight =
                12.0.dp, viewportWidth = 20.0f, viewportHeight = 12.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(14.0f, 0.0f)
                lineTo(16.29f, 2.29f)
                lineTo(11.41f, 7.17f)
                lineTo(7.41f, 3.17f)
                lineTo(0.0f, 10.59f)
                lineTo(1.41f, 12.0f)
                lineTo(7.41f, 6.0f)
                lineTo(11.41f, 10.0f)
                lineTo(17.71f, 3.71f)
                lineTo(20.0f, 6.0f)
                verticalLineTo(0.0f)
                horizontalLineTo(14.0f)
                close()
            }
        }
        .build()
        return _icUpwardTrend!!
    }

private var _icUpwardTrend: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = RingCore.IcUpwardTrend, contentDescription = "")
    }
}
