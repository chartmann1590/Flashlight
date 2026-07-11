package com.charles.flashlight.data.feedback

import android.content.Context
import android.net.Uri
import android.util.Base64
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

fun uriToBase64(context: Context, uri: Uri): String {
    return context.contentResolver.openInputStream(uri)?.use { input ->
        Base64.encodeToString(input.readBytes(), Base64.NO_WRAP)
    } ?: throw IOException("Unable to open selected image.")
}

fun uniqueFeedbackFilename(extension: String = "png"): String {
    val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
    val suffix = Random.nextInt(1000, 9999)
    val cleanExtension = extension.trim().trimStart('.').ifBlank { "png" }
    return "issue-$timestamp-$suffix.$cleanExtension"
}

fun extensionForUri(context: Context, uri: Uri): String {
    val type = context.contentResolver.getType(uri).orEmpty().lowercase(Locale.US)
    return when {
        type.contains("jpeg") || type.contains("jpg") -> "jpg"
        type.contains("webp") -> "webp"
        type.contains("gif") -> "gif"
        else -> "png"
    }
}
