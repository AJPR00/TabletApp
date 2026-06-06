package com.ajpr00.visumloop.mobile.qr.parse

import com.ajpr00.mobile.qr.parse.QrParser
import com.google.gson.JsonSyntaxException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Test unitario encargado de validar el parseo del QR en el módulo móvil.
 *
 * Este test garantiza que el contenido JSON recibido desde el QR:
 * - Se interpreta correctamente cuando es válido.
 * - Lanza una excepción cuando el formato es incorrecto.
 * - Gestiona correctamente campos opcionales o ausentes.
 *
 */
class QrParserTest {

    private val parser = QrParser()

    /**
     * Verifica que un QR válido se parsea correctamente y que todos los campos
     * del JSON se asignan al modelo QrPayload sin alteraciones.
     *
     * Caso cubierto:
     * - JSON bien formado
     * - Todos los campos presentes
     */
    @Test
    fun `parse valid QR returns correct connection data`() {
        val json = """
            {
                "id": "tablet01",
                "nombre": "Tablet Cocina",
                "ip": "192.168.1.45",
                "puerto": 8080,
                "aesKey": "ABC123"
            }
        """.trimIndent()

        val payload = parser.parse(json)

        assertEquals("tablet01", payload.id)
        assertEquals("Tablet Cocina", payload.nombre)
        assertEquals("192.168.1.45", payload.ip)
        assertEquals(8080, payload.puerto)
        assertEquals("ABC123", payload.aesKey)
    }

    /**
     * Verifica que un JSON mal formado provoca una excepción de tipo
     * JsonSyntaxException, lo cual demuestra que el parser detecta errores
     * estructurales en el contenido del QR.
     *
     * Caso cubierto:
     * - JSON inválido
     * - Estructura rota
     */
    @Test
    fun `parse invalid JSON throws exception`() {
        val invalidJson = "{ id: 123, wrong }"

        assertThrows(JsonSyntaxException::class.java) {
            parser.parse(invalidJson)
        }
    }

    /**
     * Verifica que el parser puede manejar JSONs incompletos y que los campos
     * ausentes se asignan como null, tal y como hace Gson por defecto.
     *
     * Caso cubierto:
     * - JSON válido pero con campos opcionales faltantes
     * - Comprobación de valores por defecto
     */
    @Test
    fun `parse QR missing fields still parses but with defaults`() {
        val json = """
            {
                "id": "tablet01",
                "ip": "10.0.0.1",
                "puerto": 8080
            }
        """.trimIndent()

        val payload = parser.parse(json)

        assertEquals("tablet01", payload.id)
        assertEquals("10.0.0.1", payload.ip)
        assertEquals(8080, payload.puerto)

        // Campos faltantes → Gson los deja en null
        assertEquals(null, payload.nombre)
        assertEquals(null, payload.aesKey)
    }
}
