package io.github.nerd.discordkt.discord.entity.guild

import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.entity.base.Entity
import io.github.nerd.discordkt.discord.entity.base.Identifiable
import io.github.nerd.discordkt.discord.entity.guild.channel.GuildChannel
import io.github.nerd.discordkt.discord.entity.guild.channel.UserPermissionOverwrite
import io.github.nerd.discordkt.discord.entity.user.User
import io.github.nerd.discordkt.discord.exception.ensurePermission
import io.github.nerd.discordkt.discord.exception.ensureThat
import io.github.nerd.discordkt.discord.util.Permissions
import io.github.nerd.discordkt.discord.util.cache.RoleCache
import io.github.nerd.discordkt.discordLogger
import java.time.ZonedDateTime

/**
 * @author ashley
 * @since 5/25/17 6:57 PM
 */
class GuildMember internal constructor(val user: User,
                                       val guild: Guild,
                                       nickname: String?,
                                       mute: Boolean,
                                       deaf: Boolean,
                                       val joinDate: ZonedDateTime,
                                       private val discord: Discord) : Entity, Identifiable {
	/**
	 * Roles which this user has
	 */
	val roles = RoleCache()

	/**
	 * Whether or not this member is muted
	 */
	var mute = mute
		internal set // TODO ability to mute

	/**
	 * Whether or not this member is deafened
	 */
	var deaf = deaf
		internal set // TODO ability to deafen

	/**
	 * This member's nickname
	 */
	var nickname
		set(value) {
			if (this.user == discord.me) {
				this.guild.ensurePermission(Permissions.CHANGE_NICKNAME)
			} else {
				this.guild.ensurePermission(Permissions.MANAGE_NICKNAMES)
			}

			guild.changeNickname(value ?: "", this)
			this.realActualNicknameValueThanks = value
		}
		get() = realActualNicknameValueThanks

	internal var realActualNicknameValueThanks = nickname

	/**
	 * The ID of this member.
	 */
	override val id = user.id

	/**
	 * Bans this member, optionally with specified reason and how days prior worth of this members' messages will be deleted
	 */
	fun ban(reason: String = "", daysToClear: Int = 0) {
		ensureThat(this.id != discord.me.id, "You cannot ban yourself.")
		this.guild.ensurePermission(Permissions.BAN_MEMBERS)
		var actualDays = daysToClear
		if (actualDays > 7) {
			actualDays = 7
			discordLogger.warn("Attempt to clear more than 7 days of messages on ban. Capping to 7. (API limitation)")
		}

		discord.api.banUser(guild.id, id, actualDays, reason)
	}

	/**
	 * Kicks this member from the guild
	 */
	fun kick() {
		ensureThat(this.user != discord.me, "You cannot kick yourself")
		this.guild.ensurePermission(Permissions.KICK_MEMBERS)
		discord.api.kickMember(this.guild.id, this.user.id)
	}

	/**
	 * Whether or not this member has the specified permissions, optionally in the specified channel
	 */
	fun hasPermission(permission: Permissions, channel: GuildChannel? = null)
			= allPermissions(channel).contains(permission)

	/**
	 * Every permission this member has, optionally in the specified channel
	 */
	fun allPermissions(channel: GuildChannel? = null): List<Permissions> {
		val result = arrayListOf<Permissions>()
		roles.forEach { role ->
			result.addAll(role.permissions)
		}

		channel?.permissions?.sortedBy { it.type.ordinal }?.filter {
			if (it is UserPermissionOverwrite)
				it.entity == this
			else
				roles.has(it.id)
		}?.forEach {
			result.removeAll(it.denied)
			result.addAll(it.allowed)
		}
		return result.distinct()
	}
}