package pt.isel.daw.dicepoker.repository.jdbi

import jakarta.inject.Named
import org.jdbi.v3.core.Jdbi
import org.springframework.stereotype.Component
import pt.isel.daw.dicepoker.repository.Transaction
import pt.isel.daw.dicepoker.repository.TransactionManager

@Component
@Named
class JdbiTransactionManager(
    private val jdbi: Jdbi,
) : TransactionManager {
    override fun <R> run(block: (Transaction) -> R): R =
        jdbi.inTransaction<R, Exception> { handle ->
            val transaction = JdbiTransaction(handle)
            block(transaction)
        }
}
