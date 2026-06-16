package com.ajpr00.tabletapp.data.repository

import com.ajpr00.tablet.data.datasource.local.preferences.AppPreference
import com.ajpr00.tablet.data.repositoryImp.preference.TabletAppPreferencesRepositoryImpl
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Test unitarios para `TabletAppPreferencesRepositoryImpl`.
 *
 * ## Qué se prueba aquí
 * Este test verifica que el repositorio:
 * - Delegue correctamente las lecturas hacia `AppPreference`.
 * - Delegue correctamente las escrituras hacia `AppPreference`.
 * - No añada lógica adicional (domain no debe tener lógica en data).
 *
 * ## Por qué es importante
 * La capa data debe ser completamente determinista y delegar sin modificar datos.
 * Estos tests garantizan que el repositorio cumple su rol de "puente" entre domain y DataStore.
 */
class TabletAppPreferencesRepositoryImplTest {

    private val prefs = mock<AppPreference>()
    private val repository = TabletAppPreferencesRepositoryImpl(prefs)

    // ---------------------------------------------------------
    // CONFIGURACIÓN GENERAL
    // ---------------------------------------------------------

    @Test
    fun `repository gets dark mode`() = runTest {
        whenever(prefs.isDarkMode).thenReturn(flowOf(true))

        val result = repository.isDarkMode().first()

        assert(result)
    }

    @Test
    fun `repository sets dark mode`() = runTest {
        repository.setDarkMode(true)

        verify(prefs).setDarkMode(eq(true))
    }

    @Test
    fun `repository gets language`() = runTest {
        whenever(prefs.language).thenReturn(flowOf("es"))

        val result = repository.getLanguage().first()

        assert(result == "es")
    }

    @Test
    fun `repository sets language`() = runTest {
        repository.setLanguage("en")

        verify(prefs).setLanguage(eq("en"))
    }

    @Test
    fun `repository gets first run`() = runTest {
        whenever(prefs.isFirstRun).thenReturn(flowOf(true))

        val result = repository.isFirstRun().first()

        assert(result)
    }

    @Test
    fun `repository sets first run completed`() = runTest {
        repository.setFirstRunCompleted()

        verify(prefs).setFirstRunCompleted()
    }

    // ---------------------------------------------------------
    // IDENTIDAD DEL DISPOSITIVO
    // ---------------------------------------------------------

    @Test
    fun `repository gets device id`() = runTest {
        whenever(prefs.tabletId).thenReturn(flowOf("tablet-123"))

        val result = repository.getDeviceId().first()

        assert(result == "tablet-123")
    }

    @Test
    fun `repository sets device id`() = runTest {
        repository.setDeviceId("tablet-123")

        verify(prefs).setTabletId(eq("tablet-123"))
    }

    @Test
    fun `repository gets device name`() = runTest {
        whenever(prefs.tabletName).thenReturn(flowOf("Tablet Cocina"))

        val result = repository.getDeviceName().first()

        assert(result == "Tablet Cocina")
    }

    @Test
    fun `repository sets device name`() = runTest {
        repository.setDeviceName("Tablet Cocina")

        verify(prefs).setTabletName(eq("Tablet Cocina"))
    }

    // ---------------------------------------------------------
    // ESTADO DE CONEXIÓN
    // ---------------------------------------------------------

    @Test
    fun `repository gets device connected state`() = runTest {
        whenever(prefs.isMobileConnected).thenReturn(flowOf(true))

        val result = repository.isDeviceConnected().first()

        assert(result)
    }

    @Test
    fun `repository sets device connected state`() = runTest {
        repository.setDeviceConnected(true)

        verify(prefs).setMobileConnected(eq(true))
    }

    // ---------------------------------------------------------
    // TOKEN
    // ---------------------------------------------------------

    @Test
    fun `repository gets token`() = runTest {
        whenever(prefs.token).thenReturn(flowOf("abc123"))

        val result = repository.getToken().first()

        assert(result == "abc123")
    }

    @Test
    fun `repository sets token`() = runTest {
        repository.setToken("abc123")

        verify(prefs).setToken(eq("abc123"))
    }

    // ---------------------------------------------------------
    // AES KEY
    // ---------------------------------------------------------

    @Test
    fun `repository gets aes key`() = runTest {
        val key = byteArrayOf(1, 2, 3)
        whenever(prefs.aesKey).thenReturn(flowOf(key))

        val result = repository.getAesKey().first()

        assert(result!!.contentEquals(key))
    }

    @Test
    fun `repository sets aes key`() = runTest {
        val key = byteArrayOf(1, 2, 3)

        repository.setAesKey(key)

        verify(prefs).setAesKey(eq(key))
    }
}