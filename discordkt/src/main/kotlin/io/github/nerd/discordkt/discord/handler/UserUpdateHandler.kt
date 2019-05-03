package io.github.nerd.discordkt.discord.handler

import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.handler.base.WebSocketResponseHandler
import io.github.nerd.discordkt.discord.model.entity.ModelLocalUser
import io.github.nerd.discordkt.discordLogger

/**
 * @author ashley
 * @since 6/22/17 12:21 AM
 */
internal class UserUpdateHandler : WebSocketResponseHandler<ModelLocalUser>("USER_UPDATE", ModelLocalUser::class.java) {
	override fun handle(data: ModelLocalUser, discord: Discord) {
		discord.me = data.toEntity(discord)
		discordLogger.debug("Logged in user has been updated.")
	}
}