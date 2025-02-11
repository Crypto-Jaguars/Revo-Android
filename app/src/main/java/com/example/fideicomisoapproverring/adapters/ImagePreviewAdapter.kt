package com.example.fideicomisoapproverring.adapters

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.fideicomisoapproverring.R
import java.util.Collections

class ImagePreviewAdapter(
    private val images: MutableList<String>,
    private val onImageClick: (Int) -> Unit,
    private val onReorderComplete: () -> Unit
) : RecyclerView.Adapter<ImagePreviewAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.previewImage)
        val editButton: ImageView = view.findViewById(R.id.editButton)
        val dragHandle: ImageView = view.findViewById(R.id.dragHandle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_preview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val base64Image = images[position]
        val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        holder.imageView.setImageBitmap(bitmap)

        holder.editButton.setOnClickListener {
            onImageClick(position)
        }

        holder.dragHandle.setOnTouchListener { _, _ ->
            // Handle drag and drop reordering
            false
        }
    }

    override fun getItemCount() = images.size

    fun moveItem(fromPosition: Int, toPosition: Int) {
        Collections.swap(images, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        onReorderComplete()
    }
}