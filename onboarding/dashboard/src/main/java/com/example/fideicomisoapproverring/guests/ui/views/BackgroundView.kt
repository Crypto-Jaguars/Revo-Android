package com.example.fideicomisoapproverring.guests.ui.views

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.fideicomisoapproverring.dashboard.R
import com.example.fideicomisoapproverring.theme.ui.theme.RingCoreTheme
import com.example.fideicomisoapproverring.theme.utils.ImageUtils

@Composable
fun BackgroundView(
    modifier: Modifier = Modifier.background(MaterialTheme.colorScheme.background)
) {
    val colorFilter = ImageUtils.rememberDarkModeColorFilter()
    
    ConstraintLayout(
        modifier = modifier.fillMaxSize()
    ) {
        val (
            topRightEllipseConstraint,
            leftEllipseConstraint,
            bottomLeftEllipseConstraint
        ) = createRefs()

        Image(
            modifier =
                Modifier.constrainAs(topRightEllipseConstraint) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                }.offset(x = 88.dp)
                    .blur(radius = 48.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded),
            painter = painterResource(id = R.drawable.img_blue_ellipsis),
            contentDescription = null,
            colorFilter = colorFilter
        )

        Image(
            modifier =
                Modifier.constrainAs(leftEllipseConstraint) {
                    top.linkTo(topRightEllipseConstraint.bottom, margin = 48.dp)
                    start.linkTo(parent.start)
                }.offset(x = (-88).dp)
                    .blur(radius = 48.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded),
            painter = painterResource(id = R.drawable.img_blue_ellipsis),
            contentDescription = null,
            colorFilter = colorFilter
        )

        Image(
            modifier =
                Modifier.constrainAs(bottomLeftEllipseConstraint) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                }.blur(radius = 64.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded),
            painter = painterResource(id = R.drawable.img_blue_ellipsis),
            contentDescription = null,
            colorFilter = colorFilter
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Composable
private fun BackgroundPreview() {
    RingCoreTheme {
        BackgroundView()
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun BackgroundDarkPreview() {
    RingCoreTheme {
        BackgroundView()
    }
}
