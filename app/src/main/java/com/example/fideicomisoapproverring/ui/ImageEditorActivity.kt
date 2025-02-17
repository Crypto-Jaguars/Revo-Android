package com.example.fideicomisoapproverring.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.fideicomisoapproverring.R
import java.io.ByteArrayOutputStream
import android.widget.Toast

class ImageEditorActivity : AppCompatActivity() {
    private lateinit var imagePreview: ImageView
    private lateinit var rotateButton: Button
    private lateinit var cropButton: Button
    private lateinit var saveButton: Button
    private lateinit var brightnessSeekBar: SeekBar
    private var currentBitmap: Bitmap? = null
    private var rotationDegrees = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_editor)

        initializeViews()
        setupListeners()

        // Get image URI from intent
        val imageUri = intent.getParcelableExtra<Uri>("imageUri")
        imageUri?.let { loadImage(it) }
    }

    private fun initializeViews() {
        imagePreview = findViewById(R.id.imagePreview)
        rotateButton = findViewById(R.id.rotateButton)
        cropButton = findViewById(R.id.cropButton)
        saveButton = findViewById(R.id.saveButton)
        brightnessSeekBar = findViewById(R.id.brightnessSeekBar)
    }

    private fun setupListeners() {
        rotateButton.setOnClickListener {
            rotationDegrees = (rotationDegrees + 90) % 360
            imagePreview.rotation = rotationDegrees.toFloat()
        }

        cropButton.setOnClickListener {
            currentBitmap?.let { bitmap ->
                val uri = getImageUriFromBitmap(bitmap)
                val resultIntent = Intent()
                resultIntent.data = uri
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }

        saveButton.setOnClickListener {
            currentBitmap?.let { bitmap ->
                try {
                    val uri = getImageUriFromBitmap(bitmap)
                    val resultIntent = Intent().apply {
                        data = uri
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed to save image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(this, "No image to save", Toast.LENGTH_SHORT).show()
            }
        }

        brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    adjustBrightness(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun loadImage(uri: Uri) {
        Glide.with(this)
            .asBitmap()
            .load(uri)
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    currentBitmap = resource
                    imagePreview.setImageBitmap(resource)
                }
            })
    }

    private fun adjustBrightness(progress: Int) {
        currentBitmap?.let { bitmap ->
            // Convert progress (0-100) to a brightness factor (-255 to 255)
            val brightnessFactor = (progress - 50) * 5.1f // This gives us a range of -255 to 255

            // Create a new bitmap for the adjusted image
            val adjustedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)

            // Apply brightness adjustment
            for (x in 0 until bitmap.width) {
                for (y in 0 until bitmap.height) {
                    val pixel = bitmap.getPixel(x, y)

                    // Get color channels
                    var red = ((pixel shr 16) and 0xff) + brightnessFactor
                    var green = ((pixel shr 8) and 0xff) + brightnessFactor
                    var blue = (pixel and 0xff) + brightnessFactor

                    // Clamp values
                    red = red.coerceIn(0f, 255f)
                    green = green.coerceIn(0f, 255f)
                    blue = blue.coerceIn(0f, 255f)

                    // Combine channels back into pixel
                    val newPixel = (0xff shl 24) or
                                 (red.toInt() shl 16) or
                                 (green.toInt() shl 8) or
                                 blue.toInt()

                    adjustedBitmap.setPixel(x, y, newPixel)
                }
            }

            currentBitmap = adjustedBitmap
            imagePreview.setImageBitmap(adjustedBitmap)
        }
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
    }

    companion object {
        const val REQUEST_CODE_IMAGE_EDIT = 2001
    }
}