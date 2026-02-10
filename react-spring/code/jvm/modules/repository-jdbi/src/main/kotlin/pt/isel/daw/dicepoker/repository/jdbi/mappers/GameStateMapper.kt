package pt.isel.daw.dicepoker.repository.jdbi.mappers

import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import org.postgresql.util.PGobject
import pt.isel.daw.dicepoker.domain.games.GameState
import pt.isel.daw.dicepoker.repository.jdbi.JdbiGamesRepository
import java.sql.ResultSet
import java.sql.SQLException

class GameStateMapper : ColumnMapper<GameState> {
    @Throws(SQLException::class)
    override fun map(
        r: ResultSet,
        columnNumber: Int,
        ctx: StatementContext?,
    ): GameState {
        val stateTypeColumnIndex = r.findColumn("game_state_type")
        val stateType = r.getString(stateTypeColumnIndex)

        val obj = r.getObject(columnNumber, PGobject::class.java)
        return JdbiGamesRepository.deserializeGameStateFromJson(
            obj.value ?: throw IllegalArgumentException("Error occurred while parsing json"),
            stateType,
        )
    }
}
