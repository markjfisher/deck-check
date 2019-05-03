package io.github.nerd.discordkt.discord.handler

import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.entity.guild.Role
import io.github.nerd.discordkt.discord.handler.base.WebSocketResponseHandler
import io.github.nerd.discordkt.discord.model.response.GuildRoleResponseModel

/**
 * @author ashley
 * @since 6/7/17 5:42 AM
 */
internal class GuildRoleUpdateHandler : WebSocketResponseHandler<GuildRoleResponseModel>("GUILD_ROLE_UPDATE"
		, GuildRoleResponseModel::class.java) {
	override fun handle(data: GuildRoleResponseModel, discord: Discord) {
		if (discord.guilds.has(data.guildID)) {
			val guild = discord.guilds[data.guildID]!!
			val role = Role(data.role, guild)
			guild.roles.updateObj(role)
			//Events.dispatch(RoleCreateEvent(role))
		}
	}
}