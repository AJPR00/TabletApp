package com.ajpr00.components.cards

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri

// Esta función intenta sacar un frame del vídeo usando
// MediaMetadataRetriever.
//
// IMPORTANTE:
// - Si el path es "content://", hay que usar setDataSource(context, uri)
//   porque setDataSource(path) falla en Android moderno.
// - Funciona incluso en tablets antiguas (Android 6 y 7).
fun getVideoThumbnail(path: String, context: Context): Bitmap? {
    return try {
        val retriever = MediaMetadataRetriever()
        val uri = Uri.parse(path)

        retriever.setDataSource(context, uri)

        val bitmap = retriever.getFrameAtTime(0) // primer frame del vídeo
        retriever.release()
        bitmap

    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}