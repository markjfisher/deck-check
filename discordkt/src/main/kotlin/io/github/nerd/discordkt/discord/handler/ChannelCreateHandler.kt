package io.github.nerd.discordkt.discord.handler

import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.handler.base.WebSocketResponseHandler
import io.github.nerd.discordkt.discord.model.channel.ModelChannel
import io.github.nerd.discordkt.discord.model.channel.ModelGuildChannel
import io.github.nerd.discordkt.discord.model.channel.ModelGuildTextChannel
import io.github.nerd.discordkt.discordLogger

/**
 * @author ashley
 * @since 5/27/17 11:10 PM
 */
internal class ChannelCreateHandler : WebSocketResponseHandler<ModelChannel>("CHANNEL_CREATE", handledClass = ModelChannel::class.java) {
	override fun handle(data: ModelChannel, discord: Discord) {
		if (data is ModelGuildChannel) {
			if (discord.guilds.has(data.guildID)) {
				val guild = discord.guilds[data.guildID]!!

				if (data is ModelGuildTextChannel) {
					val chan = data.toEntity(guild, discord)
					chan?.let {
						discordLogger.debug("Channel #{} created in guild \"{}\"", chan.name, chan.guild.name)
						discord.channels + chan
						guild.channels + chan
					}
				}
			}
		} else {

		}
	}
}