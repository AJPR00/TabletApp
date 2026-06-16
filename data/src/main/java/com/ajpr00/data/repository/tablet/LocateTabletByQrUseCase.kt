package com.ajpr00.data.repository.tablet

import com.ajpr00.core.domain.repository.dispositivo.TabletLocatorRepository
import javax.inject.Inject;

class LocateTabletByQrUseCase @Inject constructor(
        private val repo: TabletLocatorRepository
) {
    suspend operator fun invoke(qr: String) = {}
}
