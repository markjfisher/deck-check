package io.github.nerd.discordkt.discord.entity.emoji

import io.github.nerd.discordkt.discord.util.EmojiCategories
import io.github.nerd.discordkt.discord.util.SkinTone

/**
 * @author ashley
 * @since 5/26/17 9:52 PM
 *
 * A global emoji that doesn't belong to a server
 */
class DefaultEmoji(names: Array<String>,
                   val category: EmojiCategories,
                   val unicode: String,
                   val isDiverse: Boolean,
                   val skinTone: SkinTone = SkinTone.NONE) : Emoji(if (names[0].hashCode().toLong() < 0) names[0].hashCode().toLong() else -names[0].hashCode().toLong(), names, category) {
	/**
	 * Returns a String object that, when sent, will show this emoji
	 */
	override val mention = unicode + skinTone

	/**
	 * Applies a given skin tone to an emoji
	 */
	override fun withSkinTone(skinTone: SkinTone) = DefaultEmoji(names, category, unicode, isDiverse, skinTone)
}