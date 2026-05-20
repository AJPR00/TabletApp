package com.ajpr00.core.domain.model

sealed class MediaResult {
    data class Success(val files: List<MediaContent>) : MediaResult()
    data class Error(val message: String, val cause: Throwable? = null) : MediaResult()
}