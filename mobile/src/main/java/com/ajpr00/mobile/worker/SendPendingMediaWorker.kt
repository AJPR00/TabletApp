/*
package com.ajpr00.mobile.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.ajpr00.core.domain.usecase.media.GetNextPendingMediaUseCase
import com.ajpr00.core.domain.usecase.media.DeletePendingMediaUseCase
import com.ajpr00.core.domain.usecase.media.SendEncryptedMediaUseCase
import com.ajpr00.core.domain.usecase.preference.GetAesKeyUseCase
import com.ajpr00.core.domain.usecase.network.IsTabletAliveUseCase

@HiltWorker
class SendPendingMediaWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val getNextPendingMediaUseCase: GetNextPendingMediaUseCase,

    private val deletePendingMediaUseCase: DeletePendingMediaUseCase,
    private val sendEncryptedMediaUseCase: SendEncryptedMediaUseCase,
    private val getAesKeyUseCase: GetAesKeyUseCase,
    private val isTabletAliveUseCase: IsTabletAliveUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        // 1. Obtener siguiente pending
        val next = getNextPendingMediaUseCase() ?: return Result.success()

        // 2. Comprobar tablet
        val deviceIp = next.ip ?: return Result.retry()
        val devicePort = next.port ?: return Result.retry()

        val alive = isTabletAliveUseCase(deviceIp, devicePort).getOrDefault(false)
        if (!alive) return Result.retry()

        // 3. Obtener clave AES
        val aesKey = getAesKeyUseCase() ?: return Result.retry()

        return try {
            // 4. Enviar media
            sendEncryptedMediaUseCase(
                media = next,
                ip = deviceIp,
                port = devicePort,
                aesKey = aesKey
            )

            // 5. Borrar de la BD
            deletePendingMediaUseCase(next)

            // 6. Si quedan más → reprogramar
            if (getNextPendingMediaUseCase() != null) {
                WorkManager.getInstance(applicationContext)
                    .enqueue(
                        OneTimeWorkRequestBuilder<SendPendingMediaWorker>()
                            .build()
                    )
            }

            Result.success()

        } catch (e: Exception) {
            Result.retry()
        }
    }
}
*/
