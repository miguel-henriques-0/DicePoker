package pt.isel.daw.dicepoker

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import pt.isel.daw.dicepoker.repository.Transaction
import pt.isel.daw.dicepoker.repository.TransactionManager
import pt.isel.daw.dicepoker.repository.jdbi.JdbiTransaction
import pt.isel.daw.dicepoker.repository.jdbi.configureWithAppRequirements

private val jdbi =
    Jdbi.create(
        PGSimpleDataSource().apply {
            setURL("jdbc:postgresql://localhost:5432/db?user=dbuser&password=changeit")
        },
    ).configureWithAppRequirements()

fun testWithHandleAndRollback(block: (Handle) -> Unit) =
    jdbi.useTransaction<Exception> { handle ->
        block(handle)
        handle.rollback()
    }

fun testWithTransactionManagerAndRollback(block: (TransactionManager) -> Unit) =
    jdbi.useTransaction<Exception> { handle ->

        val transaction = JdbiTransaction(handle)

        // a test TransactionManager that never commits
        val transactionManager =
            object : TransactionManager {
                override fun <R> run(block: (Transaction) -> R): R {
                    return block(transaction)
                    // n.b. no commit happens
                }
            }
        block(transactionManager)

        // finally, we rollback everything
        handle.rollback()
    }
