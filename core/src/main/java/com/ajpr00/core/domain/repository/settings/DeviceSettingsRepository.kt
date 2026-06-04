package com.ajpr00.core.domain.repository.settings

import com.ajpr00.core.domain.model.api.DeviceInfo

interface DeviceSettingsRepository {
    suspend fun getDeviceInfo(): DeviceInfo
    suspend fun getUserEmail(): String?
}