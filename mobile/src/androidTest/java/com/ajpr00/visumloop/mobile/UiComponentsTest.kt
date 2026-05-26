package com.ajpr00.visumloop.mobile

import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.ajpr00.core.domain.model.EstadoDispositivo
import com.ajpr00.mobile.ui.components.DispositivoCard
import com.ajpr00.mobile.ui.components.DrawerMenu
import com.ajpr00.mobile.ui.components.FabAdd
import com.ajpr00.mobile.ui.components.FloatingBottomBar
import com.ajpr00.mobile.ui.model.DispositivoUi
import org.junit.Rule
import org.junit.Test

class UiComponentsTest {

    @get:Rule
    val composeRule = createComposeRule()

    // ---------------------------------------------------------
    // FloatingBottomBar
    // ---------------------------------------------------------
    @Test
    fun floatingBottomBar_tiene_botones_accesibles() {
        composeRule.setContent {
            FloatingBottomBar()
        }

        composeRule.onNodeWithContentDescription("Configuración").assertExists()
        composeRule.onNodeWithContentDescription("Eliminar").assertExists()
    }

    @Test
    fun floatingBottomBar_botones_tienen_tamano_minimo() {
        composeRule.setContent {
            FloatingBottomBar()
        }

        composeRule.onNodeWithContentDescription("Configuración")
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)

        composeRule.onNodeWithContentDescription("Eliminar")
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)
    }

    // ---------------------------------------------------------
    // DispositivoCard
    // ---------------------------------------------------------
    @Test
    fun dispositivoCard_muestra_nombre_estado_y_bateria() {
        val dispositivo = DispositivoUi(
            id = "1",
            nombre = "Tablet Cocina",
            estado = EstadoDispositivo.ONLINE,
            nivelBatery = 87,
            icono = R.drawable.ic_battery_full_24
        )

        composeRule.setContent {
            DispositivoCard(
                size = 400.dp,
                dispositivo = dispositivo
            )
        }

        composeRule.onNodeWithText("Tablet Cocina").assertExists()
        composeRule.onNodeWithText("Online").assertExists()
        composeRule.onNodeWithText("87%").assertExists()
    }

    // ---------------------------------------------------------
    // FabAdd
    // ---------------------------------------------------------
    @Test
    fun fabAdd_tiene_content_description() {
        composeRule.setContent {
            FabAdd(
                icon = androidx.compose.ui.res.painterResource(id = R.drawable.ic_battery_full_24),
                icDesc = "Añadir dispositivo",
                onClick = {}
            )
        }

        composeRule.onNodeWithContentDescription("Añadir dispositivo").assertExists()
    }

    @Test
    fun fabAdd_tiene_tamano_minimo() {
        composeRule.setContent {
            FabAdd(
                icon = androidx.compose.ui.res.painterResource(id = R.drawable.ic_battery_full_24),
                icDesc = "Añadir dispositivo",
                onClick = {}
            )
        }

        composeRule.onNodeWithContentDescription("Añadir dispositivo")
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)
    }

    // ---------------------------------------------------------
    // DrawerMenu
    // ---------------------------------------------------------
    @Test
    fun drawerMenu_muestra_todos_los_items() {
        composeRule.setContent {
            DrawerMenu(
                selectedItem = "inicio",
                onItemSelected = {}
            )
        }

        composeRule.onNodeWithText("Inicio").assertExists()
        composeRule.onNodeWithText("Perfil").assertExists()
        composeRule.onNodeWithText("Ajustes").assertExists()
        composeRule.onNodeWithText("Ayuda").assertExists()
    }
}
