package com.ajpr00.core.domain.usecase.preference

import com.ajpr00.core.domain.model.UserAuthData

interface SaveSessionUseCase {
    suspend operator fun invoke(data: UserAuthData)
}
