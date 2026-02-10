package com.example.chelaspokerdice.apiClient

import com.example.chelaspokerdice.domain.User

interface UserAPIInterface {
    suspend fun login(username: String, password: String): User?
    suspend fun create(username: String, password: String, inviteCode: String): User?
    suspend fun getInviteCode(token: String): String?
}
