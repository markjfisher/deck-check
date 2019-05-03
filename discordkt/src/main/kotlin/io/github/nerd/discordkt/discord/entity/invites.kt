package io.github.nerd.discordkt.discord.entity

import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.auth.AuthType
import io.github.nerd.discordkt.discord.entity.base.Entity
import io.github.nerd.discordkt.discord.entity.user.User
import io.github.nerd.discordkt.discord.exception.ensureThat

/**
 * @author ashley
 * @since 7/16/17 3:03 PM
 */
class Invite internal constructor(val code: String, val guild: InviteGuild,
                                  val channel: InviteChannel, val inviter: User?, private val discord: Discord) : Entity {
	fun accept() {
		ensureThat(discord.auth.type == AuthType.USER, "Only user accounts can accept invites.")
		discord.api.acceptInvite(code)
	}
}

data class InviteGuild internal constructor(val id: Long, val name: String, val splash: String?, val icon: String?)
data class InviteChannel internal constructor(val id: Long, val name: String, val type: InviteChannelType)

enum class InviteChannelType {
	TEXT,
	VOICE
}