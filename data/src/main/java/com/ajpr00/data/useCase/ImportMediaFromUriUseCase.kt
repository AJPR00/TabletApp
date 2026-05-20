package com.ajpr00.data.useCase

import android.content.Context
import android.net.Uri
import com.ajpr00.core.domain.repository.MediaRepository
import com.ajpr00.data.mapper.toMediaContent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ImportMediaFromUriUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: MediaRepository
) {
    suspend operator fun invoke(uri: Uri) {
        val media = uri.toMediaContent(context)
        repo.addBd(media)
    }
}
