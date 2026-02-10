package pt.isel.daw.dicepoker.repository.jdbi.mappers

import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.daw.dicepoker.domain.users.PasswordValidationInfo
import java.sql.ResultSet
import java.sql.SQLException

class PasswordValidationInfoMapper : ColumnMapper<PasswordValidationInfo> {
    @Throws(SQLException::class)
    override fun map(
        r: ResultSet,
        columnNumber: Int,
        ctx: StatementContext?,
    ): PasswordValidationInfo = PasswordValidationInfo(r.getString(columnNumber))
}
