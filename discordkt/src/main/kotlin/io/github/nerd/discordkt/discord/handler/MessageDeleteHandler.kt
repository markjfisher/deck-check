package io.github.nerd.discordkt.discord.handler

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.events.MessageDeleteEvent
import io.github.nerd.discordkt.discord.handler.base.WebSocketResponseHandler
import io.github.nerd.discordkt.discord.model.base.Model

/**
 * @author ashley
 * @since 5/27/17 5:47 AM
 */
internal class MessageDeleteHandler : WebSocketResponseHandler<MessageDeleteHandler.MessageDeleteModel>("MESSAGE_DELETE",
		MessageDeleteModel::class.java) {
	override fun handle(data: MessageDeleteModel, discord: Discord) {
		val chan = discord.channels.getTextChannelByID(data.channelID)
		if (null != chan) {
			val message = chan.messages - data.messageID
			if (null != message && message.author.id != discord.me.id) {
				discord.events.dispatch(MessageDeleteEvent(message))
			}
		}
	}

	internal class MessageDeleteModel(@JsonProperty("id") val messageID: Long,
	                                  @JsonProperty("channel_id") val channelID: Long) : Model
}