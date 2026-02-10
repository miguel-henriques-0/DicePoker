package pt.isel.daw.dicepoker

import kotlinx.datetime.Clock
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import pt.isel.daw.dicepoker.domain.games.GameDomainConfig
import pt.isel.daw.dicepoker.domain.users.Sha256TokenEncoder
import pt.isel.daw.dicepoker.domain.users.UsersDomainConfig
import pt.isel.daw.dicepoker.http.pipeline.AuthenticatedUserArgumentResolver
import pt.isel.daw.dicepoker.http.pipeline.AuthenticationInterceptor
import pt.isel.daw.dicepoker.repository.jdbi.configureWithAppRequirements
import kotlin.time.Duration.Companion.hours

@SpringBootApplication
class ChelasDicePokerApplication {
    @Bean
    fun jdbi() =
        Jdbi.create(
            PGSimpleDataSource().apply {
                setURL(Environment.getDbUrl())
            },
        ).configureWithAppRequirements()

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun tokenEncoder() = Sha256TokenEncoder()

    @Bean
    fun clock() = Clock.System

    @Bean
    fun usersDomainConfig() =
        UsersDomainConfig(
            tokenSizeInBytes = 256 / 8,
            tokenTtl = 24.hours,
            tokenRollingTtl = 1.hours,
            maxTokensPerUser = 3,
            maxInvitesPerUser = 1,
        )

    @Bean
    fun gamesDomainConfig() =
        GameDomainConfig(
            minPlayers = 2,
            maxPlayers = 5,
            timeout = 60,
            maxConcurrentGames = 1,
            maxRollsPerTurn = 3,
            turnTimer = 180,
            handSize = 5,
            initialBalance = 20,
        )
}

@Configuration
class PipelineConfigurer(
    val authenticationInterceptor: AuthenticationInterceptor,
    val authenticatedUserArgumentResolver: AuthenticatedUserArgumentResolver,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authenticationInterceptor)
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authenticatedUserArgumentResolver)
    }
}

private val logger = LoggerFactory.getLogger("main")

fun main(args: Array<String>) {
    logger.info("Starting app")
    runApplication<ChelasDicePokerApplication>(*args)
}
