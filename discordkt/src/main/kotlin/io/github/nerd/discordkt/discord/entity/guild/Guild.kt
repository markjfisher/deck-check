package io.github.nerd.discordkt.discord.entity.guild

import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.entity.base.Entity
import io.github.nerd.discordkt.discord.entity.base.Identifiable
import io.github.nerd.discordkt.discord.entity.guild.channel.GuildChannel
import io.github.nerd.discordkt.discord.entity.guild.channel.GuildTextChannel
import io.github.nerd.discordkt.discord.entity.user.User
import io.github.nerd.discordkt.discord.exception.ensurePermission
import io.github.nerd.discordkt.discord.model.request.NicknameChangeRequestModel
import io.github.nerd.discordkt.discord.util.IdentifiableCache
import io.github.nerd.discordkt.discord.util.Permissions
import io.github.nerd.discordkt.discord.util.cache.EmojiCache
import io.github.nerd.discordkt.discord.util.cache.RoleCache

/**
 * @author ashley
 * @since 5/25/17 6:55 PM
 * Represents a discord "server" or guild.
 */
class Guild internal constructor(override val id: Long,
                                 val name: String,
                                 private val discord: Discord) : Entity, Identifiable {
	/**
	 * A cache of all members in this guild.
	 */
	val members = MemberCache()

	/**
	 * A cache of all roles in this guild.
	 */
	val roles = RoleCache()

	/**
	 * A cache of all channels in this guild.
	 */
	val channels = GuildChannelCache()

	/**
	 * A cache of all emojis in this guild.
	 */
	val emojis = EmojiCache()

	/**
	 * The owner of this guild.
	 */
	lateinit var owner: GuildMember
		internal set

	/**
	 * The logged in user or bot's member object.
	 */
	lateinit var me: GuildMember
		internal set

	/**
	 * Change the specified user's nickname in this guild.
	 */
	fun changeNickname(nickname: String = "", member: GuildMember = me) {
		ensurePermission(Permissions.CHANGE_NICKNAME, this)
		discord.api.changeNickname(this.id, if (member == me) "@me" else member.id.toString(), NicknameChangeRequestModel(nickname))
	}

	/**
	 * Leave this guild.
	 */
	fun leave() = discord.api.leaveGuild(this.id)

	class MemberCache : IdentifiableCache<GuildMember>() {
		fun has(user: User) = this.has(user.id)
		operator fun get(user: User) = this[user.id]
	}

	class GuildChannelCache : IdentifiableCache<GuildChannel>() {
		fun getTextChannel(id: Long) = values.filterIsInstance<GuildTextChannel>().firstOrNull { it.id == id }
		operator fun get(name: String, ignoreCase: Boolean = false) = values.filter { it.name.equals(name, ignoreCase) }
	}
}