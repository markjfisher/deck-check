package io.github.nerd.discordkt.discord.handler

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.handler.base.WebSocketResponseHandler
import io.github.nerd.discordkt.discord.model.base.Model
import io.github.nerd.discordkt.discord.model.entity.ModelGuild
import io.github.nerd.discordkt.discord.model.entity.ModelLocalUser
import io.github.nerd.discordkt.discordLogger

/**
 * @author ashley
 * @since 6/22/17 12:56 AM
 */
open internal class ReadyHandler : WebSocketResponseHandler<ReadyHandler.ReadyModel>("READY", ReadyModel::class.java) {
	override fun handle(data: ReadyModel, discord: Discord) {
		discordLogger.info("Discord API version {}", data.apiVersion)
		discordLogger.info("Connected as {}#{} (ID {})", data.user!!.username, data.user.discriminator, data.user.id)
		discordLogger.debug("Connected via {} at ready.", data.connectedServers.joinToString(" -> "))
		discord.me = data.user.toEntity(discord)

		if (data.guilds != null
				&& data.guilds.isNotEmpty()) {
			data.guilds.forEach {
				if (!it.unavailable)
					guildCreateHandler.handle(it, discord)
			}
		}

		discord.ws.sessionID = data.sessionID
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	internal class ReadyModel(@JsonProperty("v") val apiVersion: Int,
	                          @JsonProperty("session_id") val sessionID: String,
	                          @JsonProperty("_trace") val connectedServers: Array<String>,
	                          @JsonProperty("user") val user: ModelLocalUser?,
	                          @JsonProperty("guilds") val guilds: Array<ModelGuild>?) : Model
}