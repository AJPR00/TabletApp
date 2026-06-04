package com.ajpr00.tablet.data.server

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ajpr00.tablet.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Los Services tienen constructor, pero Android siempre usa el constructor vacío por defecto.
 * No se pueden definir constructores con parámetros ni usar @Inject constructor(),
 * porque el Service lo instancia el sistema y no el desarrollador.
 * Las dependencias deben inicializarse en onCreate() o inyectarse con Hilt mediante @AndroidEntryPoint.
 *
 * ServerService es un componente del sistema independiente del ciclo de vida de la Activity,
 * pero NO es independiente del ciclo de vida del proceso de la app.
 * Si el proceso muere (cerrar app desde recientes, forzar detención, OOM), el Service muere también.
 */
@AndroidEntryPoint
class ServerService : Service() {

    // Hilt nos inyecta el servidor ya configurado.
    // Esto evita tener que crearlo a mano.
    @Inject lateinit var tabletServer: TabletServer

    override fun onCreate() {
        super.onCreate()

        Log.d("ServerService", "onCreate() → El servicio está arrancando")

        // Obligatorio para que Android no mate el servicio.
        // Aquí mostramos la notificación permanente.
        startForeground(1, createNotification())

        Log.d("ServerService", "startForeground() → Notificación mostrada")

        // Arrancamos NanoHTTPD (nuestro servidor LAN)
        try {
            tabletServer.start()
            Log.d("ServerService", "Servidor HTTP iniciado correctamente en el puerto 8080")
        } catch (e: Exception) {
            Log.e("ServerService", "Error al iniciar el servidor: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        Log.d("ServerService", "onDestroy() → El servicio se está deteniendo")

        // Parada limpia del servidor
        try {
            tabletServer.stop()
            Log.d("ServerService", "Servidor HTTP detenido correctamente")
        } catch (e: Exception) {
            Log.e("ServerService", "Error al detener el servidor: ${e.message}", e)
        }

        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    private fun createNotification(): Notification {
        Log.d("ServerService", "Creando notificación del servidor")

        val channelId = "server_channel"

        // Crear canal para Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Servidor LAN",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)

            Log.d("ServerService", "Canal de notificación creado (Android 8+)")
        }

        // Acción para detener el servidor desde la notificación
        val stopIntent = Intent(this, ServerService::class.java).apply {
            action = "STOP_SERVER"
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        Log.d("ServerService", "Acción STOP_SERVER añadida a la notificación")

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Servidor activo")
            .setContentText("La tablet está compartiendo contenido en la red")
            .setSmallIcon(R.drawable.ic_notificacion)
            .setColor(0xFF2196F3.toInt()) // Azul Material
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "El servidor LAN está activo.\n" +
                            "Puedes enviar archivos desde el móvil o acceder al contenido multimedia."
                )
            )
            .addAction(
                R.drawable.ic_notificacion,
                "Detener servidor",
                stopPendingIntent
            )
            .setOngoing(true) // No se puede deslizar
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d("ServerService", "onStartCommand() → Acción recibida: ${intent?.action}")

        // Si el usuario pulsa "Detener servidor" en la notificación
        if (intent?.action == "STOP_SERVER") {
            Log.d("ServerService", "STOP_SERVER → Deteniendo servicio por acción del usuario")
            stopSelf()
            return START_NOT_STICKY
        }

        // Mantener el servicio vivo si Android lo reinicia
        return START_STICKY
    }
}
