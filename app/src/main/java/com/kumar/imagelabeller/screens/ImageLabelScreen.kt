package com.kumar.imagelabeller.screens

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.kumar.imagelabeller.api.Vm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ImageLabelScreen(
    viewModel: Vm = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // Gallery picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = cameraUri
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Image Labelling",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Image preview area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Selected image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Photo",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Select or capture an image")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Image source buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Camera button
            OutlinedButton(
                onClick = {
                    // Create a file to save the image
                    val timeStamp =
                        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_$timeStamp.jpg")
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    }

                    cameraUri = context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    )

                    cameraUri?.let {
                        cameraLauncher.launch(it)
                    } ?: run {
                        Toast.makeText(context, "Cannot create image file", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Capture Image",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Camera")
            }

            // Gallery button
            OutlinedButton(
                onClick = { imagePickerLauncher.launch("image/*") }
            ) {
                Icon(
                    imageVector = Icons.Default.Photo,
                    contentDescription = "Choose from Gallery",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gallery")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Process button
        Button(
            onClick = {
                if (imageUri != null && !isProcessing) {
                    isProcessing = true

                    // Compression in background thread
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val compressedFile = compressImage(context, imageUri!!)

                            withContext(Dispatchers.Main) {
                                viewModel.processImage(compressedFile)
                                isProcessing = false
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    "Error processing image: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                isProcessing = false
                            }
                        }
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Please select or capture an image",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && !isProcessing && imageUri != null
        ) {
            if (uiState.isLoading || isProcessing) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Label Image")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Result area
        if (uiState.isLoading || isProcessing) {
            CircularProgressIndicator()
        } else if (uiState.error != null) {
            Text(
                text = "Error: ${uiState.error}",
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        } else if (uiState.result != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    uiState.result?.let { resultMap ->
                        when (val data = resultMap["data"]) {
                            is String -> {
                                Text(
                                    text = data.trim(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            null -> {
                                Text(
                                    text = "No data available",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }

                            else -> {
                                Text(
                                    text = data.toString().trim(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper function to compress the image
private suspend fun compressImage(context: android.content.Context, uri: Uri): File {
    return withContext(Dispatchers.IO) {
        // Read the image from Uri
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // Calculate new dimensions while maintaining aspect ratio
        val maxDimension = 1024 // Maximum width or height
        val originalWidth = originalBitmap.width
        val originalHeight = originalBitmap.height

        val scale = when {
            originalWidth > originalHeight -> maxDimension.toFloat() / originalWidth
            else -> maxDimension.toFloat() / originalHeight
        }

        val newWidth = (originalWidth * scale).toInt()
        val newHeight = (originalHeight * scale).toInt()

        // Create scaled bitmap
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)

        // Compress to JPEG with reduced quality
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)

        // Create a temp file to store the compressed image
        val tempFile = File.createTempFile("compressed_image", ".jpg", context.cacheDir)
        val fileOutputStream = FileOutputStream(tempFile)
        fileOutputStream.write(outputStream.toByteArray())
        fileOutputStream.close()

        // Clean up
        originalBitmap.recycle()
        scaledBitmap.recycle()

        tempFile
    }
}