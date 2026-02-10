package pt.isel.daw.dicepoker.repository.jdbi

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import org.slf4j.LoggerFactory
import pt.isel.daw.dicepoker.domain.invite.InviteCode
import pt.isel.daw.dicepoker.domain.users.PasswordValidationInfo
import pt.isel.daw.dicepoker.domain.users.Token
import pt.isel.daw.dicepoker.domain.users.TokenValidationInfo
import pt.isel.daw.dicepoker.domain.users.User
import pt.isel.daw.dicepoker.repository.UsersRepository

class JdbiUsersRepository(
    private val handle: Handle,
) : UsersRepository {
    override fun getUserByUsername(username: String): User? =
        handle.createQuery("select * from dbo.Users where username = :username")
            .bind("username", username)
            .mapTo<User>()
            .singleOrNull()

    override fun storeUser(
        username: String,
        passwordValidation: PasswordValidationInfo,
    ): Int =
        handle.createUpdate(
            """
            insert into dbo.Users (username, password_validation) values (:username, :password_validation)
            """,
        )
            .bind("username", username)
            .bind("password_validation", passwordValidation.validationInfo)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

    override fun isUserStoredByUsername(username: String): Boolean =
        handle.createQuery("select count(*) from dbo.Users where username = :username")
            .bind("username", username)
            .mapTo<Int>()
            .single() == 1

    override fun createToken(
        token: Token,
        maxTokens: Int,
    ) {
        val deletions =
            handle.createUpdate(
                """
                delete from dbo.Tokens 
                where user_id = :user_id 
                    and token_validation in (
                        select token_validation from dbo.Tokens where user_id = :user_id 
                            order by last_used_at desc offset :offset
                    )
                """.trimIndent(),
            )
                .bind("user_id", token.userId)
                .bind("offset", maxTokens - 1)
                .execute()

        logger.info("{} tokens deleted when creating new token", deletions)

        handle.createUpdate(
            """
            insert into dbo.Tokens(user_id, token_validation, created_at, last_used_at) 
            values (:user_id, :token_validation, :created_at, :last_used_at)
            """.trimIndent(),
        )
            .bind("user_id", token.userId)
            .bind("token_validation", token.tokenValidationInfo.validationInfo)
            .bind("created_at", token.createdAt.epochSeconds)
            .bind("last_used_at", token.lastUsedAt.epochSeconds)
            .execute()
    }

    override fun updateTokenLastUsed(
        token: Token,
        now: Instant,
    ) {
        handle.createUpdate(
            """
            update dbo.Tokens
            set last_used_at = :last_used_at
            where token_validation = :validation_information
            """.trimIndent(),
        )
            .bind("last_used_at", now.epochSeconds)
            .bind("validation_information", token.tokenValidationInfo.validationInfo)
            .execute()
    }

    override fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<User, Token>? =
        handle.createQuery(
            """
                select id, username, password_validation, token_validation, created_at, last_used_at
                from dbo.Users as users 
                inner join dbo.Tokens as tokens 
                on users.id = tokens.user_id
                where token_validation = :validation_information
            """,
        )
            .bind("validation_information", tokenValidationInfo.validationInfo)
            .mapTo<UserAndTokenModel>()
            .singleOrNull()
            ?.userAndToken

    override fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int {
        return handle.createUpdate(
            """
                delete from dbo.Tokens
                where token_validation = :validation_information
            """,
        )
            .bind("validation_information", tokenValidationInfo.validationInfo)
            .execute()
    }

    override fun createInviteCode(
        userId: Int,
        inviteCodeLimit: Int,
    ): InviteCode? {
        val deletedCodes =
            handle.createUpdate(
                """
                delete from dbo.userinvite
                where created_by = :userId
                and invite_code in (
                    select invite_code from dbo.userinvite
                    where created_by = :userId
                    order by created_at desc offset :offset
                )
                """.trimIndent(),
            )
                .bind("userId", userId)
                .bind("offset", inviteCodeLimit - 1)
                .execute()


        return handle.createUpdate(
            """
            insert into dbo.userinvite(created_by, invite_code, created_at)
            values (:userId, :inviteCode, :createdAt)
            returning invite_code
            """.trimIndent(),
        )
            .bind("userId", userId)
            .bind("inviteCode", Math.random() * 9999)
            .bind("createdAt", Clock.System.now().epochSeconds)
            .executeAndReturnGeneratedKeys()
            .mapTo<InviteCode>()
            .singleOrNull()
    }

    override fun isInviteCodeValid(inviteCode: Long): Boolean {
        val code =
            handle.createQuery(
                """
                select * from dbo.userinvite
                where invite_code = :inviteCode
                and used_by is null
                """.trimIndent(),
            )
                .bind("inviteCode", inviteCode)
                .mapTo<InviteCode>()
                .singleOrNull()

        return code != null
    }

    override fun updateInviteCodeAsUsed(
        inviteCode: Long,
        usedBy: Int,
    ): Boolean {
        val rowsUpdated =
            handle.createUpdate(
                """
                update dbo.userinvite
                set used_by = :usedBy
                where invite_code = :inviteCode
                and used_by is null
                """.trimIndent(),
            )
                .bind("usedBy", usedBy)
                .bind("inviteCode", inviteCode)
                .execute()

        return rowsUpdated == 1
    }

    private data class UserAndTokenModel(
        val id: Int,
        val username: String,
        val passwordValidation: PasswordValidationInfo,
        val tokenValidation: TokenValidationInfo,
        val createdAt: Long,
        val lastUsedAt: Long,
    ) {
        val userAndToken: Pair<User, Token>
            get() =
                Pair(
                    User(id, username, passwordValidation),
                    Token(
                        tokenValidation,
                        id,
                        Instant.fromEpochSeconds(createdAt),
                        Instant.fromEpochSeconds(lastUsedAt),
                    ),
                )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JdbiUsersRepository::class.java)
    }
}
