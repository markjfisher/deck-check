package io.github.nerd.discordkt.discord.entity.user

import io.github.nerd.discordkt.discord.entity.base.Entity
import io.github.nerd.discordkt.discord.entity.base.Identifiable
import io.github.nerd.discordkt.discord.entity.base.Mentionable
import io.github.nerd.discordkt.discord.util.Status

/**
 * @author ashley
 * @since 5/24/17 9:59 PM
 * Defines a discord user
 */
open class User internal constructor(override final val id: Long,
                                     username: String,
                                     val discriminator: String,
                                     private val avatar: String,
                                     val isBot: Boolean) : Identifiable, Mentionable, Entity {
	/**
	 * This user's username
	 */
	open var username = username
		internal set

	/**
	 * This user's presence
	 */
	var presence = Presence(Status.OFFLINE, null)
		internal set

	override val mention = "<@$id>"
	val fullName = "$username#$discriminator"
	val avatarURL
		get() = "https://cdn.discordapp.com/avatars/$id/$avatar.png"

	override fun equals(other: Any?): Boolean {
		if (other !is User)
			return false
		return other.id == this.id
	}

	override fun hashCode(): Int {
		return id.hashCode()
	}

	override fun toString() = mention
}