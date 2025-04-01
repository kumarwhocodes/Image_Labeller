package com.kumar.imagelabeller.model

import android.content.Context
import android.util.Log

/**
 * Helper class to list files in assets and debug asset access
 */
class AssetHelper(private val context: Context) {

    /**
     * Lists all files in the assets folder recursively
     */
    fun listAllAssets(): List<String> {
        val fileList = mutableListOf<String>()
        try {
            listAssetsRecursively("", fileList)
        } catch (e: Exception) {
            Log.e("AssetHelper", "Error listing assets", e)
        }
        return fileList
    }

    private fun listAssetsRecursively(path: String, fileList: MutableList<String>) {
        val assets = context.assets.list(path) ?: return

        for (asset in assets) {
            val fullPath = if (path.isEmpty()) asset else "$path/$asset"
            try {
                // Check if this is a directory by trying to list its contents
                val subAssets = context.assets.list(fullPath)
                if (subAssets != null && subAssets.isNotEmpty()) {
                    listAssetsRecursively(fullPath, fileList)
                } else {
                    fileList.add(fullPath)
                }
            } catch (e: Exception) {
                // Not a directory, add as file
                fileList.add(fullPath)
            }
        }
    }

    /**
     * Reads a file from assets and returns its content
     */
    fun readAssetFile(fileName: String): String {
        return try {
            context.assets.open(fileName).use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            }
        } catch (e: Exception) {
            Log.e("AssetHelper", "Error reading asset: $fileName", e)
            "Error reading file: ${e.message}"
        }
    }
}