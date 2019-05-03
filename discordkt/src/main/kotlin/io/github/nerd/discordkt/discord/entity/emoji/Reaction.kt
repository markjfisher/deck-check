package io.github.nerd.discordkt.discord.entity.emoji

import io.github.nerd.discordkt.discord.entity.Message
import io.github.nerd.discordkt.discord.entity.user.User
import io.github.nerd.discordkt.discord.util.Cacheable

/**
 * @author ashley
 * @since 6/1/17 2:58 AM
 */
class Reaction internal constructor(val message: Message, val emoji: Emoji, val user: User) : Cacheable<Long> {
	override val id = emoji.id.hashCode().toLong() + user.id.hashCode() + message.id.hashCode()

	fun delete() = message.deleteReaction(emoji, user)
}