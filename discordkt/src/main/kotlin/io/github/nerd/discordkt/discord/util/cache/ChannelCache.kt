package io.github.nerd.discordkt.discord.util.cache

import io.github.nerd.discordkt.discord.entity.channel.Channel
import io.github.nerd.discordkt.discord.entity.channel.DirectPrivateChannel
import io.github.nerd.discordkt.discord.entity.channel.GroupPrivateChannel
import io.github.nerd.discordkt.discord.entity.channel.TextChannel
import io.github.nerd.discordkt.discord.entity.guild.channel.GuildTextChannel
import io.github.nerd.discordkt.discord.util.IdentifiableCache
import io.github.nerd.discordkt.discordLogger

/**
 * @author ashley
 * @since 5/16/17 8:41 PM
 */
open class ChannelCache<T : Channel> internal constructor() : IdentifiableCache<T>() {
	fun getTextChannelByID(id: Long) = things.values.filterIsInstance<TextChannel>().firstOrNull { it.id == id }

	override fun postCache(t: T) {
		if (t is TextChannel) {
			try {
				t.messages.fetchMessages().thenAccept {
					t.messages.cacheAll(it)
				}
			} catch (e: Exception) {
				when (t) {
					is GuildTextChannel -> discordLogger.warn("Failed to fetch message history in channel \"{}\" in guild \"{}\"", t.name, t.guild.name)
					is DirectPrivateChannel -> discordLogger.warn("Failed to fetch message history in private chat with \"{}\"", t.recipient.fullName)
					is GroupPrivateChannel -> discordLogger.warn("Failed to fetch message history in private chat with \"{}\"", t.recipients.joinToString { it.fullName })
					else -> discordLogger.warn("Failed to fetch message history in channel with ID {}", t.id)
				}
			}
		}
	}
}