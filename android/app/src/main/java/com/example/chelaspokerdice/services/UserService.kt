package com.example.chelaspokerdice.services

import com.example.chelaspokerdice.apiClient.UserAPIClient
import com.example.chelaspokerdice.domain.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class UserService(
    private val userDataStore: DataStoreUserService,
    private val userApi: UserAPIClient
): PlayerServiceInterface {
    override val currentUser: Flow<User?>
        get() = userDataStore.currentUser

    override suspend fun set(id: Int, username: String, token: String) {
        userDataStore.set(id, username, token)
    }

    override suspend fun clear() {
        userDataStore.clear()
    }

    override suspend fun login(
        username: String,
        password: String
    ): User? {
        val user = userApi.login(username, password)
        if (user != null) {
            userDataStore.set(user.id, user.username, user.token)
        }
        return user
    }

    override suspend fun register(
        username: String,
        password: String,
        inviteCode: String
    ): User? {
        val user = userApi.create(username, password, inviteCode)
        if(user != null) {
            userDataStore.set(user.id, user.username, user.token)
        }
        return user
    }

    override suspend fun logout() {
        userDataStore.logout()
    }

    override suspend fun getInviteCode(): String? {
        val user = userDataStore.currentUser.first()
        val token = user?.token

        return if (token != null) {
            userApi.getInviteCode(token)
        } else {
            null
        }
    }

}