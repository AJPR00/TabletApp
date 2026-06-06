package com.ajpr00.core.domain.model.api

/**
 * # UploadResult
 *
 * Modelo que representa el resultado de subir un archivo al servidor de la tablet.
 *
 * ## ¿Para qué sirve?
 * Este modelo viaja dentro de `ApiResponse<UploadResult>` cuando el móvil
 * realiza una petición `POST /upload` enviando un archivo cifrado.
 *
 * El servidor responde con:
 * - `success = true` si el archivo se ha descifrado, guardado y registrado en Room.
 * - `uploaded = true` como confirmación explícita de que el archivo ya está disponible.
 *
 * ## Relación con otras capas
 * - **TabletServer**: lo genera tras procesar el archivo.
 * - **Retrofit (móvil)**: lo recibe y lo parsea con Gson.
 * - **SendEncryptedMediaUseCase**: usa `success` para saber si debe marcar el media como enviado.
 *
 * ## Ejemplo de JSON esperado
 * ```json
 * {
 *   "status": "OK",
 *   "data": {
 *     "success": true,
 *     "uploaded": true
 *   },
 *   "error": null
 * }
 * ```
 *
 * ## Notas
 * - Ambos campos son obligatorios para evitar errores de inferencia de tipos en Gson.
 * - No se incluyen rutas ni nombres de archivo por seguridad.
 */
data class UploadResult(
    val success: Boolean,
    val uploaded: Boolean
)
