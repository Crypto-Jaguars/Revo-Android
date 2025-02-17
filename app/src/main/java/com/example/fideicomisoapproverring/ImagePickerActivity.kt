package com.example.fideicomisoapproverring

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.fideicomisoapproverring.adapters.ImagePreviewAdapter
import com.example.fideicomisoapproverring.utils.ImagePickerHelper

class ImagePickerActivity : AppCompatActivity(), ImagePickerBottomSheet.ImagePickerListener {

    private lateinit var imagePickerHelper: ImagePickerHelper
    private lateinit var selectImageButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImagePreviewAdapter
    private val selectedImages = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_picker)

        imagePickerHelper = ImagePickerHelper(this)

        // Initialize views
        selectImageButton = findViewById(R.id.selectImageButton)
        recyclerView = findViewById(R.id.imagePreviewGrid)

        setupRecyclerView()

        selectImageButton.setOnClickListener {
            if (selectedImages.size >= 10) {
                Toast.makeText(this, "Maximum 10 images allowed", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showImagePickerOptions()
        }
    }

    private fun setupRecyclerView() {
        imageAdapter = ImagePreviewAdapter(
            selectedImages,
            onImageClick = { position ->
                // Launch image editor
                launchImageEditor(position)
            },
            onReorderComplete = {
                // Save new order locally
                saveImagesLocally()
            }
        )

        recyclerView.apply {
            layoutManager = GridLayoutManager(this@ImagePickerActivity, 2)
            adapter = imageAdapter
            // Add ItemTouchHelper for drag and drop
            ItemTouchHelper(createDragCallback()).attachToRecyclerView(this)
        }
    }

    private fun createDragCallback(): ItemTouchHelper.Callback {
        return object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                imageAdapter.moveItem(from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Not used
            }
        }
    }

    private fun launchImageEditor(position: Int) {
        val intent = Intent(this, ImageEditorActivity::class.java).apply {
            putExtra(ImageEditorActivity.EXTRA_IMAGE_BASE64, selectedImages[position])
            putExtra(ImageEditorActivity.EXTRA_IMAGE_POSITION, position)
        }
        startActivityForResult(intent, REQUEST_IMAGE_EDIT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_EDIT && resultCode == RESULT_OK) {
            val editedImage = data?.getStringExtra(ImageEditorActivity.EXTRA_IMAGE_BASE64)
            val position = data?.getIntExtra(ImageEditorActivity.EXTRA_IMAGE_POSITION, -1)
            if (editedImage != null && position != null && position != -1) {
                selectedImages[position] = editedImage
                imageAdapter.notifyItemChanged(position)
                saveImagesLocally()
            }
        }
    }

    private fun saveImagesLocally() {
        // TODO: Save images and their order to local storage
    }

    private fun showImagePickerOptions() {
        val bottomSheet = ImagePickerBottomSheet()
        bottomSheet.setListener(this)
        bottomSheet.show(supportFragmentManager, "ImagePicker")
    }

    override fun onCameraSelected() {
        imagePickerHelper.launchCamera { images ->
            handleImages(images)
        }
    }

    override fun onGallerySelected() {
        imagePickerHelper.launchGallery { images ->
            handleImages(images)
        }
    }

    private fun handleImages(images: List<String>) {
        selectedImages.clear()
        selectedImages.addAll(images)

        // Update UI to show selected images count
        Toast.makeText(this, "${selectedImages.size} images selected", Toast.LENGTH_SHORT).show()

        // Here you can:
        // 1. Display the images
        // 2. Upload them to your server
        // 3. Save them locally
    }

    companion object {
        private const val REQUEST_IMAGE_EDIT = 100
    }
}