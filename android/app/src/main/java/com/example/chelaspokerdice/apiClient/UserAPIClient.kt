package com.example.chelaspokerdice.apiClient

import android.util.Log
import com.example.chelaspokerdice.domain.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

class UserAPIClient(
    private val httpClient: HttpClient
): UserAPIInterface {

    override suspend fun login(
        username: String,
        password: String
    ): User? {

        val response = httpClient.post {
            url("http://10.0.2.2:8180/api/users/token")
            contentType(ContentType.Application.Json)
            setBody(LoginDTO(username, password))
        }

        if (response.status != HttpStatusCode.OK) {
            Log.e("UserAPIClient", "Login failed: ${response.status}")
            return null
        }

        val authUser = response.body<UserDTO>()

        val userHome = httpClient.get {
            url("http://10.0.2.2:8180/api/me")
            header("Authorization", "Bearer ${authUser.token}")
        }.body<UserHomeDTO>()

        return User(userHome.id, username, authUser.token)
    }

    override suspend fun create(username: String, password: String, inviteCode: String): User? {
        val response = httpClient.post {
            url("http://10.0.2.2:8180/api/users")
            contentType(ContentType.Application.Json)
            setBody(RegisterDTO(username, password, inviteCode))
        }

        if (response.status != HttpStatusCode.Created) {
            return null
        }

        return login(username, password)
    }

    override suspend fun getInviteCode(token: String): String? {
        val response = httpClient.get {
            url("http://10.0.2.2:8180/api/users/createInvite")
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
        }

        if (response.status != HttpStatusCode.Created) {
            Log.e("UserAPIClient", "Get invite code failed: ${response.status}")
            return null
        }
        return response.body<InviteCodeDTO>().inviteCode
    }
}

@Serializable
data class UserDTO(
    val token: String
)

@Serializable
data class UserHomeDTO(
    val id: Int,
    val username: String
)

@Serializable
data class LoginDTO(
    val username: String,
    val password: String
)

@Serializable
data class RegisterDTO(
    val username: String,
    val password: String,
    val inviteCode: String
)

@Serializable
data class InviteCodeDTO(
    val inviteCode: String
)