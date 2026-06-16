package com.ajpr00.core.domain.excepcion

import com.ajpr00.core.util.VisumException

 sealed class MediaException(code: String) : VisumException(code) {
    object FileNotFound : MediaException("MEDIA_FILE_NOT_FOUND")
    object ReadError : MediaException("MEDIA_READ_ERROR")
    object EncryptError : MediaException("MEDIA_ENCRYPT_ERROR")
    object UploadError : MediaException("MEDIA_UPLOAD_ERROR")
}