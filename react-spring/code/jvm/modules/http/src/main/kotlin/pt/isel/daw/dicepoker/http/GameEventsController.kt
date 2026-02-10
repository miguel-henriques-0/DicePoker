package pt.isel.daw.dicepoker.http

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import pt.isel.daw.dicepoker.services.EventService
import pt.isel.daw.dicepoker.services.GameService
import java.util.concurrent.TimeUnit
import pt.isel.daw.dicepoker.utils.Success
import org.springframework.web.bind.annotation.PathVariable


@RestController
class GameEventsController(
    private val gameService: GameService,
    private val eventService: EventService
) {
    @GetMapping(Uris.Events.LISTEN)
    fun listen(
        @PathVariable id: Int,
    ): SseEmitter {
        val sseEmitter = SseEmitter(TimeUnit.HOURS.toMillis(1))
        eventService.addEventEmitter(
            SseEmitterBasedEventEmitter(
                sseEmitter,
            ),
            id
        )
        return sseEmitter
    }

    @PostMapping(Uris.Events.SEND)
    fun send(
        @PathVariable id: Int,
        @RequestBody message: String,
    ) {
        logger.info("Received event: $message")

        when (val res = gameService.getGameById(id)) {
            is Success -> {
                eventService.sendMessage(res.value.toString(), id)
            }
            else -> {
                logger.error("Received invalid event: $message")
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GameEventsController::class.java)
    }
}

//https://bugsee.com/kotlin/kotlin-data-class-to-json/