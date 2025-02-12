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

public val RingCore.IcCategories: ImageVector
    get() {
        if (_icShapesPlus != null) {
            return _icShapesPlus!!
        }
        _icShapesPlus =
            Builder(
                name = "IcCategories",
                defaultWidth = 20.0.dp,
                defaultHeight =
                    20.0.dp,
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
                    moveTo(9.0f, 9.0f)
                    verticalLineTo(0.0f)
                    horizontalLineTo(0.0f)
                    verticalLineTo(9.0f)
                    moveTo(2.0f, 7.0f)
                    verticalLineTo(2.0f)
                    horizontalLineTo(7.0f)
                    verticalLineTo(7.0f)
                    moveTo(18.0f, 4.5f)
                    curveTo(18.0f, 5.9f, 16.9f, 7.0f, 15.5f, 7.0f)
                    curveTo(14.1f, 7.0f, 13.0f, 5.9f, 13.0f, 4.5f)
                    curveTo(13.0f, 3.1f, 14.11f, 2.0f, 15.5f, 2.0f)
                    curveTo(16.89f, 2.0f, 18.0f, 3.11f, 18.0f, 4.5f)
                    close()
                    moveTo(4.5f, 12.0f)
                    lineTo(0.0f, 20.0f)
                    horizontalLineTo(9.0f)
                    moveTo(5.58f, 18.0f)
                    horizontalLineTo(3.42f)
                    lineTo(4.5f, 16.08f)
                    moveTo(20.0f, 4.5f)
                    curveTo(20.0f, 2.0f, 18.0f, 0.0f, 15.5f, 0.0f)
                    curveTo(13.0f, 0.0f, 11.0f, 2.0f, 11.0f, 4.5f)
                    curveTo(11.0f, 7.0f, 13.0f, 9.0f, 15.5f, 9.0f)
                    curveTo(18.0f, 9.0f, 20.0f, 7.0f, 20.0f, 4.5f)
                    close()
                    moveTo(17.0f, 15.0f)
                    verticalLineTo(12.0f)
                    horizontalLineTo(15.0f)
                    verticalLineTo(15.0f)
                    horizontalLineTo(12.0f)
                    verticalLineTo(17.0f)
                    horizontalLineTo(15.0f)
                    verticalLineTo(20.0f)
                    horizontalLineTo(17.0f)
                    verticalLineTo(17.0f)
                    horizontalLineTo(20.0f)
                    verticalLineTo(15.0f)
                    horizontalLineTo(17.0f)
                    close()
                }
            }
                .build()
        return _icShapesPlus!!
    }

private var _icShapesPlus: ImageVector? = null

@Preview
@Composable
private fun Preview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = RingCore.IcCategories, contentDescription = "")
    }
}
