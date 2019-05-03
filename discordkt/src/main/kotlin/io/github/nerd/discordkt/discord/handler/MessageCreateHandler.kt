package io.github.nerd.discordkt.discord.handler

import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.events.MessageRecvEvent
import io.github.nerd.discordkt.discord.handler.base.WebSocketResponseHandler
import io.github.nerd.discordkt.discord.model.entity.ModelMessage
import io.github.nerd.discordkt.discordLogger

/**
 * @author ashley
 * @since 5/16/17 1:44 PM
 */
internal class MessageCreateHandler : WebSocketResponseHandler<ModelMessage>("MESSAGE_CREATE", handledClass = ModelMessage::class.java) {
	override fun handle(data: ModelMessage, discord: Discord) {
		if (data.author != null) {
			val message = data.toEntity(discord)
			message.channel?.let {
				it.messages + message
				if (data.author.id != discord.me.id) {
					discordLogger.debug("Message received from \"{}#{}\" with content \"{}\"", data.author.username,
							data.author.discriminator, data.content)
					discord.events.dispatch(MessageRecvEvent(message))
				}
			}
		}
	}
}