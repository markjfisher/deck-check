package io.github.nerd.discordkt.discord.util

import com.fasterxml.jackson.annotation.JsonValue

/**
 * @author ashley
 * @since 5/14/17 11:09 PM
 */
enum class DiscordOpCodes(val code: Byte) {
	UNKNOWN(-1),
	DISPATCH(0),
	HEARTBEAT(1),
	IDENTIFY(2),
	STATUS_UPDATE(3),
	VOICE_STATUS_UPDATE(4),
	VOICE_SERVER_PING(5),
	RESUME(6),
	RECONNECT(7),
	REQUEST_GUILD_MEMBERS(8),
	INVALID_SESSION(9),
	HELLO(10),
	HEARTBEAT_ACK(11);

	companion object {
		fun getOpCodeByCode(code: Byte): DiscordOpCodes {
			return values().firstOrNull { it.code == code }
					?: UNKNOWN
		}
	}

	@JsonValue private val jsonValue = code
}