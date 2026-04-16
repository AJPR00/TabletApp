package com.ajpr00.visumloop.tablet.domain.usecase

import com.ajpr00.visumloop.tablet.data.repository.MediaRepository
import com.ajpr00.visumloop.tablet.domain.model.MediaResult
import jakarta.inject.Inject

class ListMediaDriveUseCase @Inject constructor(
    private val repo: MediaRepository
) {
    suspend operator fun invoke(): MediaResult = repo.listMediaFilesDrive()
}
