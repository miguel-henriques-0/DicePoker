package pt.isel.daw.dicepoker.services

import jakarta.inject.Named
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import pt.isel.daw.dicepoker.domain.invite.InviteCode
import pt.isel.daw.dicepoker.domain.users.Token
import pt.isel.daw.dicepoker.domain.users.User
import pt.isel.daw.dicepoker.domain.users.UsersDomain
import pt.isel.daw.dicepoker.repository.TransactionManager
import pt.isel.daw.dicepoker.utils.Either
import pt.isel.daw.dicepoker.utils.failure
import pt.isel.daw.dicepoker.utils.success

data class TokenExternalInfo(
    val tokenValue: String,
    val tokenExpiration: Instant,
)

sealed class UserCreationError {
    data object UserAlreadyExists : UserCreationError()

    data object InsecurePassword : UserCreationError()

    data object InvalidInviteCode : UserCreationError()
}
typealias UserCreationResult = Either<UserCreationError, Int>

sealed class TokenCreationError {
    data object UserOrPasswordAreInvalid : TokenCreationError()
}
typealias TokenCreationResult = Either<TokenCreationError, TokenExternalInfo>

sealed class InviteCodeCreationError {
    data object MaxInvitesReached : InviteCodeCreationError()
}
typealias InviteCodeCreationResult = Either<InviteCodeCreationError, InviteCode>

@Named
class UsersService(
    private val transactionManager: TransactionManager,
    private val usersDomain: UsersDomain,
    private val clock: Clock,
) {
    fun createUser(
        username: String,
        password: String,
        inviteCode: Long,
    ): UserCreationResult {
        if (!usersDomain.isSafePassword(password)) {
            return failure(UserCreationError.InsecurePassword)
        }

        val passwordValidationInfo = usersDomain.createPasswordValidationInformation(password)

        return transactionManager.run {
            val usersRepository = it.usersRepository

            if (!usersRepository.isInviteCodeValid(inviteCode)) {
                return@run failure(UserCreationError.InvalidInviteCode)
            }

            if (usersRepository.isUserStoredByUsername(username)) {
                failure(UserCreationError.UserAlreadyExists)
            } else {
                val id = usersRepository.storeUser(username, passwordValidationInfo)
                usersRepository.updateInviteCodeAsUsed(inviteCode, id)
                success(id)
            }
        }
    }

    fun createToken(
        username: String,
        password: String,
    ): TokenCreationResult {
        if (username.isBlank() || password.isBlank()) {
            failure(TokenCreationError.UserOrPasswordAreInvalid)
        }
        return transactionManager.run {
            val usersRepository = it.usersRepository
            val user: User =
                usersRepository.getUserByUsername(username)
                    ?: return@run failure(TokenCreationError.UserOrPasswordAreInvalid)
            if (!usersDomain.validatePassword(password, user.passwordValidation)) {
                return@run failure(TokenCreationError.UserOrPasswordAreInvalid)
            }
            val tokenValue = usersDomain.generateTokenValue()
            val now = clock.now()
            val newToken =
                Token(
                    usersDomain.createTokenValidationInformation(tokenValue),
                    user.id,
                    createdAt = now,
                    lastUsedAt = now,
                )
            usersRepository.createToken(newToken, usersDomain.maxNumberOfTokensPerUser)
            Either.Right(
                TokenExternalInfo(
                    tokenValue,
                    usersDomain.getTokenExpiration(newToken),
                ),
            )
        }
    }

    fun getUserByToken(token: String): User? {
        if (!usersDomain.canBeToken(token)) {
            return null
        }
        return transactionManager.run {
            val usersRepository = it.usersRepository
            val tokenValidationInfo = usersDomain.createTokenValidationInformation(token)
            val userAndToken = usersRepository.getTokenByTokenValidationInfo(tokenValidationInfo)
            if (userAndToken != null && usersDomain.isTokenTimeValid(clock, userAndToken.second)) {
                usersRepository.updateTokenLastUsed(userAndToken.second, clock.now())
                userAndToken.first
            } else {
                null
            }
        }
    }

    fun revokeToken(token: String): Boolean {
        val tokenValidationInfo = usersDomain.createTokenValidationInformation(token)
        return transactionManager.run {
            it.usersRepository.removeTokenByValidationInfo(tokenValidationInfo)
            true
        }
    }

    fun createInviteCode(userId: Int): InviteCodeCreationResult {
        return transactionManager.run {
            val res =
                it.usersRepository.createInviteCode(
                    userId,
                    usersDomain.maxInvitesPerUser,
                )
            if (res != null) {
                success(res)
            } else {
                failure(InviteCodeCreationError.MaxInvitesReached)
            }
        }
    }
}
