package com.example.fideicomisoapproverring.guests.model

import androidx.annotation.DrawableRes
import com.example.fideicomisoapproverring.dashboard.R

data class AgriculturalProduct(
    @DrawableRes val drawableRes: Int?,
    val price: Float,
    val unit: String,
) {
    companion object {
        val defaultItems =
            arrayOf(
                AgriculturalProduct(drawableRes = R.drawable.img_corn, price = 10000.20F, unit = "kg"),
                AgriculturalProduct(drawableRes = R.drawable.img_rice, price = 39245.00F, unit = "kg"),
                AgriculturalProduct(drawableRes = R.drawable.img_wheat_grain, price = 2890.00F, unit = "kg"),
                AgriculturalProduct(drawableRes = R.drawable.img_banana, price = 39245.00F, unit = "kg"),
                AgriculturalProduct(drawableRes = R.drawable.img_carrot, price = 900.45F, unit = "kg"),
            )
    }
}
