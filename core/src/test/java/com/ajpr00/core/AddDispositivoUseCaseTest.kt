package com.ajpr00.core

import com.ajpr00.core.domain.model.Dispositivo
import com.ajpr00.core.domain.repository.dispositivo.DispositivoRepository
import com.ajpr00.core.domain.usecase.dispositivo.AddDispositivoUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AddDispositivoUseCaseTest {

    // Fake repository para testear sin Android ni base de datos
    private class FakeRepository : DispositivoRepository {
        var ultimoInsertado: Dispositivo? = null

        override fun getDispositivos(): Flow<List<Dispositivo>> {
            throw NotImplementedError("No necesario para este test")
        }

        override suspend fun insertDispositivo(dispositivo: Dispositivo) {
            ultimoInsertado = dispositivo
        }

        override suspend fun deleteDispositivo(dispositivo: Dispositivo) {
            throw NotImplementedError("No necesario para este test")
        }

        override suspend fun updateDispositivo(dispositivo: Dispositivo) {
            throw NotImplementedError("No necesario para este test")
        }
    }

    @Test
    fun `inserta correctamente un dispositivo`() = runBlocking {
        // Arrange
        val fakeRepo = FakeRepository()
        val useCase = AddDispositivoUseCase(fakeRepo)
        val dispositivo = Dispositivo(
            id = "1",
            nombre = "Tablet",
            ip = "192.168.1.10",
            puerto = 8080
        )

        // Act
        useCase(dispositivo)

        // Assert
        assertEquals(dispositivo, fakeRepo.ultimoInsertado)
    }
}
