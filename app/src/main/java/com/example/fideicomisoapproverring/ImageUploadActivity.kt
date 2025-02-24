package com.example.fideicomisoapproverring

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.ByteArrayOutputStream

class ImageUploadActivity : AppCompatActivity() {
    private val selectedImages = mutableListOf<String>() // List to store base64 strings
    private val TARGET_SIZE = 800 // Standard size for images

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.clipData?.let { clipData ->
                // Multiple images selected
                for (i in 0 until clipData.itemCount) {
                    val imageUri = clipData.getItemAt(i).uri
                    processImage(imageUri)
                }
            } ?: result.data?.data?.let { uri ->
                // Single image selected
                processImage(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_upload)

        findViewById<Button>(R.id.btnSelectImages).setOnClickListener {
            checkPermissionAndPickImages()
        }

        // Setup RecyclerView
        findViewById<RecyclerView>(R.id.recyclerViewImages).apply {
            layoutManager = GridLayoutManager(this@ImageUploadActivity, 3)
            adapter = ImagePreviewAdapter(selectedImages)
        }
    }

    private fun checkPermissionAndPickImages() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                pickImages()
            }
            else -> {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun pickImages() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        imagePickerLauncher.launch(intent)
    }

    private fun processImage(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            var bitmap = BitmapFactory.decodeStream(inputStream)

            // Resize the bitmap while maintaining aspect ratio
            bitmap = resizeBitmap(bitmap, TARGET_SIZE)

            // Convert to base64
            val base64Image = convertBitmapToBase64(bitmap)
            selectedImages.add(base64Image)
            updateImagePreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, targetSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val ratio = width.toFloat() / height.toFloat()
        val targetWidth: Int
        val targetHeight: Int

        if (width > height) {
            targetWidth = targetSize
            targetHeight = (targetSize / ratio).toInt()
        } else {
            targetHeight = targetSize
            targetWidth = (targetSize * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun updateImagePreview() {
        (findViewById<RecyclerView>(R.id.recyclerViewImages).adapter as? ImagePreviewAdapter)?.notifyDataSetChanged()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
}