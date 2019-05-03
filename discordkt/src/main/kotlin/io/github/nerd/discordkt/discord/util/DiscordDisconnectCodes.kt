package io.github.nerd.discordkt.discord.util

/**
 * @author ashley
 * @since 5/16/17 1:12 PM
 */
enum class DiscordDisconnectCodes constructor(val code: Short) {
	UNKNOWN_ERROR(4000),
	UNKNOWN_OPCODE(4001),
	DECODE_ERROR(4002),
	NOT_AUTHENTICATED(4003),
	AUTHENTICATION_FAILED(4004),
	ALREADY_AUTHENTICATED(4005),
	INVALID_SEQUENCE(4007),
	RATE_LIMIT(4008),
	SESSION_TIMEOUT(4009),
	INVALID_SHARD(4010),
	SHARDING_REQUIRED(4011);

	companion object {
		fun forCode(code: Short): DiscordDisconnectCodes? {
			return values().firstOrNull { code == it.code }
		}
	}
}