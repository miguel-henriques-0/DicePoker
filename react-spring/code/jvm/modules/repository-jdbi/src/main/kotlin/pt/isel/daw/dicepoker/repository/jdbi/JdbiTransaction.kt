package pt.isel.daw.dicepoker.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.isel.daw.dicepoker.repository.GamesRepository
import pt.isel.daw.dicepoker.repository.Transaction
import pt.isel.daw.dicepoker.repository.UsersRepository

class JdbiTransaction(
    private val handle: Handle,
) : Transaction {
    override val usersRepository: UsersRepository = JdbiUsersRepository(handle)
    override val gamesRepository: GamesRepository = JdbiGamesRepository(handle)

    override fun rollback() {
        handle.rollback()
    }
}
