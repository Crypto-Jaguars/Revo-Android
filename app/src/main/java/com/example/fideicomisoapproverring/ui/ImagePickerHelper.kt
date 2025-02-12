import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.example.fideicomisoapproverring.ui.ImageEditorActivity
import java.io.ByteArrayOutputStream
import android.util.Base64
import java.util.ArrayList
import com.example.fideicomisoapproverring.ui.MultipleImagesActivity

class ImagePickerHelper(private val activity: AppCompatActivity) {
    private var imageCallback: ((String) -> Unit)? = null

    private val cropImage = activity.registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                val base64 = getBase64FromUri(uri)
                imageCallback?.invoke(base64)
            }
        } else {
            val error = result.error
            Toast.makeText(activity, "Image cropping failed: ${error?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchCamera(callback: (String) -> Unit) {
        imageCallback = callback
        // Launch camera intent
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        activity.startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    fun launchGallery(callback: (String) -> Unit) {
        imageCallback = callback
        // Launch gallery intent
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val imageBitmap = data?.extras?.get("data") as? Bitmap
                    imageBitmap?.let { bitmap ->
                        val uri = getImageUriFromBitmap(bitmap)
                        launchImageEditor(uri)
                    }
                }
                GALLERY_REQUEST_CODE -> {
                    val selectedImages = ArrayList<String>()
                    if (data?.clipData != null) {
                        // Multiple images selected
                        val clipData = data.clipData!!
                        for (i in 0 until clipData.itemCount) {
                            val uri = clipData.getItemAt(i).uri
                            selectedImages.add(uri.toString())
                        }
                    } else {
                        // Single image selected
                        data?.data?.let { uri ->
                            selectedImages.add(uri.toString())
                        }
                    }

                    if (selectedImages.isNotEmpty()) {
                        val intent = Intent(activity, MultipleImagesActivity::class.java)
                        intent.putStringArrayListExtra("images", selectedImages)
                        activity.startActivityForResult(intent, MULTIPLE_IMAGES_REQUEST_CODE)
                    }
                }
                ImageEditorActivity.REQUEST_CODE_IMAGE_EDIT -> {
                    data?.data?.let { uri ->
                        // Launch cropper directly after editing
                        val cropOptions = CropImageContractOptions(
                            uri,
                            CropImageOptions().apply {
                                guidelines = CropImageView.Guidelines.ON
                                aspectRatioX = 1
                                aspectRatioY = 1
                                outputCompressFormat = Bitmap.CompressFormat.JPEG
                                outputCompressQuality = 90
                            }
                        )
                        cropImage.launch(cropOptions)
                    } ?: run {
                        Toast.makeText(activity, "Failed to get edited image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(activity, "Image selection cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchImageEditor(uri: Uri) {
        val intent = Intent(activity, ImageEditorActivity::class.java).apply {
            putExtra("imageUri", uri)
        }
        activity.startActivityForResult(intent, ImageEditorActivity.REQUEST_CODE_IMAGE_EDIT)
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(activity.contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
    }

    private fun getBase64FromUri(uri: Uri): String {
        val inputStream = activity.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 1001
        private const val GALLERY_REQUEST_CODE = 1002
        private const val MULTIPLE_IMAGES_REQUEST_CODE = 1003
    }
}