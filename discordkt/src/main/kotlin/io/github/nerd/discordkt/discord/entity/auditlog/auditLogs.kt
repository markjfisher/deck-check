package io.github.nerd.discordkt.discord.entity.auditlog

import io.github.nerd.discordkt.discord.entity.Invite
import io.github.nerd.discordkt.discord.entity.base.Entity
import io.github.nerd.discordkt.discord.entity.base.Identifiable
import io.github.nerd.discordkt.discord.entity.channel.Channel
import io.github.nerd.discordkt.discord.entity.channel.TextChannel
import io.github.nerd.discordkt.discord.entity.guild.Guild
import io.github.nerd.discordkt.discord.entity.guild.Role
import io.github.nerd.discordkt.discord.entity.guild.channel.GuildChannel
import io.github.nerd.discordkt.discord.entity.guild.channel.GuildTextChannel
import io.github.nerd.discordkt.discord.entity.user.User

class AuditLogChange<out T>(val new: T, val old: T)

class AuditLogEntry<out T : Identifiable>(override val id: Long,
                                          val target: T,
                                          val user: User,
                                          val actionType: AuditLogEvents,
                                          val changes: Map<AuditLogChangeKey, AuditLogChange<*>>) : Identifiable

enum class AuditLogChangeKey(val keyString: String, targetClass: Class<out Entity>) {
	GUILD_NAME("name", Guild::class.java),
	GUILD_ICON("icon_hash", Guild::class.java),
	GUILD_SPLASH("splash_hash", Guild::class.java),
	GUILD_OWNER("owner_id", Guild::class.java),
	GUILD_REGION("region", Guild::class.java),
	GUILD_AFK_CHANNEL("afk_channel_id", Guild::class.java),
	GUILD_AFK_TIMEOUT("guild_afk_timeout", Guild::class.java),
	GUILD_MFA_LEVEL("mfa_level", Guild::class.java),
	GUILD_VERIFICATION_LEVEL("verification_level", Guild::class.java),
	GUILD_EXPLICIT_CONTENT_FILTER("explicit_content_filter", Guild::class.java),
	GUILD_DEFAULT_MESSAGE_NOTIFICATIONS("default_message_notifications", Guild::class.java),
	GUILD_VANITY_URL("vanity_url_code", Guild::class.java),
	GUILD_ROLE_ADD("\$add", Guild::class.java),
	GUILD_ROLE_REMOVE("\$remove", Guild::class.java),
	GUILD_PRUNE_DELETE_DAYS("prune_delete_days", Guild::class.java),
	GUILD_WIDGET_ENABLED("widget_enabled", Guild::class.java),
	GUILD_WIDGET_CHANNEL("widget_channel_id", Guild::class.java),
	CHANNEL_POSITION("position", GuildChannel::class.java),
	CHANNEL_TOPIC("topic", TextChannel::class.java),
	CHANNEL_BITRATE("bitrate", Channel::class.java),
	CHANNEL_PERMISSION_OVERWRITES("permission_overwrites", GuildChannel::class.java),
	CHANNEL_NSFW("nsfw", GuildTextChannel::class.java),
	CHANNEL_APPLICATION_ID("application_id", Channel::class.java), // todo better name fo this
	ROLE_PERMISSIONS("permissions", Role::class.java),
	ROLE_COLOR("color", Role::class.java),
	ROLE_HOIST("hoist", Role::class.java),
	ROLE_MENTIONABLE("mentionable", Role::class.java),
	ROLE_ALLOW("allow", Role::class.java),
	ROLE_DENY("deny", Role::class.java),
	INVITE_CODE("code", Invite::class.java),
	INVITE_CHANNEL("channel_id", Invite::class.java),
	INVITE_INVITER("inviter_id", Invite::class.java),
	INVITE_MAX_USES("max_uses", Invite::class.java),
	INVITE_USES("uses", Invite::class.java),
	INVITE_MAX_AGE("max_age", Invite::class.java),
	INVITE_TEMPORARY("temporary", Invite::class.java),
	USER_DEAF("deaf", User::class.java),
	USER_MUTE("mute", User::class.java),
	USER_NICK("nick", User::class.java),
	USER_AVATAR("avatar_hash", User::class.java);
}

enum class AuditLogEvents(val code: Byte) {
	GUILD_UPDATE(1),
	CHANNEL_CREATE(10),
	CHANNEL_UPDATE(11),
	CHANNEL_DELETE(12),
	CHANNEL_OVERWRITE_CREATE(13),
	CHANNEL_OVERWRITE_UPDATE(14),
	CHANNEL_OVERWRITE_DELETE(15),
	MEMBER_KICK(20),
	MEMBER_PRUNE(21),
	MEMBER_BAN_ADD(22),
	MEMBER_BAN_REMOVE(23), // unban
	MEMBER_UPDATE(24),
	MEMBER_ROLE_UPDATE(25),
	ROLE_CREATE(30),
	ROLE_UPDATE(31),
	ROLE_DELETE(32),
	INVITE_CREATE(40),
	INVITE_UPDATE(41),
	INVITE_DELETE(42),
	WEBHOOK_CREATE(50),
	WEBHOOK_UPDATE(51),
	WEBHOOK_DELETE(52),
	EMOJI_CREATE(60),
	EMOJI_UPDATE(61),
	EMOJI_DELETE(62),
	MESSAGE_DELETE(72)
}