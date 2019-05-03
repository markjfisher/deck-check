package io.github.nerd.discordkt.discord.auth

/**
 * @author ashley
 * @since 5/14/17 5:18 PM
 */
class Authentication internal constructor(internal val token: String, val type: AuthType) {
	companion object {
		@JvmStatic
		fun user(token: String) = createToken(AuthType.USER, token)

		@JvmStatic
		fun bot(token: String) = createToken(AuthType.BOT, "Bot " + token)

		private fun createToken(type: AuthType, token: String) = Authentication(token, type)
	}
}

/**
 * @author ashley
 * @since 5/14/17 5:15 PM
 */
internal enum class AuthType {
	USER,
	BOT
}