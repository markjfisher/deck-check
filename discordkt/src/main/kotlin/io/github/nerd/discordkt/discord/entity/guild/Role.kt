package io.github.nerd.discordkt.discord.entity.guild

import io.github.nerd.discordkt.discord.entity.base.Entity
import io.github.nerd.discordkt.discord.entity.base.Identifiable
import io.github.nerd.discordkt.discord.entity.base.Mentionable
import io.github.nerd.discordkt.discord.model.entity.ModelRole
import io.github.nerd.discordkt.discord.util.Permissions

/**
 * @author ashley
 * @since 5/25/17 6:57 PM
 * Defines a role for a given guild.
 */
class Role internal constructor(override val id: Long,
                                val position: Short = -1,
                                val permissions: Array<Permissions>,
                                val name: String,
                                val mentionable: Boolean,
                                val managed: Boolean,
                                val hoist: Boolean,
                                val color: Int,
                                val guild: Guild) : Entity, Identifiable, Mentionable {

	internal constructor(model: ModelRole, guild: Guild) : this(model.id,
			model.position, model.permissions, model.name, model.mentionable, model.managed, model.hoist, model.color, guild)

	/**
	 * Creates a mention for this role.
	 */
	override val mention = "<@&$id>"

	/**
	 * Creates a mention for this role if possible, or just returns the name.
	 */
	override fun toString() = if (this.mentionable) mention else name
}