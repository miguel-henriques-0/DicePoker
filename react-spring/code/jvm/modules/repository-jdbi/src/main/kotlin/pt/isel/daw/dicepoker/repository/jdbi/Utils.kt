package pt.isel.daw.dicepoker.repository.jdbi

import kotlinx.datetime.Instant
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import pt.isel.daw.dicepoker.domain.users.PasswordValidationInfo
import pt.isel.daw.dicepoker.domain.users.TokenValidationInfo
import pt.isel.daw.dicepoker.repository.jdbi.mappers.GameStateMapper
import pt.isel.daw.dicepoker.repository.jdbi.mappers.InstantMapper
import pt.isel.daw.dicepoker.repository.jdbi.mappers.PasswordValidationInfoMapper
import pt.isel.daw.dicepoker.repository.jdbi.mappers.TokenValidationInfoMapper

fun Jdbi.configureWithAppRequirements(): Jdbi {
    installPlugin(KotlinPlugin())
    installPlugin(PostgresPlugin())

    registerColumnMapper(PasswordValidationInfo::class.java, PasswordValidationInfoMapper())
    registerColumnMapper(TokenValidationInfo::class.java, TokenValidationInfoMapper())
    registerColumnMapper(Instant::class.java, InstantMapper())
    registerColumnMapper(GameStateMapper())
    return this
}
