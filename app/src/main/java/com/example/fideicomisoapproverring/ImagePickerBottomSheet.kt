package com.example.fideicomisoapproverring

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ImagePickerBottomSheet : BottomSheetDialogFragment() {

    interface ImagePickerListener {
        fun onCameraSelected()
        fun onGallerySelected()
    }

    private var listener: ImagePickerListener? = null

    fun setListener(listener: ImagePickerListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_image_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.cameraOption).setOnClickListener {
            listener?.onCameraSelected()
            dismiss()
        }

        view.findViewById<View>(R.id.galleryOption).setOnClickListener {
            listener?.onGallerySelected()
            dismiss()
        }
    }
}