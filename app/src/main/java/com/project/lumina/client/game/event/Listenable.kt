package com.project.lumina.client.game.event

interface Listenable {

    /**
     * Children listenables
     */
    val children: List<Listenable>
        get() = emptyList()

	val eventManager: EventManager

    /**
     * whether handle events or not
     */
	val handleEvents: Boolean
		get() = true

}

@Suppress("unchecked_cast")
inline fun <reified T : GameEvent> Listenable.handle(noinline handler: Handler<T>) {
	eventManager.register(EventHook(T::class.java, handler, this) as EventHook<in GameEvent>)
}
