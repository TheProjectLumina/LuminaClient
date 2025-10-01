package com.project.lumina.client.game.event

typealias Handler<T> = (T) -> Unit

class EventHook<T : GameEvent> (
    val eventClass: Class<T>,
    val handler: Handler<T>,
    val condition: (T) -> Boolean = { true }
) {

    constructor(eventClass: Class<T>, handler: Handler<T>, listenable: Listenable)
            : this(eventClass, handler, { listenable.handleEvents })
}
