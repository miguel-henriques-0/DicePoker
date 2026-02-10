package pt.isel.daw.dicepoker.domain

interface EventEmitter {
    fun emit(event: Event)

    fun onCompletion(callback: () -> Unit)

    fun onError(callback: (Throwable) -> Unit)
}
