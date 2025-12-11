package com.ajpr00.visumloop.tablet.navigation

import com.ajpr00.visumloop.tablet.domain.model.AccesLoginType
import kotlinx.serialization.Serializable

@Serializable
object ScreenPrincipal

@Serializable
object Reproductor

@Serializable
object MenuOpciones

@Serializable
object LoginScreen

@Serializable
object LoginDrive

@Serializable
data class LoginGraph(val acessType: AccesLoginType = AccesLoginType.LOCAL)
@Serializable
object RegisterScreen

@Serializable
object FromRecover

@Serializable
object About

@Serializable
object Splash

@Serializable
object MainGraph