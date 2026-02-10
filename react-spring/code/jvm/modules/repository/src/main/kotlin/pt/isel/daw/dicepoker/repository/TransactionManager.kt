package pt.isel.daw.dicepoker.repository

interface TransactionManager {
    fun <R> run(block: (Transaction) -> R): R
}
