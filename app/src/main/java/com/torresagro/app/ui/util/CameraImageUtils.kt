package com.torresagro.app.ui.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun createTempImageUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "activity_images").apply { mkdirs() }
    val imageFile = File.createTempFile("activity_", ".jpg", imagesDir)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}
