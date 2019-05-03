package io.github.nerd.discordkt.discord.util.cache

import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.entity.Message
import io.github.nerd.discordkt.discord.entity.emoji.Emoji
import io.github.nerd.discordkt.discord.entity.emoji.Reaction
import io.github.nerd.discordkt.discord.entity.user.User
import io.github.nerd.discordkt.discord.util.Cache

/**
 * @author ashley
 * @since 6/1/17 3:02 AM
 */
class ReactionCache(private val discord: Discord, private val message: Message) : Cache<Long, Reaction>() {
	operator fun get(user: User) = filter { it.user == user }
	operator fun get(emoji: Emoji) = filter { it.emoji == emoji }

	fun clear() = message.clearReactions()
}