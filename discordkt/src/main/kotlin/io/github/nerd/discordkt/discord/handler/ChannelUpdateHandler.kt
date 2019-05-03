package io.github.nerd.discordkt.discord.handler

import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.entity.guild.channel.GuildTextChannel
import io.github.nerd.discordkt.discord.events.ChannelNameChangeEvent
import io.github.nerd.discordkt.discord.handler.base.WebSocketResponseHandler
import io.github.nerd.discordkt.discord.model.channel.ModelGuildChannel
import io.github.nerd.discordkt.discordLogger

/**
 * @author ashley
 * @since 6/16/17 8:32 PM
 */
internal class ChannelUpdateHandler : WebSocketResponseHandler<ModelGuildChannel>("CHANNEL_UPDATE", ModelGuildChannel::class.java) {
	override fun handle(data: ModelGuildChannel, discord: Discord) {
		discord.guilds[data.guildID]?.let { guild ->
			val chan = guild.channels[data.id]
			chan?.let {
				if (chan is GuildTextChannel) {
					chan.guildChannelImpl._name = ""
				}

				if (data.parentID != null) {
					guild.channels[data.parentID]?.let {
						chan.parent = it
					}
				}

				if (data.name != chan.name) {
					val oldName = chan.name

					discord.events.dispatch(ChannelNameChangeEvent(oldName, data.name, chan))
				}

				discordLogger.debug("Channel \"${chan.name}\" in guild \"${chan.guild.name}\" was updated.")
			}
		}
	}
}