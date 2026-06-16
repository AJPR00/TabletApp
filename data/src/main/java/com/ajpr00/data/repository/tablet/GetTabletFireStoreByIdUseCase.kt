package com.ajpr00.data.repository.tablet

import com.ajpr00.core.domain.repository.dispositivo.TabletRepository
import javax.inject.Inject


class GetTabletFireStoreByIdUseCase @Inject constructor(
    private val repository: TabletRepository
) {
    suspend operator fun invoke(idTablet: String) =
        repository.getTabletById(idTablet)
}