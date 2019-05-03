package io.github.nerd.discordkt.discord.entity.user

import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.exception.ensureThat
import io.github.nerd.discordkt.discord.model.request.PresenceChangeRequest
import io.github.nerd.discordkt.discord.model.request.UserUpdateRequest
import io.github.nerd.discordkt.discord.model.request.WebSocketRequest
import io.github.nerd.discordkt.discord.util.DiscordOpCodes
import io.github.nerd.discordkt.discord.util.Status
import io.github.nerd.discordkt.tika
import java.io.File
import java.util.*

/**
 * @author ashley
 * @since 5/24/17 10:03 PM
 * Defines the user our client is logged in as.
 */
class LocalUser internal constructor(id: Long,
                                     username: String,
                                     discriminator: String,
                                     avatar: String,
                                     isBot: Boolean,
                                     val verified: Boolean,
                                     val mfaEnabled: Boolean,
                                     val email: String,
                                     private val discord: Discord) : User(id, username, discriminator, avatar, isBot) {
	override var username: String = super.username
		get() = super.username
		set(value) {
			ensureThat(this.isBot, "Cannot change user account's username.") // requires account password
			if (value != username) {
				discord.api.updateUser(UserUpdateRequest(value, null))
				field = value
			}
		}

	/**
	 * Changes the logged in user's status to online, optionally with a game or stream URL
	 */
	fun online(game: String? = null, streamURL: String? = null) = changePresence(Status.ONLINE, game, streamURL)

	/**
	 * Changes the logged in user's status to idle, optionally with a game or stream URL
	 */
	fun idle(game: String? = null, streamURL: String? = null) = changePresence(Status.IDLE, game, streamURL)

	/**
	 * Changes the logged in user's status to do not disturb, optionally with a game or stream URL
	 */
	fun doNotDisturb(game: String? = null, streamURL: String? = null) = changePresence(Status.DO_NOT_DISTURB, game, streamURL)

	/**
	 * Changes the logged in user's status to invisible, optionally with a game or stream URL
	 */
	fun invisible(game: String? = null, streamURL: String? = null) = changePresence(Status.INVISIBLE, game, streamURL)

	private fun changePresence(status: Status,
	                           game: String?,
	                           streamURL: String?) =
			discord.ws.send(WebSocketRequest(DiscordOpCodes.STATUS_UPDATE,
					PresenceChangeRequest(status,
							if (status == Status.IDLE) System.currentTimeMillis() else 0,
							if (game != null) PresenceChangeRequest.Game(game,
									if (streamURL != null) 1 else 0, streamURL) else null,
							status == Status.IDLE)))

	/**
	 * Changes our avatar (if we're logged in as a bot user)
	 */
	fun changeAvatar(avatar: File) {
		ensureThat(this.isBot, "Cannot change user account's avatar.") // requires account password
		val type = tika.detect(avatar)
		ensureThat(type.equals("image/png", ignoreCase = true) ||
				type.equals("image/jpeg", ignoreCase = true) ||
				type.equals("image/gif", ignoreCase = true),
				"You can only upload JPEG, GIF, or PNG images as an avatar.")
		discord.api.updateUser(UserUpdateRequest(null,
				"data:$type;base64,${String(Base64.getEncoder().encode(avatar.readBytes()))}"))
	}
}