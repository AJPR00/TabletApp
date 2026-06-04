package com.ajpr00.core.domain.usecase.dispositivo

import com.ajpr00.core.domain.repository.dispositivo.TabletLocatorRepository
import javax.inject.Inject

class LocateTabletByManualUseCase @Inject constructor(
    private val repo: TabletLocatorRepository
) {
    suspend operator fun invoke(ip: String, port: Int) =
        repo.locateByManual(ip, port)
}
