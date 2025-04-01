package com.kumar.imagelabeller.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.kumar.imagelabeller.model.ClassificationResult
import com.kumar.imagelabeller.model.TFLiteClassifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@ExperimentalMaterial3Api
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class
)
@Composable
fun ImageLabellerScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var latestLabels by remember { mutableStateOf<List<ClassificationResult>>(emptyList()) }
    var imageCaptureRef by remember { mutableStateOf<ImageCapture?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var cameraReady by remember { mutableStateOf(false) }
    var cameraProviderRef by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentPreviewView by remember { mutableStateOf<PreviewView?>(null) }
    var needsReinitialize by remember { mutableStateOf(false) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // TensorFlow Lite Classifier
    val tfLiteClassifier = remember { TFLiteClassifier(context) }

    // Initialize TFLite Classifier
    LaunchedEffect(Unit) {
        tfLiteClassifier.initialize()
    }

    // Cleanup camera executor when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            try {
                cameraProviderRef?.unbindAll()
            } catch (e: Exception) {
                Log.e("ImageLabellerScreen", "Error unbinding camera provider", e)
            }
            cameraExecutor.shutdown()
        }
    }

    // Show error message as a snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            errorMessage = null
        }
    }

    // Function to initialize camera
    fun initializeCamera(previewView: PreviewView) {
        try {
            // Clean up any existing camera
            cameraProviderRef?.unbindAll()
            isProcessing = true
            cameraReady = false

            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    // Get camera provider
                    val provider = cameraProviderFuture.get()
                    cameraProviderRef = provider

                    // Create camera use cases
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    imageCaptureRef = capture

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    // Bind use cases to camera
                    try {
                        provider.unbindAll()
                        provider.bindToLifecycle(
                            lifecycleOwner, cameraSelector, preview, capture
                        )
                        cameraReady = true
                        needsReinitialize = false
                        Log.d("ImageLabellerScreen", "Camera initialized successfully")
                    } catch (exc: Exception) {
                        Log.e("CameraPreview", "Use case binding failed", exc)
                        errorMessage = "Failed to initialize camera: ${exc.message}"
                        cameraReady = false
                        needsReinitialize = true
                    }
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Camera provider failed", e)
                    errorMessage = "Camera error: ${e.message}"
                    cameraReady = false
                    needsReinitialize = true
                } finally {
                    isProcessing = false
                }
            }, ContextCompat.getMainExecutor(context))
        } catch (e: Exception) {
            Log.e("ImageLabellerScreen", "Exception initializing camera", e)
            isProcessing = false
            cameraReady = false
            needsReinitialize = true
            errorMessage = "Failed to initialize camera: ${e.message}"
        }
    }

    // Trigger to reinitialize camera if needed
    LaunchedEffect(needsReinitialize) {
        if (needsReinitialize) {
            delay(500)
            currentPreviewView?.let { initializeCamera(it) }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("AI Image Labeller") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            currentPreviewView?.let {
                                needsReinitialize = true
                            }
                        },
                        enabled = !isProcessing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Camera"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Camera preview
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }
                },
                update = { previewView ->
                    // Store reference to the previewView
                    currentPreviewView = previewView
                    // Initialize camera only if not already being processed
                    if (!isProcessing && !cameraReady) {
                        initializeCamera(previewView)
                    }
                }
            )

            // Capture button
            Button(
                onClick = {
                    if (isProcessing) {
                        return@Button
                    }

                    if (!cameraReady) {
                        errorMessage = "Camera not ready yet, please wait"
                        // If camera not ready, trigger reinitialization
                        needsReinitialize = true
                        return@Button
                    }

                    isProcessing = true
                    latestLabels = emptyList()

                    val capture = imageCaptureRef
                    if (capture == null) {
                        isProcessing = false
                        errorMessage = "Camera not initialized"
                        needsReinitialize = true
                        return@Button
                    }

                    // Add a small delay to ensure camera is stable
                    scope.launch {
                        try {
                            delay(300) // Short delay to let camera stabilize

                            capture.takePicture(
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageCapturedCallback() {
                                    @OptIn(ExperimentalGetImage::class)
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        try {
                                            val bitmap = imageProxyToBitmap(image)
                                            Log.d("ImageLabellerScreen", "Captured image size: ${bitmap.width}x${bitmap.height}")

                                            // Use TFLite for classification
                                            val results = tfLiteClassifier.classify(bitmap)
                                            latestLabels = results
                                        } catch (e: Exception) {
                                            Log.e("ImageLabellerScreen", "Failed to process image", e)
                                            latestLabels = listOf(ClassificationResult("Failed to process image: ${e.message}", 0f))
                                            errorMessage = "Processing error: ${e.message}"
                                        } finally {
                                            image.close()
                                            isProcessing = false
                                        }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        Log.e("CameraPreview", "Image capture failed", exception)

                                        if (exception.message?.contains("Not bound to a valid Camera") == true ||
                                            exception.message?.contains("Camera is closed") == true) {
                                            latestLabels = listOf(ClassificationResult("Camera error. Try again.", 0f))
                                            needsReinitialize = true
                                        } else {
                                            latestLabels = listOf(ClassificationResult("Failed to capture image", 0f))
                                            errorMessage = "Capture failed: ${exception.message}"
                                        }

                                        isProcessing = false
                                    }
                                }
                            )
                        } catch (e: Exception) {
                            Log.e("ImageLabellerScreen", "Exception taking picture", e)
                            errorMessage = "Error: ${e.message}"
                            isProcessing = false
                            needsReinitialize = true
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 96.dp),
                enabled = !isProcessing && cameraReady
            ) {
                Text(if (isProcessing) "Processing..." else "Capture")
            }

            // Processing indicator
            AnimatedVisibility(
                visible = isProcessing,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                CircularProgressIndicator()
            }

            // Display classification results
            AnimatedVisibility(
                visible = latestLabels.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        items(count = latestLabels.size) { index ->
                            val result = latestLabels[index]
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = result.label,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Confidence: ${
                                        String.format(
                                            "%.1f", result.confidence * 100
                                        )
                                    }%",
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Simplified and robust ImageProxy to Bitmap conversion
@OptIn(ExperimentalGetImage::class)
private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
    val image = imageProxy.image ?: throw IllegalArgumentException("Image proxy has null image")

    // Check image format and planes
    Log.d("ImageLabellerScreen", "Image format: ${image.format}, planes: ${image.planes.size}")

    try {
        // Direct approach: Use ImageProxy's output capability
        val buffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)

        // Create bitmap from bytes
        var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        if (bitmap == null) {
            // Fallback if decoding fails
            Log.d("ImageLabellerScreen", "Fallback to simple bitmap")
            bitmap = Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.Gray.hashCode())
        }

        // Log bitmap details
        Log.d("ImageLabellerScreen", "Bitmap created: ${bitmap.width}x${bitmap.height}")

        // Adjust for rotation
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        if (rotationDegrees != 0) {
            Log.d("ImageLabellerScreen", "Rotating bitmap by $rotationDegrees degrees")
            val matrix = Matrix()
            matrix.postRotate(rotationDegrees.toFloat())
            return Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )
        }

        return bitmap
    } catch (e: Exception) {
        Log.e("ImageLabellerScreen", "Error converting image to bitmap", e)

        // Create a blank bitmap as last resort
        val bitmap = Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.Gray.hashCode())
        return bitmap
    }
}