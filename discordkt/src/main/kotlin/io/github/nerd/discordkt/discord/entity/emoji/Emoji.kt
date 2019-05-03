package io.github.nerd.discordkt.discord.entity.emoji

import io.github.nerd.discordkt.discord.entity.base.Entity
import io.github.nerd.discordkt.discord.entity.base.Identifiable
import io.github.nerd.discordkt.discord.entity.base.Mentionable
import io.github.nerd.discordkt.discord.util.EmojiCategories
import io.github.nerd.discordkt.discord.util.SkinTone
import java.util.*

/**
 * @author ashley
 * @since 5/24/17 10:13 PM
 */
abstract class Emoji internal constructor(override val id: Long,
                                          val names: Array<String>,
                                          val categories: EmojiCategories) : Entity, Identifiable, Mentionable {
	override fun toString() = mention

	abstract fun withSkinTone(skinTone: SkinTone): Emoji

	override fun equals(other: Any?): Boolean {
		if (other !is Emoji)
			return false
		return other.mention == this.mention
	}

	override fun hashCode(): Int {
		var result = id.hashCode()
		result = 31 * result + Arrays.hashCode(names)
		result = 31 * result + categories.hashCode()
		return result
	}
}