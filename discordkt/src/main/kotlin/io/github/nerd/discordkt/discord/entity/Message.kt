package io.github.nerd.discordkt.discord.entity

import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.entity.base.Entity
import io.github.nerd.discordkt.discord.entity.base.Identifiable
import io.github.nerd.discordkt.discord.entity.emoji.Emoji
import io.github.nerd.discordkt.discord.entity.guild.channel.GuildTextChannel
import io.github.nerd.discordkt.discord.entity.user.User
import io.github.nerd.discordkt.discord.exception.ensurePermission
import io.github.nerd.discordkt.discord.exception.ensureThat
import io.github.nerd.discordkt.discord.model.Embed
import io.github.nerd.discordkt.discord.model.request.MessageEditRequest
import io.github.nerd.discordkt.discord.util.Permissions
import io.github.nerd.discordkt.discord.util.cache.ReactionCache
import java.io.File
import java.time.ZonedDateTime

/**
 * @author ashley
 * @since 5/24/17 9:40 PM
 */
class Message internal constructor(override val id: Long,
                                   val content: String,
                                   val author: User,
                                   private val channelID: Long,
                                   val timestamp: ZonedDateTime,
                                   val editedTime: ZonedDateTime?,
                                   private val discord: Discord) : Entity, Identifiable {
	/**
	 * A regex that matches Discord invite links.
	 */
	private val inviteRegex = "discord\\.gg/.*".toRegex()

	/**
	 * The channel in which this message was sent
	 */
	val channel = discord.channels.getTextChannelByID(channelID)

	/**
	 * This message's reactions.
	 */
	val reactions = ReactionCache(discord, this)

	/**
	 * Invite codes contained within this message.
	 */
	val invites =
			if (inviteRegex.containsMatchIn(content)) {
				inviteRegex.findAll(content).distinct().map {
					val code = it.value.replace("discord.gg/", "")
					discord.api.getInvite(code).thenApply { it.toEntity(discord) }
				}.toList()
			} else {
				listOf()
			}

	/**
	 * Deletes this message.
	 * If another user's message, requires MANAGE_MESSAGES.
	 */
	fun delete() = channel?.let {
		if (channel is GuildTextChannel && this.author != discord.me)
			channel.guild.ensurePermission(Permissions.MANAGE_MESSAGES)
		discord.api.deleteMessage(channelID, this.id)
	}

	/**
	 * Pins this message in the channel.
	 * Requires MANAGE_MESSAGES
	 */
	fun pin() = channel?.let {
		if (channel is GuildTextChannel) {
			channel.guild.ensurePermission(Permissions.MANAGE_MESSAGES)
			discord.api.pinMessage(channelID, this.id)
		}
	}

	/**
	 * Sends a message in the same channel as this message
	 */
	fun reply(content: String = "",
	          tts: Boolean = false,
	          files: Array<File> = arrayOf(),
	          embed: Embed? = null) = channel?.send(content, tts, files, embed)

	/**
	 * Changes the content of this message.
	 * Author must be logged in user.
	 */
	fun edit(content: String? = null, embed: Embed? = null) {
		ensureThat(author.id != discord.me.id, "Cannot edit another user's message.")
		ensureThat(content != null || embed != null, "Cannot edit a message to be blank.")

		discord.api.editMessage(channelID, this.id, MessageEditRequest(content, embed))
	}

	/**
	 * Adds a reaction to this message.
	 */
	fun react(emoji: Emoji) = channel?.let {
		if (it is GuildTextChannel) {
			it.guild.ensurePermission(Permissions.ADD_REACTIONS, it)
		}
		discord.api.addReaction(it.id, this.id, emoji.toString())
	}

	/**
	 * Removes a reaction from this message.
	 * If user is specified, MANAGE_MESSAGES is required.
	 */
	fun deleteReaction(emoji: Emoji, user: User? = null) = channel?.let {
		val userID = if (user != null) if (user == discord.me) "@me" else user.id.toString() else "@me"
		if (userID != "@me" && it is GuildTextChannel) {
			it.guild.ensurePermission(Permissions.MANAGE_MESSAGES, it)
		}
		discord.api.deleteReaction(channelID, this.id, emoji.toString(), userID)
	}

	/**
	 * Removes all reactions from this message. Requires MANAGE_MESSAGES.
	 */
	fun clearReactions() = channel?.let {
		if (it is GuildTextChannel) {
			it.guild.ensurePermission(Permissions.MANAGE_MESSAGES, it)
			discord.api.deleteAllReactions(this.channelID, this.id)
		}
	}
}