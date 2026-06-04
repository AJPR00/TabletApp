package com.ajpr00.core.domain.usecase.dispositivo

import com.ajpr00.core.domain.model.api.DeviceInfo
import com.ajpr00.core.domain.repository.media.TabletApiRepository
import javax.inject.Inject

class GetInfoIpUseCase @Inject constructor(
    private val repo: TabletApiRepository
) {
    suspend operator fun invoke(ip: String, port: Int): DeviceInfo? {
        val response = repo.getInfo(ip, port)

        if (response?.status != "OK") return null

        return response.data
    }
}
