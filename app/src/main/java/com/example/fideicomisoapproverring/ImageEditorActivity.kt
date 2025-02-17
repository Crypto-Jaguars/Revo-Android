package com.example.fideicomisoapproverring

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.ImageView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.io.ByteArrayOutputStream

class ImageEditorActivity : AppCompatActivity() {

    private lateinit var imagePreview: ImageView
    private lateinit var rotateButton: MaterialButton
    private lateinit var cropButton: MaterialButton
    private lateinit var brightnessSeekBar: SeekBar
    private lateinit var saveButton: MaterialButton
    private var currentBitmap: Bitmap? = null
    private var currentRotation = 0f

    companion object {
        const val EXTRA_IMAGE_BASE64 = "extra_image_base64"
        const val EXTRA_IMAGE_POSITION = "extra_image_position"
        private const val CROP_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_editor)

        // Initialize views
        imagePreview = findViewById(R.id.imagePreview)
        rotateButton = findViewById(R.id.rotateButton)
        cropButton = findViewById(R.id.cropButton)
        brightnessSeekBar = findViewById(R.id.brightnessSeekBar)
        saveButton = findViewById(R.id.saveButton)

        // Get image from intent
        val base64Image = intent.getStringExtra(EXTRA_IMAGE_BASE64)
        val position = intent.getIntExtra(EXTRA_IMAGE_POSITION, -1)

        // Load and display image
        base64Image?.let { loadImage(it) }

        // Set up click listeners
        rotateButton.setOnClickListener {
            rotateImage()
        }

        cropButton.setOnClickListener {
            startCrop()
        }

        brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                adjustBrightness(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        saveButton.setOnClickListener {
            saveEditedImage(position)
        }
    }

    private fun loadImage(base64Image: String) {
        val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
        currentBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        imagePreview.setImageBitmap(currentBitmap)
    }

    private fun rotateImage() {
        currentRotation += 90f
        val matrix = Matrix().apply {
            postRotate(currentRotation)
        }
        currentBitmap?.let {
            val rotatedBitmap = Bitmap.createBitmap(
                it, 0, 0, it.width, it.height, matrix, true
            )
            currentBitmap = rotatedBitmap
            imagePreview.setImageBitmap(rotatedBitmap)
        }
    }

    private fun adjustBrightness(progress: Int) {
        // Implement brightness adjustment
        // This is a simplified version - you might want to use more sophisticated image processing
        currentBitmap?.let {
            val matrix = Matrix()
            val scale = progress / 50f
            matrix.setScale(scale, scale)
            imagePreview.imageMatrix = matrix
        }
    }

    private fun startCrop() {
        currentBitmap?.let { bitmap ->
            // Create a temporary file to store the bitmap
            val tempUri = getImageUri(bitmap)
            // Launch the crop activity
            val intent = Intent("com.android.camera.action.CROP").apply {
                setDataAndType(tempUri, "image/*")
                putExtra("crop", "true")
                putExtra("aspectX", 1)
                putExtra("aspectY", 1)
                putExtra("return-data", true)
            }
            startActivityForResult(intent, CROP_REQUEST_CODE)
        }
    }

    private fun getImageUri(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmap,
            "Title",
            null
        )
        return Uri.parse(path)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CROP_REQUEST_CODE && resultCode == RESULT_OK) {
            val extras = data?.extras
            val croppedBitmap = extras?.getParcelable<Bitmap>("data")
            if (croppedBitmap != null) {
                currentBitmap = croppedBitmap
                imagePreview.setImageBitmap(croppedBitmap)
            }
        }
    }

    private fun saveEditedImage(position: Int) {
        currentBitmap?.let {
            val outputStream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val byteArray = outputStream.toByteArray()
            val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)

            // Return the edited image to the previous activity
            setResult(RESULT_OK, intent.apply {
                putExtra(EXTRA_IMAGE_BASE64, base64String)
                putExtra(EXTRA_IMAGE_POSITION, position)
            })
            finish()
        }
    }
}