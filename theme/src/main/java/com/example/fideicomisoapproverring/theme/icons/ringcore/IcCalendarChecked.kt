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

public val RingCore.IcCalendarChecked: ImageVector
    get() {
        if (_icCalendarChecked != null) {
            return _icCalendarChecked!!
        }
        _icCalendarChecked =
            Builder(
                name = "IcCalendarChecked",
                defaultWidth = 33.0.dp,
                defaultHeight = 32.0.dp,
                viewportWidth = 33.0f,
                viewportHeight = 32.0f,
            ).apply {
                path(
                    fill = SolidColor(Color(0xFFffffff)),
                    stroke = null,
                    strokeLineWidth = 0.0f,
                    strokeLineCap = Butt,
                    strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f,
                    pathFillType = NonZero,
                ) {
                    moveTo(25.833f, 4.0f)
                    horizontalLineTo(24.5f)
                    verticalLineTo(1.333f)
                    horizontalLineTo(21.833f)
                    verticalLineTo(4.0f)
                    horizontalLineTo(11.167f)
                    verticalLineTo(1.333f)
                    horizontalLineTo(8.5f)
                    verticalLineTo(4.0f)
                    horizontalLineTo(7.167f)
                    curveTo(5.7f, 4.0f, 4.5f, 5.2f, 4.5f, 6.667f)
                    verticalLineTo(25.333f)
                    curveTo(4.5f, 26.813f, 5.7f, 28.0f, 7.167f, 28.0f)
                    horizontalLineTo(25.833f)
                    curveTo(27.313f, 28.0f, 28.5f, 26.813f, 28.5f, 25.333f)
                    verticalLineTo(6.667f)
                    curveTo(28.5f, 5.2f, 27.313f, 4.0f, 25.833f, 4.0f)
                    close()
                    moveTo(25.833f, 25.333f)
                    horizontalLineTo(7.167f)
                    verticalLineTo(12.0f)
                    horizontalLineTo(25.833f)
                    verticalLineTo(25.333f)
                    close()
                    moveTo(7.167f, 9.333f)
                    verticalLineTo(6.667f)
                    horizontalLineTo(25.833f)
                    verticalLineTo(9.333f)
                    horizontalLineTo(7.167f)
                    close()
                    moveTo(14.58f, 23.28f)
                    lineTo(22.5f, 15.373f)
                    lineTo(21.073f, 13.96f)
                    lineTo(14.58f, 20.453f)
                    lineTo(11.767f, 17.64f)
                    lineTo(10.353f, 19.053f)
                    lineTo(14.58f, 23.28f)
                    close()
                }
            }
                .build()
        return _icCalendarChecked!!
    }

private var _icCalendarChecked: ImageVector? = null

@Preview
@Composable
private fun Preview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = RingCore.IcCalendarChecked, contentDescription = "")
    }
}
