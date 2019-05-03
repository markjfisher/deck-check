package io.github.nerd.discordkt.discord.entity.guild.channel

import io.github.nerd.discordkt.discord.entity.base.Identifiable
import io.github.nerd.discordkt.discord.entity.guild.GuildMember
import io.github.nerd.discordkt.discord.entity.guild.Role
import io.github.nerd.discordkt.discord.util.Permissions

/**
 * @author ashley
 * @since 5/26/17 4:09 AM
 */
open class PermissionOverwrite<out T : Identifiable>(val entity: T,
                                                     val type: OverwriteType,
                                                     val allowed: List<Permissions>,
                                                     val denied: List<Permissions>) : Identifiable {
	override val id = entity.id
}

class UserPermissionOverwrite(entity: GuildMember, allowed: List<Permissions>, denied: List<Permissions>) : PermissionOverwrite<GuildMember>(entity, OverwriteType.USER, allowed, denied)
class RolePermissionOverwrite(entity: Role, allowed: List<Permissions>, denied: List<Permissions>) : PermissionOverwrite<Role>(entity, OverwriteType.ROLE, allowed, denied)

enum class OverwriteType {
	ROLE,
	USER;
}