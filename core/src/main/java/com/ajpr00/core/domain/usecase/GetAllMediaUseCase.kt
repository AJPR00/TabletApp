package com.ajpr00.core.domain.usecase

import com.ajpr00.core.domain.model.MediaContent
import com.ajpr00.core.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllMediaUseCase @Inject constructor(
    private val repo: MediaRepository
) {
    operator fun invoke(): Flow<List<MediaContent>> = repo.getAllMediaBd()
}
