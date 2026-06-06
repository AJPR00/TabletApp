package com.ajpr00.tabletapp.data.repository

import com.ajpr00.tablet.data.datasource.local.preferences.AppPreference
import com.ajpr00.tablet.data.repositoryImp.PreferencesRepositoryImpl
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Test unitario que valida la lógica previa a la persistencia en DataStore
 * para el repositorio de preferencias del módulo tablet.
 *
 * Este test NO usa DataStore real. Se mockea AppPreference para comprobar:
 * - Delegación correcta de operaciones de lectura y escritura.
 * - Emisión de Flows con valores esperados.
 * - Correcto funcionamiento de métodos suspend.
 *
 * Importancia:
 * La capa de dominio solo conoce la interfaz PreferencesRepository.
 * Este test garantiza que la implementación concreta usada por la tablet
 * funciona correctamente sin depender de DataStore real.
 */
class PreferencesRepositoryImplTest {

    private val prefs = mock<AppPreference>()
    private val repository = PreferencesRepositoryImpl(prefs)

    /**
     * Verifica que isDarkMode() devuelve el Flow emitido por AppPreference.
     */
    @Test
    fun `repository gets dark mode state`() = runTest {
        whenever(prefs.isDarkMode).thenReturn(flowOf(true))

        val result = repository.isDarkMode().first()

        assert(result)
    }

    /**
     * Verifica que setDarkMode() delega correctamente en AppPreference.
     */
    @Test
    fun `repository sets dark mode`() = runTest {
        repository.setDarkMode(true)

        verify(prefs).setDarkMode(eq(true))
    }

    /**
     * Verifica que getLanguage() devuelve el Flow emitido por AppPreference.
     */
    @Test
    fun `repository gets language`() = runTest {
        whenever(prefs.language).thenReturn(flowOf("es"))

        val result = repository.getLanguage().first()

        assert(result == "es")
    }

    /**
     * Verifica que setLanguage() delega correctamente en AppPreference.
     */
    @Test
    fun `repository sets language`() = runTest {
        repository.setLanguage("en")

        verify(prefs).setLanguage(eq("en"))
    }

    /**
     * Verifica que isFirstRun() devuelve el Flow emitido por AppPreference.
     */
    @Test
    fun `repository gets first run state`() = runTest {
        whenever(prefs.isFirstRun).thenReturn(flowOf(true))

        val result = repository.isFirstRun().first()

        assert(result)
    }

    /**
     * Verifica que setFirstRunCompleted() delega correctamente en AppPreference.
     */
    @Test
    fun `repository sets first run completed`() = runTest {
        repository.setFirstRunCompleted()

        verify(prefs).setFirstRunCompleted()
    }

    /**
     * Verifica que getTabletId() devuelve el Flow emitido por AppPreference.
     */
    @Test
    fun `repository gets tablet id`() = runTest {
        whenever(prefs.tabletId).thenReturn(flowOf("tablet-123"))

        val result = repository.getTabletId().first()

        assert(result == "tablet-123")
    }

    /**
     * Verifica que setTabletId() delega correctamente en AppPreference.
     */
    @Test
    fun `repository sets tablet id`() = runTest {
        repository.setTabletId("tablet-123")

        verify(prefs).setTabletId(eq("tablet-123"))
    }

    /**
     * Verifica que getTabletName() devuelve el Flow emitido por AppPreference.
     */
    @Test
    fun `repository gets tablet name`() = runTest {
        whenever(prefs.tabletName).thenReturn(flowOf("Tablet Cocina"))

        val result = repository.getTabletName().first()

        assert(result == "Tablet Cocina")
    }

    /**
     * Verifica que setTabletName() delega correctamente en AppPreference.
     */
    @Test
    fun `repository sets tablet name`() = runTest {
        repository.setTabletName("Tablet Cocina")

        verify(prefs).setTabletName(eq("Tablet Cocina"))
    }
}
