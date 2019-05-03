package io.github.nerd.discordkt.discord.handler

import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.entity.channel.TextChannel
import io.github.nerd.discordkt.discord.events.MessageUpdateEvent
import io.github.nerd.discordkt.discord.handler.base.WebSocketResponseHandler
import io.github.nerd.discordkt.discord.model.entity.ModelMessage

/**
 * @author ashley
 * @since 5/27/17 8:14 PM
 */
internal class MessageUpdateHandler : WebSocketResponseHandler<ModelMessage>("MESSAGE_UPDATE", handledClass = ModelMessage::class.java) {
	override fun handle(data: ModelMessage, discord: Discord) {
		if (data.editedTime != null && discord.channels.has(data.channelID)) {
			val chan = discord.channels[data.channelID]!!
			if (chan is TextChannel && chan.messages.has(data.id)) {
				val newMessage = data.toEntity(discord)
				val oldMessage = chan.messages[data.id]!!
				chan.messages.updateObj(newMessage)
				discord.events.dispatch(MessageUpdateEvent(oldMessage, newMessage))
			}
		}
	}
}