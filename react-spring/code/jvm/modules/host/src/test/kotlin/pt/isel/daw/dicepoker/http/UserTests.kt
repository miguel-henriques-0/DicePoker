package pt.isel.daw.dicepoker.http

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import pt.isel.daw.dicepoker.http.model.InviteResponse
import pt.isel.daw.dicepoker.http.model.TokenResponse
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test

const val FIRST_USER_USERNAME: String = "firstUser"
const val FIRST_USER_PASSWORD: String = "password1"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserTests {
    // One of the very few places where we use property injection
    @LocalServerPort
    var port: Int = 0

    @Test
    fun `access user home, and logout`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        val result =
            client.post().uri("/users/token")
                .bodyValue(
                    mapOf(
                        "username" to FIRST_USER_USERNAME,
                        "password" to FIRST_USER_PASSWORD,
                    ),
                )
                .exchange()
                .expectStatus().isOk
                .expectBody(TokenResponse::class.java)
                .returnResult()
                .responseBody!!
        val username = FIRST_USER_USERNAME

        // when: getting the user home with a valid token
        // then: the response is a 200 with the proper representation
        client.get().uri("/me")
            .header("Authorization", "Bearer ${result.token}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("username").isEqualTo(username)

        // when: getting the user home with an invalid token
        // then: the response is a 4001 with the proper problem
        client.get().uri("/me")
            .header("Authorization", "Bearer ${result.token}-invalid")
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().valueEquals("WWW-Authenticate", "bearer")

        // when: revoking the token
        // then: response is a 200
        client.post().uri("/logout")
            .header("Authorization", "Bearer ${result.token}")
            .exchange()
            .expectStatus().isOk

        // when: getting the user home with the revoked token
        // then: response is a 401
        client.get().uri("/me")
            .header("Authorization", "Bearer ${result.token}")
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().valueEquals("WWW-Authenticate", "bearer")
    }

    @Test
    fun `get invite code from firstUser and create another user with that code`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        val tokenResult =
            client.post().uri("/users/token")
                .bodyValue(
                    mapOf(
                        "username" to FIRST_USER_USERNAME,
                        "password" to FIRST_USER_PASSWORD,
                    ),
                )
                .exchange()
                .expectStatus().isOk
                .expectBody(TokenResponse::class.java)
                .returnResult()
                .responseBody!!

        client.get().uri("/users/createInvite")
            .header("Authorization", "Bearer ${tokenResult.token}")

        val inviteCodeResponse =
            client.get().uri("/users/createInvite")
                .header("Authorization", "Bearer ${tokenResult.token}")
                .exchange()
                .expectStatus().isCreated
                .expectBody(InviteResponse::class.java)
                .returnResult()
                .responseBody!!

        // when: creating a new user with the invite code
        val newUserName = newTestUserName()
        val newUserPassword = "changeit"

        client.post().uri("/users")
            .bodyValue(
                mapOf(
                    "username" to newUserName,
                    "password" to newUserPassword,
                    "inviteCode" to inviteCodeResponse.inviteCode,
                ),
            )
            .exchange()
            .expectStatus().isCreated
    }

    @Test
    fun `cannot create user with an used invite code`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: user info
        val firstUserTokenResult =
            client.post().uri("/users/token")
                .bodyValue(
                    mapOf(
                        "username" to FIRST_USER_USERNAME,
                        "password" to FIRST_USER_PASSWORD,
                    ),
                )
                .exchange()
                .expectStatus().isOk
                .expectBody(TokenResponse::class.java)
                .returnResult()
                .responseBody!!

        val newUserNameWithValidCode = newTestUserName()
        val newUserPasswordWithValidCode = "changeit"

        val inviteCodeResponse =
            client.get().uri("/users/createInvite")
                .header("Authorization", "Bearer ${firstUserTokenResult.token}")
                .exchange()
                .expectStatus().isCreated
                .expectBody(InviteResponse::class.java)
                .returnResult()
                .responseBody!!

        client.post().uri("/users")
            .bodyValue(
                mapOf(
                    "username" to newUserNameWithValidCode,
                    "password" to newUserPasswordWithValidCode,
                    "inviteCode" to inviteCodeResponse.inviteCode,
                ),
            )
            .exchange()
            .expectStatus().isCreated

        // when: creating another user with the same invite code
        val newUserNameWithUsedCode = newTestUserName()
        val newUserPasswordWithUsedCode = "changeit"

        client.post().uri("/users")
            .bodyValue(
                mapOf(
                    "username" to newUserNameWithUsedCode,
                    "password" to newUserPasswordWithUsedCode,
                    "inviteCode" to inviteCodeResponse.inviteCode,
                ),
            )
            .exchange()
            .expectStatus().isBadRequest
    }

    companion object {
        private fun newTestUserName() = "user-${abs(Random.nextLong())}"
    }
}
