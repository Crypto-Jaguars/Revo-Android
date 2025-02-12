package com.example.fideicomisoapproverring.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fideicomisoapproverring.R

class MultipleImagesActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var editButton: Button
    private lateinit var uploadButton: Button
    private var selectedImages: ArrayList<Uri> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiple_images)

        // Get selected images from intent
        val uriStrings = intent.getStringArrayListExtra("images")
        selectedImages = uriStrings?.map { Uri.parse(it) } as ArrayList<Uri>

        initializeViews()
        setupRecyclerView()
        setupButtons()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.imagesRecyclerView)
        editButton = findViewById(R.id.editButton)
        uploadButton = findViewById(R.id.uploadButton)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = ImageAdapter(selectedImages) { uri ->
            // Handle image click - launch editor for single image
            val intent = Intent(this, ImageEditorActivity::class.java)
            intent.putExtra("imageUri", uri)
            startActivityForResult(intent, REQUEST_EDIT_IMAGE)
        }
    }

    private fun setupButtons() {
        uploadButton.setOnClickListener {
            // Return all images for upload
            val intent = Intent()
            intent.putParcelableArrayListExtra("editedImages", selectedImages)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_EDIT_IMAGE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                // Update the edited image in the list
                val position = selectedImages.indexOfFirst { it == data.getParcelableExtra<Uri>("originalUri") }
                if (position != -1) {
                    selectedImages[position] = uri
                    recyclerView.adapter?.notifyItemChanged(position)
                }
            }
        }
    }

    companion object {
        const val REQUEST_EDIT_IMAGE = 2002
    }
}