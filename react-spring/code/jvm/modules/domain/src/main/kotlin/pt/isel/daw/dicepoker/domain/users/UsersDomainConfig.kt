package pt.isel.daw.dicepoker.domain.users

import kotlin.time.Duration

data class UsersDomainConfig(
    val tokenSizeInBytes: Int,
    val tokenTtl: Duration,
    val tokenRollingTtl: Duration,
    val maxTokensPerUser: Int,
    val maxInvitesPerUser: Int,
) {
    init {
        require(tokenSizeInBytes > 0)
        require(tokenTtl.isPositive())
        require(tokenRollingTtl.isPositive())
        require(maxTokensPerUser > 0)
        require(maxInvitesPerUser > 0)
    }
}
