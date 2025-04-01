package com.kumar.imagelabeller.model

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import java.io.BufferedReader
import java.io.InputStreamReader

class TFLiteClassifier(private val context: Context) {
    private var imageClassifier: ImageClassifier? = null
    private var labelList: List<String> = emptyList()

    fun initialize() {
        // Load labels from raw resource
        try {
            val resourceId = context.resources.getIdentifier("labels", "raw", context.packageName)

            if (resourceId != 0) {
                context.resources.openRawResource(resourceId).use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        labelList = reader.lineSequence().toList()
                        Log.d(
                            "TFLiteClassifier",
                            "Successfully loaded ${labelList.size} labels from raw/labels.txt"
                        )
                    }
                }
            } else {
                Log.e("TFLiteClassifier", "Could not find labels.txt in raw resources")
                labelList = readDefaultLabels()
            }
        } catch (e: Exception) {
            Log.e("TFLiteClassifier", "Error loading labels from raw", e)
            labelList = readDefaultLabels()
        }

        // Initialize TensorFlow Lite model
        try {
            val baseOptions = BaseOptions.builder()
                .setNumThreads(4)
                .build()

            val options = ImageClassifier.ImageClassifierOptions.builder()
                .setBaseOptions(baseOptions)
                .setMaxResults(5)
                .setScoreThreshold(0.05f)
                .build()

            imageClassifier = ImageClassifier.createFromFileAndOptions(
                context,
                "mobilenet_v1_1.0_224_quant.tflite",
                options
            )
            Log.d("TFLiteClassifier", "Classifier initialized successfully")
        } catch (e: Exception) {
            Log.e("TFLiteClassifier", "Error initializing classifier", e)
            e.printStackTrace()
        }
    }

    fun classify(originalBitmap: Bitmap): List<ClassificationResult> {
        val classifier = imageClassifier ?: run {
            Log.e("TFLiteClassifier", "Classifier not initialized")
            return listOf(ClassificationResult("Classifier not initialized", 0f))
        }

        return try {
            // Process bitmap for model input (224x224 is the expected input size for MobileNet)
            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
                .build()

            val tensorImage = TensorImage.fromBitmap(originalBitmap)
            val processedImage = imageProcessor.process(tensorImage)

            Log.d(
                "TFLiteClassifier",
                "Processing image: ${processedImage.width}x${processedImage.height}"
            )

            // Classify
            val results = classifier.classify(processedImage)
            Log.d("TFLiteClassifier", "Raw Results: $results")

            if (results.isEmpty()) {
                Log.w("TFLiteClassifier", "No classification results returned")
                return listOf(ClassificationResult("No results found", 0f))
            }

            Log.d("TFLiteClassifier", "Classification results count: ${results.size}")

            val classificationResults = results.flatMap { classification ->
                classification.categories.map { category ->
                    // Get the label text from our label list
                    val labelText = if (category.label.isNotEmpty()) {
                        // If model returns text labels directly
                        category.label
                    } else if (category.index >= 0 && category.index < labelList.size) {
                        // If model returns index, look up in our label list
                        labelList[category.index]
                    } else {
                        // Fallback if label is not found
                        "Unknown (Index: ${category.index})"
                    }

                    Log.d(
                        "TFLiteClassifier",
                        "Index: ${category.index}, Label: $labelText, Score: ${category.score}"
                    )

                    ClassificationResult(
                        label = labelText,
                        confidence = category.score
                    )
                }
            }.sortedByDescending { it.confidence }

            Log.d(
                "TFLiteClassifier",
                "Processed classification results: ${classificationResults.size}"
            )

            // Return default message if no valid results
            if (classificationResults.isEmpty()) {
                listOf(ClassificationResult("No objects detected", 0f))
            } else {
                classificationResults
            }
        } catch (e: Exception) {
            Log.e("TFLiteClassifier", "Classification error", e)
            e.printStackTrace()
            listOf(ClassificationResult("Error classifying image", 0f))
        }
    }

    /**
     * Provides default ImageNet labels when the file can't be loaded
     */
    private fun readDefaultLabels(): List<String> {
        // These labels correspond to the ImageNet labels for the MobileNet model
        // This is a small subset focusing on common objects
        val labels = mutableListOf<String>()

        // Ensure we have enough entries to cover all possible indices
        for (i in 0 until 1000) {
            labels.add("unknown")
        }

        // Add specific labels for common objects
        labels[486] = "ballpoint"
        labels[789] = "water bottle"
        labels[775] = "vase"
        labels[674] = "mouse"
        labels[502] = "lighter"
        labels[895] = "wine bottle"
        labels[832] = "notebook"
        labels[798] = "cellular telephone"

        Log.d("TFLiteClassifier", "Using fallback labels")
        return labels
    }
}