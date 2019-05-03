@file:Suppress("UNCHECKED_CAST")

package io.github.nerd.discordkt.event

import java.util.concurrent.ConcurrentHashMap

/**
 * @author ashley
 * @since 5/15/17 4:44 PM
 */
class Events {
	private val listeners = ConcurrentHashMap<Class<*>, MutableList<EventSubscriber<*>>>()

	inline fun <reified T : Event> on(noinline exec: (event: T) -> Unit) =
			registerListener(T::class.java, EventSubscriber(exec))

	fun <T : Event> registerListener(type: Class<T>, subscriber: EventSubscriber<T>) {
		listeners.putIfAbsent(type, arrayListOf())
		listeners[type]?.add(subscriber)
	}

	fun <T : Event> dispatch(event: T) {
		listeners[event.javaClass]?.forEach { (it as EventSubscriber<T>).exec(event) }
	}

	internal fun isHandling(clazz: Class<*>) = listeners[clazz]?.isNotEmpty() ?: false
}

class EventSubscriber<in T>(val exec: (T) -> Unit)

