package com.ajpr00.core.domain.usecase

import com.ajpr00.core.domain.model.MediaResult
import com.ajpr00.core.domain.repository.MediaRepository
import javax.inject.Inject

class ListMediaDriveUseCase @Inject constructor(
    private val repo: MediaRepository
) {
    suspend operator fun invoke(): MediaResult = repo.listMediaFilesDrive()
}
