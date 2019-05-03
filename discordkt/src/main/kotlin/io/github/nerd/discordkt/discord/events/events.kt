package io.github.nerd.discordkt.discord.events

import io.github.nerd.discordkt.discord.entity.Message
import io.github.nerd.discordkt.discord.entity.channel.Channel
import io.github.nerd.discordkt.discord.entity.emoji.Emoji
import io.github.nerd.discordkt.discord.entity.emoji.Reaction
import io.github.nerd.discordkt.discord.entity.guild.Guild
import io.github.nerd.discordkt.discord.entity.guild.GuildMember
import io.github.nerd.discordkt.discord.entity.guild.Role
import io.github.nerd.discordkt.discord.entity.user.Presence
import io.github.nerd.discordkt.discord.model.Embed
import io.github.nerd.discordkt.event.Event
import java.io.File

/**
 * @author ashley
 * @since 6/7/17 5:48 AM
 */

/**
 * Called when the client receives a new guild.
 */
class GuildCreateEvent internal constructor(val guild: Guild) : Event

/**
 * Called when a message is deleted.
 */
class MessageDeleteEvent internal constructor(val message: Message) : Event

/**
 * Called when a message is updated.
 */
class MessageUpdateEvent internal constructor(val oldMessage: Message, val newMessage: Message) : Event

/**
 * Called when a member's nickname is updated in a guild.
 */
class NicknameChangeEvent internal constructor(val oldNickname: String?, val newNickname: String?, val member: GuildMember) : Event

/**
 * Called when a user's presence (game or online status) updates.
 */
class PresenceUpdateEvent internal constructor(val oldPresence: Presence, val newPresence: Presence) : Event

/**
 * Called when a reaction is added to a message.
 */
class ReactionAddEvent internal constructor(val reaction: Reaction) : Event {
	/**
	 * Deletes the reaction. Requires MANAGE_MESSAGES if another user is the author.
	 */
	fun delete() = reaction.delete()
}

/**
 * Called when a reaction is removed from a message.
 */
class ReactionRemoveEvent(val reaction: Reaction) : Event

/**
 * Called when a role is created within a guild.
 */
class RoleCreateEvent internal constructor(val role: Role) : Event

/**
 * Called when a role is updated within a guild.
 */
class RoleUpdateEvent(val oldRoles: Array<Role>, val newRoles: Array<Role>, val member: GuildMember) : Event

/**
 * Called when a channel changes its name
 */
class ChannelNameChangeEvent(val oldName: String, val newName: String, channel: Channel) : Event

/**
 * Called when a message is received.
 */
class MessageRecvEvent internal constructor(val message: Message) : Event {
	val channel = message.channel
	val content = message.content
	val author = message.author

	fun reply(content: String = "",
	          tts: Boolean = false,
	          files: Array<File> = arrayOf(),
	          embed: Embed? = null) = message.reply(content, tts, files, embed)

	fun delete() = message.delete()
	fun react(emoji: Emoji) = message.react(emoji)
}
