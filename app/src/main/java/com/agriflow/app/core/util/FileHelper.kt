package com.agriflow.app.core.util

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileHelper @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    /**
     * Copy a Uri content into a temporary cache file.
     */
    fun uriToFile(uri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            
            // Create a temp file in the app's cache directory
            val tempFile = File(context.cacheDir, "upload_${UUID.randomUUID()}.jpg")
            val outputStream = FileOutputStream(tempFile)
            
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Convert a temporary File into a MultipartBody.Part
     */
    fun fileToMultipartPart(file: File, partName: String = "file"): MultipartBody.Part {
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }
}
