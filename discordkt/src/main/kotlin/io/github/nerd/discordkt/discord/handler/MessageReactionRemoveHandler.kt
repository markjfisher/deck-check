package io.github.nerd.discordkt.discord.handler

import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.entity.channel.TextChannel
import io.github.nerd.discordkt.discord.entity.emoji.Emoji
import io.github.nerd.discordkt.discord.entity.emoji.Reaction
import io.github.nerd.discordkt.discord.events.ReactionRemoveEvent
import io.github.nerd.discordkt.discord.handler.base.WebSocketResponseHandler
import io.github.nerd.discordkt.discord.model.response.ReactionEventDataModel
import io.github.nerd.discordkt.discordLogger

/**
 * @author ashley
 * @since 6/1/17 6:32 PM
 */
internal class MessageReactionRemoveHandler : WebSocketResponseHandler<ReactionEventDataModel>("MESSAGE_REACTION_REMOVE",
		ReactionEventDataModel::class.java) {
	override fun handle(data: ReactionEventDataModel, discord: Discord) {
		if (discord.channels.has(data.channelID) && discord.users.has(data.userID)) {
			val user = discord.users[data.userID]!!
			val chan = discord.channels[data.channelID]!!
			if (chan is TextChannel && chan.messages.has(data.messageID)) {
				val message = chan.messages[data.messageID]!!
				val emoji: Emoji
				if (data.emoji.id == null && discord.emojis.has(data.emoji.name)) {
					emoji = discord.emojis[data.emoji.name]!!
				} else if (data.emoji.id != null && discord.emojis.has(data.emoji.id)) {
					emoji = discord.emojis[data.emoji.id]!!
				} else {
					discordLogger.debug("{} un-reacted to message {} with an unknown emoji", user.fullName, message.id)
					return
				}

				discordLogger.debug("{} un-reacted to message {} with emoji {}", user.fullName, message.id, emoji.names[0])
				val reaction = Reaction(message, emoji, user)
				message.reactions - reaction
				discord.events.dispatch(ReactionRemoveEvent(reaction))
			}
		}
	}
}