package com.ajpr00.tablet.presentation.server

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.tablet.data.server.TabletServer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServerViewModel @Inject constructor(
    private val server: TabletServer
) : ViewModel() {

    init {
        startServer()
    }

    private fun startServer() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("TabletServer", "🚀 Iniciando servidor en puerto 8080...")
                server.start()
                Log.d("TabletServer", "✅ Servidor iniciado correctamente")
            } catch (e: Exception) {
                Log.e("TabletServer", "❌ Error al iniciar el servidor: ${e.message}", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("TabletServer", "🛑 Servidor detenido")
        server.stop()
    }
}
