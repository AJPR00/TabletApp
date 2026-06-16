package com.ajpr00.data.file

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.ajpr00.core.domain.model.FormatType
import com.ajpr00.core.util.CoreLog
import com.ajpr00.data.exception.FileException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * ## FileManager
 *
 * Utilidad centralizada para gestionar archivos dentro de la app.
 *
 * ### Responsabilidad dentro de la arquitectura
 * - Pertenece a la **Data layer (local)**.
 * - Encapsula toda la lógica de creación, copia y almacenamiento de archivos.
 * - Evita duplicar código en módulos mobile/tablet.
 *
 * ### Qué gestiona
 * - Creación de carpetas internas privadas.
 * - Copia de archivos desde `content://`.
 * - Guardado de archivos recibidos por HTTP.
 * - Generación de nombres únicos.
 *
 * ### Notas
 * - Las carpetas se crean dentro de `Android/data/.../files/media`.
 * - No son accesibles por otras apps ni aparecen en la galería.
 */
object FileManager {

    // -------------------------------------------------------------------------
    //  CREACIÓN DE CARPETAS
    // -------------------------------------------------------------------------

    /**
     * Carpeta base donde se guardan imágenes y vídeos.
     *
     * @return Carpeta `media/` dentro del sandbox privado.
     */
    private fun getBaseDir(context: Context): File {
        val base = File(context.getExternalFilesDir(null), "media")
        if (!base.exists()) base.mkdirs()
        return base
    }

    /** Carpeta de imágenes. */
    fun getImagesDir(context: Context): File =
        File(getBaseDir(context), "images").apply { mkdirs() }

    /** Carpeta de vídeos. */
    fun getVideosDir(context: Context): File =
        File(getBaseDir(context), "videos").apply { mkdirs() }

    // -------------------------------------------------------------------------
    //  METADATOS Y EXTENSIONES
    // -------------------------------------------------------------------------

    /**
     * Devuelve la carpeta correcta según el tipo de archivo.
     */
    private fun getDirForType(context: Context, type: FormatType): File =
        when (type) {
            FormatType.IMAGE -> getImagesDir(context)
            FormatType.VIDEO -> getVideosDir(context)
            else -> getImagesDir(context)
        }

    /**
     * Obtiene la extensión correcta a partir del MIME.
     */
    private fun getExtensionFromMime(mime: String?): String {
        if (mime == null) return ""

        return when {
            mime.contains("jpeg") -> ".jpg"
            mime.contains("png") -> ".png"
            mime.contains("webp") -> ".webp"
            mime.contains("mp4") -> ".mp4"
            else -> {
                val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
                if (ext != null) ".$ext" else ""
            }
        }
    }

    // -------------------------------------------------------------------------
    //  GENERACIÓN DE NOMBRES
    // -------------------------------------------------------------------------

    /**
     * Genera nombres únicos usando timestamp.
     *
     * Ejemplo: `media_1715850000000.jpg`
     */
    fun generateFileName(extension: String): String =
        "media_${System.currentTimeMillis()}$extension"

    // -------------------------------------------------------------------------
    //  COPIA DE ARCHIVOS
    // -------------------------------------------------------------------------

    /**
     * Crea un archivo destino adecuado según el tipo y la extensión.
     */
    private fun createFileForUri(
        context: Context,
        uri: Uri,
        type: FormatType
    ): File {
        val mime = context.contentResolver.getType(uri)
        val extension = getExtensionFromMime(mime)
        val targetDir = getDirForType(context, type)
        return File(targetDir, generateFileName(extension))
    }

    /**
     * Copia un archivo desde una URI `content://` a un archivo real.
     *
     * ### Flujo interno
     * 1. Abre InputStream desde la URI.
     * 2. Copia con buffer de 8 KB para evitar truncado.
     * 3. Valida tamaño antes y después.
     *
     * @throws FileException.ReadError si falla la lectura.
     * @throws FileException.WriteError si falla la escritura.
     */
    private fun writeUriToFile(context: Context, uri: Uri, file: File) {
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->

                val expectedSize = input.available()
                CoreLog.d("FileManager", "Tamaño esperado: $expectedSize bytes")

                FileOutputStream(file).use { output ->
                    input.copyTo(output, bufferSize = 8 * 1024)
                }

                val finalSize = file.length()
                CoreLog.d("FileManager", "Tamaño final: $finalSize bytes")

                if (expectedSize > 0 && finalSize < expectedSize) {
                    CoreLog.e("FileManager", "Archivo truncado al copiar desde content://")
                    throw FileException.WriteError
                }
            } ?: throw FileException.ReadError

        } catch (e: IOException) {
            CoreLog.e("FileManager", "Error copiando archivo: ${e.message}")
            throw FileException.WriteError
        }
    }

    /**
     * Copia un archivo desde una URI `content://` a almacenamiento interno.
     *
     * ### Flujo
     * 1. Determina carpeta según tipo.
     * 2. Crea archivo destino.
     * 3. Copia bytes con validación.
     *
     * @return Archivo final guardado.
     */
    fun copyUriToInternalFile(
        context: Context,
        uri: Uri,
        type: FormatType
    ): File {
        val file = createFileForUri(context, uri, type)
        writeUriToFile(context, uri, file)
        return file
    }

    // -------------------------------------------------------------------------
    //  ARCHIVOS RECIBIDOS POR HTTP
    // -------------------------------------------------------------------------

    /**
     * Guarda un archivo recibido desde el servidor HTTP (tablet).
     *
     * ### Flujo interno
     * 1. Recibe archivo temporal generado por NanoHTTPD.
     * 2. Determina extensión real.
     * 3. Copia a carpeta definitiva.
     *
     * @return Archivo final guardado.
     *
     * @throws FileException.WriteError si falla la copia.
     */
    fun saveIncomingFile(
        context: Context,
        tempFile: File,
        mime: String,
        type: FormatType,
        extension1: String
    ): File {

        val extension = extension1.ifBlank { getExtensionFromMime(mime) }
        val targetDir = getDirForType(context, type)
        val finalFile = File(targetDir, generateFileName(extension))

        try {
            tempFile.copyTo(finalFile, overwrite = true)
        } catch (e: Exception) {
            CoreLog.e("FileManager", "Error guardando archivo entrante: ${e.message}")
            throw FileException.WriteError
        }

        return finalFile
    }
}