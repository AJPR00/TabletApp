package com.ajpr00.data.exception

import com.ajpr00.core.util.VisumException

sealed class DataException(code: String) : VisumException(code)

/* NETWORK */
sealed class NetworkException(code: String) : DataException(code) {
    object Timeout : NetworkException("NETWORK_TIMEOUT")
    object ConnectionLost : NetworkException("NETWORK_CONNECTION_LOST")
    object ServerError : NetworkException("NETWORK_SERVER_ERROR")
}

/* DATABASE */
sealed class DatabaseException(code: String) : DataException(code) {
    object ReadError : DatabaseException("DB_READ_ERROR")
    object WriteError : DatabaseException("DB_WRITE_ERROR")
    object NotFound : DatabaseException("DB_NOT_FOUND")
}

/* FILE I/O */
sealed class FileException(code: String) : DataException(code) {
    object NotFound : FileException("FILE_NOT_FOUND")
    object ReadError : FileException("FILE_READ_ERROR")
    object WriteError : FileException("FILE_WRITE_ERROR")
}