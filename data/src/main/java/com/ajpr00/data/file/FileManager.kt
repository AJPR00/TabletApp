package com.ajpr00.data.file

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.ajpr00.core.domain.model.FormatType
import java.io.File
import java.io.FileOutputStream

/**
 * FileManager
 *
 * Este objeto es el “encargado de los archivos” dentro de la app.
 * Aquí centralizamos todo lo relacionado con:
 *  - Crear carpetas internas (images, videos)
 *  - Copiar archivos desde content:// (cuando el usuario selecciona algo)
 *  - Guardar archivos que llegan desde el servidor HTTP (tablet)
 *  - Generar nombres únicos
 *
 * La idea es que cualquier parte de la app que necesite guardar un archivo
 * venga aquí, y así no duplicamos lógica por todos lados.
 */
object FileManager {

    //****************************************** CREACIÓN DE CARPETAS ******************************************
    /**
     * Carpeta base donde guardamos TODO lo multimedia.
     *
     * OJO IMPORTANTE:
     * - Esto NO es público.
     * - Está dentro de Android/data/tu.paquete/files/
     * - Otras apps NO pueden entrar.
     * - No aparece en la galería.
     *
     * Es perfecto para guardar imágenes y vídeos sin pedir permisos.
     */
    private fun getBaseDir(context: Context): File {
        val base = File(context.getExternalFilesDir(null), "media")
        if (!base.exists()) base.mkdirs()

        return base
    }

    /** Carpeta de imágenes */
    fun getImagesDir(context: Context): File =
        File(getBaseDir(context), "images").apply { mkdirs() }

    /** Carpeta de vídeos */
    fun getVideosDir(context: Context): File =
        File(getBaseDir(context), "videos").apply { mkdirs() }

    //****************************************** OBTENCION METADATOS ******************************************
    /**
     * Mediante FormatType devuelve la carpeta correcta asignadas.
     */
    private fun getDirForType(context: Context, type: FormatType): File =
        when (type) {
            FormatType.IMAGE -> getImagesDir(context)
            FormatType.VIDEO -> getVideosDir(context)
            else -> getImagesDir(context)
        }

    /**
     * Dado un MIME, devolvemos la extensión correspondiente.
     */
    private fun getExtensionFromMime(mime: String?): String {
        if (mime == null) return ""

        return when {
            mime.contains("jpeg") -> ".jpg"
            mime.contains("png") -> ".png"
            mime.contains("webp") -> ".webp"
            mime.contains("mp4") -> ".mp4"
            else -> {
                // Último recurso: preguntamos a MimeTypeMap
                val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
                if (ext != null) ".$ext" else ""
            }
        }
    }
    //****************************************** GENERACIÓN DE NOMBRES ***************************************

    /**
     * Genera nombres únicos usando la hora actual.
     * Ejemplo: media_1715850000000.jpg
     */
    fun generateFileName(extension: String): String =
        "media_${System.currentTimeMillis()}.$extension"


    //****************************************** GUARDAR ARCHIVOS *******************************************
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

    private fun writeUriToFile(context: Context, uri: Uri, file: File) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
    }

    /**
     * Este método se usa cuando el usuario selecciona un archivo desde la galería.
     *
     * Flujo:
     *  1. Android nos da una URI (content://)
     *  2. Pedimos el MIME a Android
     *  3. Elegimos la carpeta según el tipo
     *  4. Creamos un archivo destino
     *  5. Copiamos los bytes desde la URI al archivo real
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

    /**
     * Este método se usa cuando la tablet recibe un archivo desde el móvil
     * mediante el endpoint /upload del servidor NanoHTTPD.
     *
     * Flujo:
     *  1. NanoHTTPD guarda el archivo en un archivo temporal
     *  2. Nosotros recibimos ese archivo temporal
     *  3. Detectamos el tipo desde el MIME
     *  4. Elegimos la carpeta correcta
     *  5. Copiamos el archivo temporal a su ubicación final
     *  6. Devolvemos un FILE
     */
    fun saveIncomingFile(
        context: Context,
        tempFile: File,
        mime: String,
        type: FormatType,
        extension1: String
    ): File {

        // Si la extensión real viene vacía, usamos la del MIME como fallback
        val extension = if (extension1.isNotBlank()) extension1 else getExtensionFromMime(mime)

        val targetDir = getDirForType(context, type)

        val finalFile = File(targetDir, generateFileName(extension))

        tempFile.copyTo(finalFile, overwrite = true)

        return finalFile
    }
}