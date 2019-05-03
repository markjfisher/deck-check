package io.github.nerd.discordkt.discord.util

import java.util.*

/**
 * @author ashley
 * @since 4/27/17 9:45 PM
 * Definitions of various skin tones for applicable emojis
 */
enum class SkinTone constructor(unicode: String,
                                /**
                                 * gets the alias of this skin tone
                                 * @return alias, in the format of skin-tone-n
                                 */
                                val alias: String) {
	LIGHT_SKIN("\uDFFB", "skin-tone-1"),
	MEDIUM_LIGHT_SKIN("\uDFFC", "skin-tone-2"),
	MEDIUM_SKIN("\uDFFD", "skin-tone-3"),
	MEDIUM_DARK_SKIN("\uDFFE", "skin-tone-4"),
	DARK_SKIN("\uDFFF", "skin-tone-5"),

	TYPE_1("\uDFFB", "skin-tone-1"),
	TYPE_2("\uDFFB", "skin-tone-1"),
	TYPE_3("\uDFFC", "skin-tone-2"),
	TYPE_4("\uDFFD", "skin-tone-3"),
	TYPE_5("\uDFFE", "skin-tone-4"),
	TYPE_6("\uDFFF", "skin-tone-5"),

	NONE("", "");

	/**
	 * gets the unicode representation of this skin tone
	 * @return unicode representation of this skin tone
	 */
	val unicode: String = if (unicode.isNotEmpty()) "\uD83C" + unicode else unicode

	/**
	 * @return unicode representation of this skin tone
	 */
	override fun toString(): String {
		return this.unicode
	}

	companion object {

		/**
		 * A pattern that matches the unicode of a fitzpatrick character
		 */
		val skinTonePattern = Regex("[\\uD83C\\uDFFB-\\uD83C\\uDFFF]")

		/**
		 * A pattern that matches skin-tone-n
		 */
		val skinToneAliasPattern = Regex("skin-tone-[1-5]")

		/**
		 * @param unicode a unicode string
		 *
		 * @return a skin tone that matches the given unicode string, or NONE
		 */
		fun fromUnicode(unicode: String): SkinTone {
			if (!skinTonePattern.containsMatchIn(unicode))
				return NONE
			return values().firstOrNull { it.unicode.equals(skinTonePattern.find(unicode)?.value, ignoreCase = true) } ?: NONE
		}

		/**
		 * @param alias an alias, matching skin-tone-n
		 * *
		 * @return a skin tone matching the given alias, or NONE
		 */
		fun fromAlias(alias: String): SkinTone {
			return Arrays.stream(SkinTone.values())
					.filter { it.alias.equals(alias, ignoreCase = true) }
					.findFirst().orElse(NONE)
		}
	}
}
