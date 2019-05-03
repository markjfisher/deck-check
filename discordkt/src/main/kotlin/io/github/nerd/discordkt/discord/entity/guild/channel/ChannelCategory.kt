package io.github.nerd.discordkt.discord.entity.guild.channel

import io.github.nerd.discordkt.discord.entity.guild.Guild

class ChannelCategory(override val id: Long,
                      override var name: String,
                      override var position: Short,
                      override val guild: Guild,
                      override val permissions: List<PermissionOverwrite<*>>) : GuildChannel {
	override var parent: GuildChannel? = null // Categories cannot have parents
		set(value) {
			field = null
		} // Use strong arm tactics to ensure that this field is null

	val channels = guild.channels.filter { it !is ChannelCategory && it.parent == this }
}