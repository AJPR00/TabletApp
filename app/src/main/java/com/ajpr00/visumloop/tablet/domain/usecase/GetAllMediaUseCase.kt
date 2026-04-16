package com.ajpr00.visumloop.tablet.domain.usecase

import com.ajpr00.visumloop.tablet.data.repository.MediaRepository
import com.ajpr00.visumloop.tablet.domain.model.MediaContent
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetAllMediaUseCase @Inject constructor(
    private val repo: MediaRepository
) {
    operator fun invoke(): Flow<List<MediaContent>> = repo.getAllMediaBd()
}
