package io.github.nerd.discordkt.discord.entity.user

import io.github.nerd.discordkt.discord.model.entity.ModelPresence
import io.github.nerd.discordkt.discord.util.Status

/**
 * @author ashley
 * @since 5/27/17 8:38 PM
 */
class Presence internal constructor(val status: Status, val game: Game?) {
	val isPlaying: Boolean
		get() = game != null

	val isStreaming: Boolean
		get() = game?.streamURL != null

	class Game(val name: String,
	           val streamURL: String?) {

		internal companion object {
			fun fromModel(model: ModelPresence.ModelGame?) =
					if (model != null) Presence.Game(model.name, model.streamURL) else null
		}
	}
}