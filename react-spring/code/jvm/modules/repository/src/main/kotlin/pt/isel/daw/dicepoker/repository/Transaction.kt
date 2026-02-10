package pt.isel.daw.dicepoker.repository

interface Transaction {
    val usersRepository: UsersRepository

//    val gamesRepository: GamesRepository
    val gamesRepository: GamesRepository

    // other repository types
    fun rollback()
}
