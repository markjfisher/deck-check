package io.github.nerd.discordkt.discord.handler

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.events.NicknameChangeEvent
import io.github.nerd.discordkt.discord.events.RoleUpdateEvent
import io.github.nerd.discordkt.discord.handler.base.WebSocketResponseHandler
import io.github.nerd.discordkt.discord.model.base.Model
import io.github.nerd.discordkt.discord.model.entity.ModelUser
import io.github.nerd.discordkt.discordLogger

/**
 * @author ashley
 * @since 5/30/17 11:23 PM
 */
internal class GuildMemberUpdateHandler : WebSocketResponseHandler<GuildMemberUpdateHandler.GuildMemberUpdateModel>("GUILD_MEMBER_UPDATE", GuildMemberUpdateModel::class.java) {
	override fun handle(data: GuildMemberUpdateModel, discord: Discord) {
		if (discord.guilds.has(data.guildID)) {
			val guild = discord.guilds[data.guildID]!!
			if (guild.members.has(data.user.id)) {
				val member = guild.members[data.user.id]!!
				val oldRoles = member.roles.values.toTypedArray()
				if (!data.roles.isEmpty()) {
					member.roles.clearCache()
					data.roles.forEach {
						if (guild.roles.has(it)) {
							member.roles + guild.roles[it]!!
						}
					}
				}
				val newRoles = member.roles.values.toTypedArray()
				if (!oldRoles.contentDeepEquals(newRoles)) {
					discord.events.dispatch(RoleUpdateEvent(oldRoles, newRoles, member))
				}

				if (member.realActualNicknameValueThanks != data.nickname) {
					val oldNickname = member.realActualNicknameValueThanks
					member.realActualNicknameValueThanks = data.nickname
					discordLogger.debug("\"{}\" changed their nickname in guild \"{}\" from \"{}\" to \"{}\"", member.user.fullName,
							guild.name, oldNickname ?: member.user.username, data.nickname ?: member.user.username)
					discord.events.dispatch(NicknameChangeEvent(oldNickname, data.nickname, member))
				}
			}
		}
	}

	class GuildMemberUpdateModel(@JsonProperty("user") val user: ModelUser,
	                             @JsonProperty("roles") val roles: Array<Long>,
	                             @JsonProperty("nick") val nickname: String?,
	                             @JsonProperty("guild_id") val guildID: Long) : Model
}