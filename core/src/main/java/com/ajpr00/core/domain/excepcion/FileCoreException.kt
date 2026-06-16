package com.ajpr00.core.domain.excepcion

import com.ajpr00.core.util.VisumException

/* FILE I/O */
sealed class FileCoreException(code: String) : VisumException(code) {
    object NotFound : FileCoreException("FILE_NOT_FOUND")
    object ReadError : FileCoreException("FILE_READ_ERROR")
    object WriteError : FileCoreException("FILE_WRITE_ERROR")
}