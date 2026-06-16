package com.ajpr00.data.repository.tablet

import com.ajpr00.core.domain.model.mDNS.MdnsServiceInfo
import com.ajpr00.core.domain.repository.dispositivo.TabletLocatorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DiscovermDNSTabletUseCase @Inject constructor(
    private val repository: TabletLocatorRepository
) {
    operator fun invoke(): Flow<MdnsServiceInfo> {
        return repository.discoverTablet()
    }
}

