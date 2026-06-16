package com.ajpr00.visumloop.mobile.data.repository

import com.ajpr00.core.domain.model.Dispositivo
import com.ajpr00.core.domain.model.EstadoDispositivo
import com.ajpr00.mobile.data.datasource.local.DispositivoLocalDataSource
import com.ajpr00.mobile.data.repositoryImp.DispositivoRepositoryImpl
import org.junit.Test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.mockito.Mockito.mock
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Pruebas unitarias para la implementación del repositorio de dispositivos
 * en el módulo móvil.
 *
 * Este conjunto de tests valida la lógica previa a la persistencia,
 * asegurando que el repositorio:
 *
 * - Delegue correctamente todas las operaciones al DataSource local.
 * - No contenga lógica adicional inesperada.
 * - No dependa de Room ni de Android (test unitario puro).
 * - Mantenga el contrato definido por la interfaz de dominio.
 *
 * Importancia:
 * La capa de dominio solo conoce la interfaz `DispositivoRepository`.
 * Este test garantiza que la implementación concreta usada por el módulo móvil
 * cumple su responsabilidad sin acoplarse a la base de datos real.
 */
class SDispositivoRepositoryImplTest {

    /** DataSource local mockeado para aislar la lógica del repositorio. */
    private val dataSource = mock<DispositivoLocalDataSource>()

    /** Repositorio real bajo prueba, usando el DataSource simulado. */
    private val repository = DispositivoRepositoryImpl(dataSource)

    /**
     * Caso: Inserción de un dispositivo.
     *
     * Verifica que el repositorio delega la operación de inserción
     * al DataSource con el mismo objeto recibido.
     */
    @Test
    fun `repository inserts dispositivo through datasource`() = runTest {
        val dispositivo = Dispositivo(
            id = "device01",
            nombre = "Tablet Cocina",
            ip = "192.168.1.45",
            puerto = 8080,
            nivelBatery = null,
            estado = EstadoDispositivo.DESCONOCIDO,
            aesKey = "ABC123"
        )

        repository.insertDispositivo(dispositivo)

        verify(dataSource).insertDispositivo(eq(dispositivo))
    }

    /**
     * Caso: Eliminación de un dispositivo.
     *
     * Comprueba que el repositorio llama al método correspondiente del DataSource
     * pasando exactamente el mismo objeto de dominio.
     */
    @Test
    fun `repository deletes dispositivo through datasource`() = runTest {
        val dispositivo = Dispositivo(
            id = "device01",
            nombre = "Tablet Cocina",
            ip = "192.168.1.45",
            puerto = 8080,
            nivelBatery = null,
            estado = EstadoDispositivo.DESCONOCIDO,
            aesKey = "ABC123"
        )

        repository.deleteDispositivo(dispositivo)

        verify(dataSource).deleteDispositivo(eq(dispositivo))
    }

    /**
     * Caso: Actualización de un dispositivo.
     *
     * Valida que el repositorio delega correctamente la operación de actualización
     * sin modificar el objeto recibido.
     */
    @Test
    fun `repository updates dispositivo through datasource`() = runTest {
        val dispositivo = Dispositivo(
            id = "device01",
            nombre = "Tablet Cocina",
            ip = "192.168.1.45",
            puerto = 8080,
            nivelBatery = null,
            estado = EstadoDispositivo.DESCONOCIDO,
            aesKey = "ABC123"
        )

        repository.updateDispositivo(dispositivo)

        verify(dataSource).updateDispositivo(eq(dispositivo))
    }

    /**
     * Caso: Obtención de la lista de dispositivos.
     *
     * Se mockea un flujo con datos simulados y se comprueba que el repositorio
     * devuelve exactamente el mismo flujo sin alterarlo.
     */
    @Test
    fun `repository gets dispositivos from datasource`() = runTest {
        val expectedList = listOf(
            Dispositivo(
                id = "device01",
                nombre = "Tablet Cocina",
                ip = "192.168.1.45",
                puerto = 8080,
                nivelBatery = null,
                estado = EstadoDispositivo.DESCONOCIDO,
                aesKey = "ABC123"
            )
        )

        whenever(dataSource.getDispositivos()).thenReturn(flowOf(expectedList))

        val result = repository.getDispositivos().first()

        assert(result == expectedList)
    }
}
