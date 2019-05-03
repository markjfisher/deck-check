package io.github.nerd.discordkt.discord.exception

import io.github.nerd.discordkt.discord.entity.guild.Guild
import io.github.nerd.discordkt.discord.entity.guild.channel.GuildChannel
import io.github.nerd.discordkt.discord.util.Permissions

/**
 * @author ashley
 * @since 6/20/17 5:44 PM
 */
class EnsuredException(message: String = "", cause: Throwable? = null) : Exception(message, cause)

class InsufficientPermissionsException(permission: Permissions, guild: Guild, channel: GuildChannel? = null) : Exception(
		"Insufficient permissions (missing ${permission.name} in guild \"${guild.name}\"" +
				"${if (channel != null) " in channel #${channel.name}" else ""})")

/**
 * Ensures that the given condition is true, otherwise throws an exception with the given message
 */
internal fun ensureThat(condition: Boolean, message: String = "") {
	if (!condition)
		throw EnsuredException(message)
}

internal fun ensurePermission(permission: Permissions, guild: Guild, channel: GuildChannel? = null) {
	if (!guild.me.hasPermission(permission, channel)
			&& !guild.me.hasPermission(Permissions.ADMINISTRATOR, channel))
		throw InsufficientPermissionsException(permission, guild, channel)
}

internal fun Guild.ensurePermission(permission: Permissions, channel: GuildChannel? = null)
		= ensurePermission(permission, this, channel)