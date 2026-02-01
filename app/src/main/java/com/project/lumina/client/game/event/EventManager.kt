package com.project.lumina.client.game.event


class EventManager {

    private val registry = mutableMapOf<Class<out GameEvent>, ArrayList<EventHook<in GameEvent>>>()

	@Suppress("unchecked_cast")
    fun register(hook: EventHook<out GameEvent>) {
        val handlers = registry.computeIfAbsent(hook.eventClass) { ArrayList() }

		handlers.add(hook as EventHook<in GameEvent>)
    }

	/**
	 * @return true if the handler has been successfully removed
	 */
	fun removeHandler(hook: EventHook<out GameEvent>): Boolean {
		val handlers = registry[hook.eventClass] ?: return false

		return handlers.remove(hook)
	}





	@Suppress("unchecked_cast")
    inline fun <reified T : GameEvent> listenNoCondition(noinline handler: Handler<T>) {
        register(EventHook(T::class.java, handler) as EventHook<in GameEvent>)
    }

    fun emit(event: GameEvent) {
        for (handler in (registry[event.javaClass] ?: return)) {
            try {
				if (handler.condition(event)) {
					handler.handler(event)
				}
            } catch (_: Throwable) {
            }
        }
    }
}
