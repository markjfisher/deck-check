package io.github.nerd.discordkt.discord.entity.guild.channel

import io.github.nerd.discordkt.discord.entity.guild.Guild
import io.github.nerd.discordkt.discord.exception.ensurePermission
import io.github.nerd.discordkt.discord.util.Permissions

internal open class GuildChannelImpl(override val id: Long,
                                     name: String,
                                     override var position: Short,
                                     override val guild: Guild,
                                     override val permissions: List<PermissionOverwrite<*>>) : GuildChannel {
	override var parent: GuildChannel? = null // todo


	internal var _name: String = name
	override var name: String
		set(value) {
			ensurePermission(Permissions.MANAGE_CHANNELS, guild)
			// todo
		}
		get() = _name
}