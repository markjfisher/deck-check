package io.github.nerd.discordkt.discord.entity.guild.channel

import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.entity.Message
import io.github.nerd.discordkt.discord.entity.base.Entity
import io.github.nerd.discordkt.discord.entity.base.Mentionable
import io.github.nerd.discordkt.discord.entity.channel.TextChannel
import io.github.nerd.discordkt.discord.exception.ensurePermission
import io.github.nerd.discordkt.discord.exception.ensureThat
import io.github.nerd.discordkt.discord.model.request.BulkDeleteRequestModel
import io.github.nerd.discordkt.discord.model.request.GuildChannelUpdateRequest
import io.github.nerd.discordkt.discord.util.Permissions

/**
 * @author ashley
 * @since 5/26/17 2:43 AM
 * Represents a text channel in a given guild.
 */
class GuildTextChannel internal constructor(override val id: Long,
                                            topic: String?,
                                            internal val guildChannelImpl: GuildChannelImpl,
                                            discord: Discord) :
		TextChannel(id, discord), GuildChannel by guildChannelImpl, Entity, Mentionable {

	internal var _topic = topic
	var topic = topic
		get() = _topic
	// todo set

	/**
	 * The category this channel is in, or null if it has no category
	 */
	override var parent: GuildChannel? = null
		set(value) {
			if (!guild.channels.has(this))
				field = value
			else
				value?.let {
					ensureThat(value is ChannelCategory, "Parent must be an instance of ChannelCategory.")
					ensurePermission(Permissions.MANAGE_CHANNELS, this.guild)
					discord.api.updateChannel(this.guild.id, GuildChannelUpdateRequest(this.id, position.toInt(), value.id))
							.thenAccept { field = value }
				}
		}

	/**
	 * Regex that matches NSFW channel names.
	 */
	private val nsfwRegex = "^nsfw(-|$)".toRegex()

	/**
	 * Whether or not this channel is marked as NSFW (not safe for work)
	 */
	val isNsfw
		get() = nsfwRegex.matches(name)

	/**
	 * Creates a clickable reference to this channel.
	 */
	override val mention = "<#$id>"

	/**
	 * Bulk deletes the given messages from this channel.
	 * Requires the MANAGE_MESSAGES permission.
	 */
	fun deleteMessages(messages: Array<Message>) {
		val clean = messages.filter { it.channel == this }.distinctBy { it.id }
		ensureThat(clean.size <= 100, "You can delete a maximum of 100 messages.")
		when {
			clean.isEmpty() -> {
				// do nothing
			}
			clean.size == 1 -> {
				clean.firstOrNull()?.delete()
			}
			else -> {
				guild.ensurePermission(Permissions.MANAGE_MESSAGES, this)
				discord.api.bulkDelete(this.id, BulkDeleteRequestModel(clean.map { it.id }.toTypedArray()))
			}
		}
	}

	/**
	 * Creates a clickable reference to this channel.
	 */
	override fun toString() = mention
}