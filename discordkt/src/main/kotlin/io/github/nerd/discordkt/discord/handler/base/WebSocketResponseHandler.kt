package io.github.nerd.discordkt.discord.handler.base

import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.model.base.Model
import io.github.nerd.discordkt.discord.model.response.base.BasicWebSocketResponse
import io.github.nerd.discordkt.discord.util.Cacheable
import io.github.nerd.discordkt.discord.util.DiscordOpCodes

/**
 * @author ashley
 * @since 5/15/17 11:45 AM
 */
@Suppress("UNCHECKED_CAST")
internal abstract class WebSocketResponseHandler<in T : Model>(val responseType: String?,
                                                               val handledClass: Class<in T>,
                                                               val opCode: DiscordOpCodes = DiscordOpCodes.DISPATCH) : Cacheable<Int> {

	override val id = (responseType ?: opCode.name).hashCode() + handledClass.simpleName.hashCode()

	inline fun <reified X> handle(data: X, discord: Discord) = handle(data as T, discord)

	protected abstract fun handle(data: T, discord: Discord)

	open fun doesHandle(response: BasicWebSocketResponse, discord: Discord) = response.type.equals(responseType)
			&& response.opCode == opCode.code
}

internal inline fun <T : Model> webSocketHandler(model: Class<T>,
                                                 responseType: String?,
                                                 opCode: DiscordOpCodes = DiscordOpCodes.DISPATCH,
                                                 crossinline handle: (data: T, discord: Discord) -> Unit) =
		object : WebSocketResponseHandler<T>(responseType, model, opCode) {
			override fun handle(data: T, discord: Discord) = handle(data, discord)
		}
