package com.example.fideicomisoapproverring.utils

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream

class ImagePickerHelper(private val activity: AppCompatActivity) {

    companion object {
        private const val MAX_IMAGES = 10
        private const val TARGET_WIDTH = 800  // Standard width for all images
        private const val TARGET_HEIGHT = 600 // Standard height for all images
    }

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private var imageCallback: ((List<String>) -> Unit)? = null
    private val selectedImages = mutableListOf<String>()

    init {
        setupLaunchers()
    }

    private fun setupLaunchers() {
        cameraLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                imageBitmap?.let {
                    if (selectedImages.size < MAX_IMAGES) {
                        val base64Image = convertBitmapToBase64(it)
                        selectedImages.add(base64Image)
                        imageCallback?.invoke(selectedImages.toList())
                    } else {
                        Toast.makeText(activity, "Maximum 10 images allowed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        galleryLauncher = activity.registerForActivityResult(
            ActivityResultContracts.GetMultipleContents()
        ) { uris: List<Uri>? ->
            uris?.let { selectedUris ->
                val remainingSlots = MAX_IMAGES - selectedImages.size
                val urisToProcess = selectedUris.take(remainingSlots)

                urisToProcess.forEach { uri ->
                    val bitmap = MediaStore.Images.Media.getBitmap(activity.contentResolver, uri)
                    val base64Image = convertBitmapToBase64(bitmap)
                    selectedImages.add(base64Image)
                }

                if (selectedUris.size > remainingSlots) {
                    Toast.makeText(activity, "Only $remainingSlots more images allowed", Toast.LENGTH_SHORT).show()
                }

                imageCallback?.invoke(selectedImages.toList())
            }
        }
    }

    fun launchCamera(callback: (List<String>) -> Unit) {
        imageCallback = callback
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    fun launchGallery(callback: (List<String>) -> Unit) {
        imageCallback = callback
        galleryLauncher.launch("image/*")
    }

    fun clearImages() {
        selectedImages.clear()
    }

    private fun convertBitmapToBase64(originalBitmap: Bitmap): String {
        // First resize the bitmap to standard dimensions
        val resizedBitmap = resizeBitmap(originalBitmap, TARGET_WIDTH, TARGET_HEIGHT)

        val outputStream = ByteArrayOutputStream()
        // Compress with 80% quality
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()

        // Recycle the resized bitmap to free memory
        if (resizedBitmap != originalBitmap) {
            resizedBitmap.recycle()
        }

        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun resizeBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        if (bitmap.width == targetWidth && bitmap.height == targetHeight) {
            return bitmap
        }

        return Bitmap.createScaledBitmap(
            bitmap,
            targetWidth,
            targetHeight,
            true // Use bilinear filtering for better quality
        )
    }
}