package com.lightningkite.kotlin.anko.viewcontrollers.image

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.util.Log
import com.lightningkite.kotlin.anko.image.getBitmapFromUri
import com.lightningkite.kotlin.anko.selector
import com.lightningkite.kotlin.anko.viewcontrollers.implementations.VCActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

fun VCActivity.dialogImageUri(fileProviderAuthority: String, cameraRes: Int, galleryRes: Int, onResult: (Uri?) -> Unit) {
    selector(
            null,
            cameraRes to {
                requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    getImageUriFromCamera(fileProviderAuthority = fileProviderAuthority) {
                        Log.i("ImageUploadLayout", it.toString())
                        onResult(it)
                    }
                }
            },
            galleryRes to {
                requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE) {
                    getImageUriFromGallery() {
                        Log.i("ImageUploadLayout", it.toString())
                        onResult(it)
                    }
                }
            }
    )
}

fun VCActivity.dialogImage(minBytes: Long, cameraRes: Int, galleryRes: Int, onResult: (Bitmap?) -> Unit) {
    selector(
            null,
            cameraRes to {
                requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    getImageFromCamera(minBytes) {
                        onResult(it)
                    }
                }
            },
            galleryRes to {
                requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE) {
                    getImageFromGallery(minBytes) {
                        onResult(it)
                    }
                }
            }
    )
}

/**
 * Pops up a dialog for getting an image from the gallery, returning it in [onResult].
 */
fun VCActivity.getImageUriFromGallery(onResult: (Uri?) -> Unit) {
    val getIntent = Intent(Intent.ACTION_GET_CONTENT)
    getIntent.type = "image/*"

    val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    pickIntent.type = "image/*"

    val chooserIntent = Intent.createChooser(getIntent, "Select Image")
    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))

    this.startIntent(chooserIntent) { code, data ->
        if (code != Activity.RESULT_OK) {
            onResult(null); return@startIntent
        }
        if (data == null) return@startIntent
        val imageUri = data.data
        onResult(imageUri)
    }
}
//
///**
// * Opens the camera to take a picture, returning it in [onResult].
// */
//fun VCActivity.getImageUriFromCamera(onResult: (Uri?) -> Unit) {
//    try {
//        val folder = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//                ?: Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
//                ?: Environment.getDownloadCacheDirectory()
//        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//
//        folder.mkdir()
//
//        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//        val file = File.createTempFile(timeStamp, ".jpg", folder)
//        val potentialFile: Uri = Uri.fromFile(file)
//
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, potentialFile)
//        this.startIntent(intent) { code, data ->
//            if (code != Activity.RESULT_OK) {
//                onResult(null); return@startIntent
//            }
//            println("Actual data:" + data?.data?.toString())
//            //val fixedUri = Uri.fromFile(File((data?.data ?: potentialFile).getRealPath(this)))
//            val fixedUri = File((data?.data ?: potentialFile).getRealPath(this)).toImageContentUri(this)
//
//            onResult(fixedUri)
//        }
//    } catch(e: Exception) {
//        e.printStackTrace()
//        onResult(null)
//    }
//}

/**
 * Opens the camera to take a picture, returning it in [onResult].
 */
fun VCActivity.getImageUriFromCamera(fileProviderAuthority: String, onResult: (Uri?) -> Unit) {
    try {
        val folder = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        folder.mkdir()

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val file = File.createTempFile(timeStamp, ".jpg", folder)
        val potentialFile: Uri = FileProvider.getUriForFile(this, fileProviderAuthority, file)

        intent.putExtra(MediaStore.EXTRA_OUTPUT, potentialFile)
        this.startIntent(intent) { code, data ->
            if (code != Activity.RESULT_OK) {
                onResult(null); return@startIntent
            }
            println("Actual data:" + data?.data?.toString())
            //val fixedUri = Uri.fromFile(File((data?.data ?: potentialFile).getRealPath(this)))
//            val fixedUri = File((data?.data ?: potentialFile).getRealPath(this)).toImageContentUri(this)

            onResult(data?.data ?: potentialFile)
        }
    } catch(e: Exception) {
        e.printStackTrace()
        onResult(null)
    }
}

/**
 * Pops up a dialog for getting an image from the gallery, returning it in [onResult].
 */
fun VCActivity.getImageFromGallery(minBytes: Int, onResult: (Bitmap?) -> Unit) {
    val getIntent = Intent(Intent.ACTION_GET_CONTENT)
    getIntent.type = "image/*"

    val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    pickIntent.type = "image/*"

    val chooserIntent = Intent.createChooser(getIntent, "Select Image")
    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))

    this.startIntent(chooserIntent) { code, data ->
        if (code != Activity.RESULT_OK) {
            onResult(null); return@startIntent
        }
        if (data == null) return@startIntent
        val imageUri = data.data
        onResult(getBitmapFromUri(imageUri, minBytes))
    }
}

/**
 * Opens the camera to take a picture, returning it in [onResult].
 */
fun VCActivity.getImageFromCamera(minBytes: Int, onResult: (Bitmap?) -> Unit) {
    val folder = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    if (folder == null) {
        onResult(null)
        return;
    }

    folder.mkdir()

    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val file = File(folder, "image_" + timeStamp + "_raw.jpg")
    val potentialFile: Uri = Uri.fromFile(file)

    intent.putExtra(MediaStore.EXTRA_OUTPUT, potentialFile)
    this.startIntent(intent) { code, data ->
        if (code != Activity.RESULT_OK) {
            onResult(null); return@startIntent
        }
        onResult(getBitmapFromUri(potentialFile, minBytes))
    }
}