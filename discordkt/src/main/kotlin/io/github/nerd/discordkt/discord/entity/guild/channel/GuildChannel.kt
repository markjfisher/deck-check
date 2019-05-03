package io.github.nerd.discordkt.discord.entity.guild.channel

import io.github.nerd.discordkt.discord.entity.channel.Channel
import io.github.nerd.discordkt.discord.entity.guild.Guild

/**
 * @author ashley
 * @since 5/25/17 7:11 PM
 */
interface GuildChannel : Channel {
	var name: String
	var position: Short
	val guild: Guild
	val permissions: List<PermissionOverwrite<*>>
	var parent: GuildChannel?
}