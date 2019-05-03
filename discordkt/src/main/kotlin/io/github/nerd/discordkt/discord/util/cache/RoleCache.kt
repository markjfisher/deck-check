package io.github.nerd.discordkt.discord.util.cache

import io.github.nerd.discordkt.discord.entity.guild.Role
import io.github.nerd.discordkt.discord.util.IdentifiableCache

/**
 * @author ashley
 * @since 5/27/17 4:59 AM
 */
class RoleCache : IdentifiableCache<Role>() {
	override val values: List<Role>
		get() = super.values.sortedBy { it.position }.asReversed()
}