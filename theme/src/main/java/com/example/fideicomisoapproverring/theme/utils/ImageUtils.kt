package com.example.fideicomisoapproverring.theme.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.widget.ImageView
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix as ComposeColorMatrix
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.request.RequestOptions

object ImageUtils {
    // Dark mode adjustments for product images
    private val darkModeMatrix = ComposeColorMatrix(
        floatArrayOf(
            0.85f, 0f, 0f, 0f, 0f,    // Red scale
            0f, 0.85f, 0f, 0f, 0f,    // Green scale
            0f, 0f, 0.85f, 0f, 0f,    // Blue scale
            0f, 0f, 0f, 1f, 0f     // Alpha scale
        )
    )
    
    // Dark mode color filter for Compose images
    val darkModeColorFilter = ColorFilter.colorMatrix(darkModeMatrix)
    
    // Dark mode color filter for traditional ImageViews
    val darkModeColorMatrixFilter = ColorMatrixColorFilter(ColorMatrix().apply {
        setScale(0.85f, 0.85f, 0.85f, 1f)
    })
    
    // Glide options for dark mode
    fun getDarkModeGlideOptions(): RequestOptions {
        return RequestOptions()
            .transform(DarkModeTransformation())
    }
    
    // Composable to get appropriate color filter based on theme
    @Composable
    fun rememberDarkModeColorFilter(): ColorFilter? {
        val isDarkMode = isSystemInDarkTheme()
        return remember(isDarkMode) {
            if (isDarkMode) darkModeColorFilter else null
        }
    }
    
    // Extension function to apply dark mode adjustments to ImageView
    fun ImageView.applyDarkModeAdjustments(isDarkMode: Boolean) {
        colorFilter = if (isDarkMode) darkModeColorMatrixFilter else null
    }
}

// Custom Glide transformation for dark mode
private class DarkModeTransformation : BitmapTransformation() {
    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val darkModeBitmap = toTransform.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(darkModeBitmap)
        val paint = Paint().apply {
            colorFilter = ImageUtils.darkModeColorMatrixFilter
        }
        canvas.drawBitmap(toTransform, 0f, 0f, paint)
        return darkModeBitmap
    }
    
    override fun updateDiskCacheKey(messageDigest: java.security.MessageDigest) {
        messageDigest.update("dark_mode_transformation".toByteArray())
    }
    
    override fun equals(other: Any?): Boolean {
        return other is DarkModeTransformation
    }
    
    override fun hashCode(): Int {
        return "dark_mode_transformation".hashCode()
    }
} 