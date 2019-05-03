package io.github.nerd.discordkt.discord.util.cache

import io.github.nerd.discordkt.discord.handler.base.WebSocketResponseHandler
import io.github.nerd.discordkt.discord.util.Cache

/**
 * @author ashley
 * @since 5/29/17 8:08 PM
 */
internal class WebSocketHandlerCache(vararg handlers: WebSocketResponseHandler<*>) : Cache<Int, WebSocketResponseHandler<*>>() {
	init {
		this.cacheAll(handlers.toList())
	}
}