package com.ajpr00.visumloop.tablet.data.repository

import com.ajpr00.visumloop.tablet.domain.model.UserGoogle
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    val currentUser: Flow<UserGoogle?>
    suspend fun logout(): Result<Unit>
    //suspend fun tryAutoLoginLocal()

}