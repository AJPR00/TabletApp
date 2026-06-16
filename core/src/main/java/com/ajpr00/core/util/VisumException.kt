package com.ajpr00.core.util

/**
 * Excepción universal del dominio.
 * NO contiene textos traducidos.
 * NO contiene mensajes para UI.
 * Solo claves semánticas.
 */
open class VisumException(val code: String) : Exception(code) {}