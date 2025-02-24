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
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.layoutId
import com.example.fideicomisoapproverring.dashboard.R
import com.example.fideicomisoapproverring.theme.ui.theme.RingCoreTheme

@Composable
fun BackgroundView() {
    RingCoreTheme(darkTheme = true) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            val (topRightEllipse, rightEllipse, leftEllipse, bottomLeftEllipse) = createRefs()

            Image(
                modifier = Modifier
                    .constrainAs(topRightEllipse) {
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                        start.linkTo(parent.start)
                        horizontalBias = 0.85f
                    }
                    .offset(y = (-20).dp)
                    .blur(radius = 44.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded),
                painter = painterResource(id = R.drawable.img_purple_ellipse),
                contentDescription = null
            )

            Image(
                modifier = Modifier
                    .constrainAs(rightEllipse) {
                        top.linkTo(topRightEllipse.bottom)
                        end.linkTo(parent.end)
                    }
                    .offset(x = 88.dp)
                    .blur(radius = 48.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded),
                painter = painterResource(id = R.drawable.img_blue_ellipsis),
                contentDescription = null
            )

            Image(
                modifier = Modifier
                    .constrainAs(leftEllipse) {
                        top.linkTo(topRightEllipse.bottom, margin = 48.dp)
                        start.linkTo(parent.start)
                    }
                    .offset(x = (-88).dp)
                    .blur(radius = 48.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded),
                painter = painterResource(id = R.drawable.img_blue_ellipsis),
                contentDescription = null
            )

            Image(
                modifier = Modifier
                    .constrainAs(bottomLeftEllipse) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                    }
                    .blur(radius = 64.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded),
                painter = painterResource(id = R.drawable.img_blue_ellipsis),
                contentDescription = null
            )
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
private fun BackgroundPreview() {
    BackgroundView()
}
