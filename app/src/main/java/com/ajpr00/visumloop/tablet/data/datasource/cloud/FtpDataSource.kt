package com.ajpr00.visumloop.tablet.data.datasource.cloud

import android.util.Log
import com.ajpr00.visumloop.tablet.domain.model.MediaContent
import com.ajpr00.visumloop.tablet.domain.model.FormatType
import com.ajpr00.visumloop.tablet.domain.model.FtpConfig
import jakarta.inject.Inject
import org.apache.commons.net.ftp.FTPClient

class FtpDataSource @Inject constructor(
    private val config: FtpConfig
) {
    private val ftpClient = FTPClient()

    // Todo controlar de que carpeta extrae los archivos
    /**
     * Función principal: conecta al servidor FTP, lista los archivos
     * y devuelve solo imágenes y vídeos convertidos a nuestro modelo de dominio.
     *
     * Piensa en esto como "entrar en la carpeta del FTP y traerte lo que te interesa".
     */
    suspend fun listMediaFiles(): List<MediaContent> {
        val result = mutableListOf<MediaContent>()

        try {
            Log.d("FtpDataSource", "🔌 Conectando al servidor FTP ${config.host}:$config.port ...")
            ftpClient.connect(config.host, config.port)

            Log.d("FtpDataSource", "👤 Logueando con usuario $config.user ...")
            ftpClient.login(config.user, config.password)

            Log.d("FtpDataSource", "📂 Listando archivos en el FTP ...")
            val files = ftpClient.listFiles()

            for (file in files) {
                if (file.isFile) {
                    // Detectamos si es imagen o vídeo según la extensión
                    val type = when {
                        file.name.endsWith(".jpg", true) || file.name.endsWith(".png", true) -> FormatType.IMAGE
                        file.name.endsWith(".mp4", true) || file.name.endsWith(".avi", true) -> FormatType.VIDEO
                        else -> {
                            Log.d("FtpDataSource", "⏭️ Ignorando archivo no multimedia: ${file.name}")
                            continue
                        }
                    }

                    Log.d("FtpDataSource", "✅ Archivo válido: ${file.name} (${type.name})")

                    // Creamos nuestro modelo de dominio para que el repo lo entienda
                    result.add(
                        MediaContent(
                            id = file.hashCode(),
                            name = file.name,
                            path = file.name, // aquí podrías construir la ruta completa si lo necesitas
                            type = type,
                            isFavorite = false
                        )
                    )
                }
            }

            Log.d("FtpDataSource", "📊 Total de archivos multimedia encontrados: ${result.size}")

        } catch (e: Exception) {
            Log.e("FtpDataSource", "❌ Error al acceder al FTP: ${e.message}", e)
        } finally {
            // Siempre cerramos la conexión para no dejar el FTP abierto
            if (ftpClient.isConnected) {
                Log.d("FtpDataSource", "🔒 Cerrando sesión FTP ...")
                ftpClient.logout()
                ftpClient.disconnect()
            }
        }

        return result
    }
}
