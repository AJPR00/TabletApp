package com.ajpr00.core.domain.usecase.dispositivo

import com.ajpr00.core.domain.repository.dispositivo.TabletLocatorRepository
import javax.inject.Inject

class LocateAutoIpByLanUseCase @Inject constructor(
    private val repo: TabletLocatorRepository
) {
    suspend operator fun invoke(port: Int) = repo.locateByLan(port)
}
