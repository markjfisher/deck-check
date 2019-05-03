package io.github.nerd.discordkt.discord.util

import com.fasterxml.jackson.annotation.JsonValue

/**
 * @author ashley
 * @since 5/26/17 10:49 PM
 */
enum class Status(val title: String) {
	ONLINE("online"),
	IDLE("idle"),
	DO_NOT_DISTURB("dnd"),
	INVISIBLE("invisible"),
	OFFLINE("invisible");

	@JsonValue private val jsonVal = title

	companion object {
		fun fromText(string: String): Status {
			return values().firstOrNull { it.title.contentEquals(string.toLowerCase()) } ?: OFFLINE
		}
	}
}