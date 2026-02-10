package pt.isel.daw.dicepoker.services

import jakarta.inject.Named
import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import pt.isel.daw.dicepoker.domain.Event
import pt.isel.daw.dicepoker.domain.EventEmitter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

@Named
class EventService: NeedsShutdown {
    // Concurrent Hash map guarantees that the last write is always read so no race issues
    private val emitters = ConcurrentHashMap<Int, CopyOnWriteArrayList<EventEmitter>>()
    // Atomic -> has a get and set thats atomic and guarantees safe access (check then act safe)
    private val currentId = AtomicLong(0)
    private val closing = AtomicBoolean(false)

    private val scheduler: ScheduledExecutorService =
        Executors.newScheduledThreadPool(1).also {
            it.scheduleAtFixedRate({ keepAlive() }, 2, 2, TimeUnit.SECONDS)
        }

    override fun shutdown() {
        logger.info("shutting down")
        closing.set(true)
        val id = currentId.incrementAndGet()
        sendEventToAll(Event.Message(id, "bye"))
        emitters.values.flatten().forEach { emitter ->
            emitter.onCompletion { closing.get() }
        }
        scheduler.shutdown()
    }

    fun addEventEmitter(emitter: EventEmitter, gameId: Int) {
        logger.info("adding emitter for game $gameId")
        val list = emitters.computeIfAbsent(gameId) { CopyOnWriteArrayList() }
        list.add(emitter)

        emitter.onCompletion {
            logger.info("onCompletion")
            removeEmitter(emitter, gameId)
        }
        emitter.onError {
            logger.info("onError")
        }
    }

    fun sendMessage(msg: String, gameId: Int) {
        logger.info("sendMessage to game $gameId")
        val id = currentId.incrementAndGet()
        sendEventToGame(gameId, Event.Message(id, msg))
    }

    private fun sendEventToGame(gameId: Int, event: Event) {
        emitters[gameId]?.forEach { emitter ->
            try {
                emitter.emit(event)
            } catch (ex: Exception) {
                logger.info("Exception while sending event - {}", ex.message)
            }
        }
    }

    private fun removeEmitter(emitter: EventEmitter, gameId: Int) {
        logger.info("removing emitter from game $gameId")
        emitters[gameId]?.remove(emitter)
        emitters.compute(gameId) { _, list ->
            if (list?.isEmpty() == true) null else list
        }
    }

    private fun keepAlive() {
        if (closing.get() || emitters.isEmpty()) {
            return
        }
        logger.info("keepAlive, sending to {} games", emitters.size)
        sendEventToAll(Event.KeepAlive(Clock.System.now()))
    }

    private fun sendEventToAll(event: Event) {
        emitters.values.flatten().forEach { emitter ->
            try {
                emitter.emit(event)
            } catch (ex: Exception) {
                logger.info("Exception while sending event - {}", ex.message)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(EventService::class.java)
    }
}

