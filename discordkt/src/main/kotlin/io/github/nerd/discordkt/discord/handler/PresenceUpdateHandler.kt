package io.github.nerd.discordkt.discord.handler

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.entity.user.Presence
import io.github.nerd.discordkt.discord.entity.user.User
import io.github.nerd.discordkt.discord.events.PresenceUpdateEvent
import io.github.nerd.discordkt.discord.handler.base.WebSocketResponseHandler
import io.github.nerd.discordkt.discord.model.base.Model
import io.github.nerd.discordkt.discord.model.entity.ModelPresence
import io.github.nerd.discordkt.discord.model.entity.ModelUser
import io.github.nerd.discordkt.discord.util.Status
import io.github.nerd.discordkt.discordLogger

/**
 * @author ashley
 * @since 5/27/17 10:28 PM
 */
internal class PresenceUpdateHandler : WebSocketResponseHandler<PresenceUpdateHandler.PresenceUpdateModel>("PRESENCE_UPDATE", handledClass = PresenceUpdateModel::class.java) {
	override fun handle(data: PresenceUpdateModel, discord: Discord) {
		val user: User
		if (data.user.username != null) {
			user = data.user.toEntity()
			discord.users.updateObj(user)
		} else if (discord.users.has(data.user.id)) {
			user = discord.users[data.user.id]!!
		} else return


		val game = Presence.Game.fromModel(data.game)
		val newPresence = Presence(Status.fromText(data.status), game)
		val oldPresence = user.presence
		user.presence = newPresence
		if (user.id != discord.me.id) {
			discord.events.dispatch(PresenceUpdateEvent(oldPresence, newPresence))
			discordLogger.debug("{} is now {}", user.fullName, data.status)
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	internal class PresenceUpdateModel(@JsonProperty("user") val user: ModelUser,
	                                   @JsonProperty("status") val status: String,
	                                   @JsonProperty("roles") val roles: Array<Long>,
	                                   @JsonProperty("guild_id") val guildID: Long,
	                                   @JsonProperty("nick") val nickname: String?,
	                                   @JsonProperty("game") val game: ModelPresence.ModelGame?) : Model
}