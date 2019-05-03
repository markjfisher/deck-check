package io.github.nerd.discordkt.discord.util

import com.fasterxml.jackson.annotation.JsonValue

/**
 * @author ashley
 * @since 5/26/17 9:42 PM
 */
enum class EmojiCategories {
	PEOPLE,
	NATURE,
	FOOD,
	ACTIVITY,
	TRAVEL,
	OBJECTS,
	SYMBOLS,
	FLAGS,
	CUSTOM;

	@JsonValue private val jsonVal = this.name.toLowerCase()
}