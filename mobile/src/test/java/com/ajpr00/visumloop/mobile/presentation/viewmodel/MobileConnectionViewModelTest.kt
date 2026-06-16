package com.ajpr00.visumloop.mobile.presentation.viewmodel

import com.ajpr00.core.domain.model.Dispositivo
import com.ajpr00.core.domain.model.EstadoDispositivo
import com.ajpr00.core.domain.model.mDNS.MdnsServiceInfo
import com.ajpr00.core.domain.unum.ConnectionMode
import com.ajpr00.core.domain.usecase.user.IsLoginStateUseCase
import com.ajpr00.core.domain.usecase.network.CheckInternetConnectionUseCase
import com.ajpr00.data.repository.tablet.DiscovermDNSTabletUseCase
import com.ajpr00.data.repository.tablet.GetDispositivosUseCase
import com.ajpr00.data.repository.tablet.UpdateDispositivoUseCase
import com.ajpr00.core.domain.repository.dispositivo.TabletLocatorRepository
import com.ajpr00.mobile.presentation.viewmodel.MobileConnectionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import org.junit.Assert.assertEquals

data class TestMdnsServiceInfo(val name: String, val ip: String, val port: Int, val txt: String)

@OptIn(ExperimentalCoroutinesApi::class)
class MobileConnectionViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val scope = TestScope(dispatcher)

    private val isLoginStateUseCase: IsLoginStateUseCase = mock()
    private val checkInternetConnectionUseCase: CheckInternetConnectionUseCase = mock()
    private val discovermDNSTabletUseCase: DiscovermDNSTabletUseCase = mock()
    private val getDispositivosUseCase: GetDispositivosUseCase = mock()
    private val updateDispositivoUseCase: UpdateDispositivoUseCase = mock()
    private val tabletLocatorRepository: TabletLocatorRepository = mock()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)

        whenever(isLoginStateUseCase()).thenReturn(flowOf(false))
        runTest {
            whenever(checkInternetConnectionUseCase()).thenReturn(false)
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun lan_detection_sets_connectionMode_LAN() = scope.runTest {
        val dbTablet = Dispositivo(
            id = "t1",
            nombre = "Tablet 1",
            ip = "192.168.0.10",
            estado = EstadoDispositivo.DESCONOCIDO
        )

        whenever(getDispositivosUseCase()).thenReturn(flowOf(listOf(dbTablet)))

        val mdnsFlow: Flow<MdnsServiceInfo> = flow {
            emit(MdnsServiceInfo("tablet-t1", "192.168.0.99", 1234, "t1"))
        }

        whenever(discovermDNSTabletUseCase()).thenReturn(mdnsFlow)
        whenever(updateDispositivoUseCase.invoke(any())).thenReturn(Unit)

        val vm = MobileConnectionViewModel(
            isLoginStateUseCase,
            checkInternetConnectionUseCase,
            discovermDNSTabletUseCase,
            getDispositivosUseCase,
            updateDispositivoUseCase,
            tabletLocatorRepository
        )

        vm.startMdnsDiscovery(2000)
        advanceUntilIdle()

        verify(updateDispositivoUseCase).invoke(check {
            assert(it.id == "t1")
            assert(it.ip == "192.168.0.99")
        })

        assertEquals(ConnectionMode.LAN, vm.connectionMode.value)
    }
}
