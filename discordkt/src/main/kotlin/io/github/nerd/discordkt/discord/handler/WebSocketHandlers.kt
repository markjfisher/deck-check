package io.github.nerd.discordkt.discord.handler

import io.github.nerd.discordkt.PROJ_NAME
import io.github.nerd.discordkt.discord.entity.emoji.GuildEmoji
import io.github.nerd.discordkt.discord.entity.guild.Guild
import io.github.nerd.discordkt.discord.entity.guild.GuildMember
import io.github.nerd.discordkt.discord.entity.guild.Role
import io.github.nerd.discordkt.discord.entity.guild.channel.ChannelCategory
import io.github.nerd.discordkt.discord.entity.user.Presence
import io.github.nerd.discordkt.discord.entity.user.User
import io.github.nerd.discordkt.discord.events.GuildCreateEvent
import io.github.nerd.discordkt.discord.handler.base.webSocketHandler
import io.github.nerd.discordkt.discord.model.entity.ModelGuild
import io.github.nerd.discordkt.discord.model.request.WebSocketRequest
import io.github.nerd.discordkt.discord.model.response.HelloModel
import io.github.nerd.discordkt.discord.util.DiscordOpCodes
import io.github.nerd.discordkt.discord.util.Status
import io.github.nerd.discordkt.discordLogger
import kotlin.concurrent.timer

/**
 * @author ashley
 * @since 6/7/17 6:10 AM
 */
internal val helloHandler = webSocketHandler(HelloModel::class.java, null, opCode = DiscordOpCodes.HELLO) { data, discord ->
	discordLogger.debug("Received heartbeat interval of {}", data.heartbeatInterval)
	discordLogger.debug("Connected via {} at hello.", data.connectedServers.joinToString(" -> "))
	timer("$PROJ_NAME heartbeat", true, data.heartbeatInterval, data.heartbeatInterval) {
		discordLogger.trace("Sending heartbeat with last sequence: {}", discord.ws.lastSequence)
		discord.ws.send(WebSocketRequest(DiscordOpCodes.HEARTBEAT, discord.ws.lastSequence))
	}
}

internal val guildCreateHandler = webSocketHandler(ModelGuild::class.java, "GUILD_CREATE") { data, discord ->
	discordLogger.debug("Received guild \"{}\" (ID {})", data.name, data.id)
	val guild = Guild(data.id, data.name ?: return@webSocketHandler, discord)

	data.roles?.forEach { guild.roles + Role(it, guild) }

	data.members?.forEach {
		val roles = guild.roles.filter { role -> it.roles.contains(role.id) || role.id == guild.id }
		val user: User
		if (discord.users.has(it.user.id)) {
			user = discord.users[it.user.id]!!
		} else {
			user = it.user.toEntity()
			discord.users + user
		}
		val memberEntity = GuildMember(user, guild, it.nick, it.mute, it.deaf, it.joinDate, discord)
		memberEntity.roles.cacheAll(roles)
		discord.users + memberEntity.user
		if (memberEntity.id == data.ownerID)
			guild.owner = memberEntity
		if (memberEntity.id == discord.me.id)
			guild.me = memberEntity
		guild.members + memberEntity
	}

	data.presences?.forEach {
		if (discord.users.has(it.user.id)) {
			val user = discord.users[it.user.id]!!
			val game = Presence.Game.fromModel(it.game)
			val presence = Presence(Status.fromText(it.status), game)
			user.presence = presence
		}
	}

	data.emojis?.forEach {
		val roles = guild.roles.filter { role -> it.roles.contains(role.id) }
		val emoji = GuildEmoji(it.id, it.name, it.requiresColons, it.isManaged, guild)
		roles.forEach { emoji.roles + it }
		discord.emojis + emoji
		guild.emojis + emoji
	}

	data.channels?.let {
		val channels = it.associate { Pair(it, it.toEntity(guild, discord)) }
		channels.filter { it.key.parentID != null && it.value !is ChannelCategory }
				.forEach { model, entity ->
					if (entity != null)
						entity.parent = channels.values.first { it != null && it.id == model.parentID }
				}
		channels.values.filterNotNull().forEach {
			discord.channels + it
			guild.channels + it
		}
	}

	discord.guilds + guild
	discord.events.dispatch(GuildCreateEvent(guild))
}