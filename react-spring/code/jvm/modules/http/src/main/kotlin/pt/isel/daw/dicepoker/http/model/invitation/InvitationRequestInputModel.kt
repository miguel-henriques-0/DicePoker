package pt.isel.daw.dicepoker.http.model.invitation

data class InvitationRequestInputModel(
    val inviteCode: Long,
    // User id that shared the code
    val sharedByUser: Int,
)
