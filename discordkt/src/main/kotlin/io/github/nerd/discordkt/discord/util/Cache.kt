package io.github.nerd.discordkt.discord.util

import java.util.concurrent.ConcurrentHashMap

/**
 * @author ashley
 * @since 5/16/17 5:09 PM
 */
open class Cache<X, T : Cacheable<X>> internal constructor() {
	protected open val things: MutableMap<X, T> = ConcurrentHashMap()
	open val values
		get() = things.values.toList()

	operator fun get(id: X): T? = things[id]
	operator fun iterator() = things.values.iterator()

	internal fun cacheObj(t: T) {
		preCache(t)
		things.putIfAbsent(t.id, t)
		postCache(t)
	}

	internal fun purgeObj(t: T) = things.remove(t.id)
	internal fun purgeObj(id: X) = things.remove(id)
	internal fun updateObj(t: T) = things.put(t.id, t)
	internal fun cacheAll(t: List<T>) = t.forEach { this + it }
	internal fun clearCache() = things.clear()
	internal open fun preCache(t: T) {}
	internal open fun postCache(t: T) {}
	internal operator fun plus(t: T) = this.cacheObj(t)
	internal operator fun minus(t: T) = this.purgeObj(t)
	internal operator fun minus(id: X) = this.purgeObj(id)

	fun has(id: X) = things.containsKey(id)
	fun has(t: T) = has(t.id)
	fun forEach(function: (T) -> Unit) = values.forEach(function)
	fun filter(function: (T) -> Boolean) = values.filter(function)
	fun firstOrNull() = values.firstOrNull()
}