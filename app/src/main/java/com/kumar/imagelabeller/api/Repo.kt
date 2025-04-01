package com.kumar.imagelabeller.api

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

// Repository
class Repo @Inject constructor(
    private val geminiImageApi: ApiService
) {
    suspend fun processImage(imageFile: File): Map<String, Any> {
        val requestImageFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestImageFile)

        return geminiImageApi.processImage(imagePart)
    }
}