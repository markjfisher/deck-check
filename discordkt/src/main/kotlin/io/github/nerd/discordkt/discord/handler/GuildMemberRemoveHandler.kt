package io.github.nerd.discordkt.discord.handler

import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.handler.base.WebSocketResponseHandler
import io.github.nerd.discordkt.discord.model.base.Model
import io.github.nerd.discordkt.discord.model.entity.ModelUser
import io.github.nerd.discordkt.discordLogger

internal class GuildMemberRemoveHandler : WebSocketResponseHandler<GuildMemberRemoveModel>("GUILD_MEMBER_REMOVE",
		GuildMemberRemoveModel::class.java) {
	override fun handle(data: GuildMemberRemoveModel, discord: Discord) {
		discord.guilds[data.guildID]?.let {
			it.members - data.user.id
			discordLogger.debug("User \"{}\" has been removed from guild \"{}\"", "${data.user.username}#${data.user.discriminator}")
		}
	}
}

internal class GuildMemberRemoveModel(val guildID: Long, val user: ModelUser) : Model