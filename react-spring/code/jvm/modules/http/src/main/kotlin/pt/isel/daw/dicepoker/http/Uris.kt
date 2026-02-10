package pt.isel.daw.dicepoker.http

import org.springframework.web.util.UriTemplate
import java.net.URI

object Uris {
    const val PREFIX = "/api"
    const val HOME = PREFIX

    fun home(): URI = URI(HOME)

    object Users {
        const val CREATE = "$PREFIX/users"
        const val TOKEN = "$PREFIX/users/token"
        const val LOGOUT = "$PREFIX/logout"
        const val GET_BY_ID = "$PREFIX/users/{id}"
        const val HOME = "$PREFIX/me"
        const val CREATE_INVITE = "$PREFIX/users/createInvite"

        fun byId(id: Int): URI = UriTemplate(GET_BY_ID).expand(id)

        fun home(): URI = URI(HOME)

        fun login(): URI = URI(TOKEN)

        fun register(): URI = URI(CREATE)
    }

    object Status {
        const val HOSTNAME = "$PREFIX/status/hostname"
        const val IP = "$PREFIX/status/ip"
    }

    object Game {
        const val CREATE = "$PREFIX/game/create"
        const val JOIN = "$PREFIX/game/{id}/join"
        const val GET_BY_ID = "$PREFIX/game/{id}"
        const val LIST = "$PREFIX/game/list"
        const val LEAVE = "$PREFIX/game/{id}/leave"
        const val START_GAME = "$PREFIX/game/{id}/start"
        const val PLAY = "$PREFIX/game/{id}/play"
        const val NEXT_ROUND = "$PREFIX/game/{id}/nextRound"
        const val NEXT_GAME_STATE = "$PREFIX/game/{id}/nextGameState"

        fun byId(id: Int): URI = UriTemplate(GET_BY_ID).expand(id)
    }

    object Events {
        const val LISTEN = "$PREFIX/events/listen/{id}"
        const val SEND = "$PREFIX/events/send/{id}"
    }


}
