package com.example.chelaspokerdice.services

import com.example.chelaspokerdice.domain.User
import kotlinx.coroutines.flow.Flow

interface PlayerServiceInterface {

    val currentUser: Flow<User?>
    suspend fun set(id: Int, username: String, token: String)
    suspend fun clear()
    suspend fun login(username: String, password: String): User?

    suspend fun logout()
    suspend fun register(username: String, password: String, inviteCode: String): User?
    suspend fun getInviteCode(): String?
}