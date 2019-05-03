package io.github.nerd.discordkt.discord.entity.base

import io.github.nerd.discordkt.discord.util.Cacheable

/**
 * @author ashley
 * @since 5/15/17 12:22 PM
 */
interface Identifiable : Cacheable<Long> {
	override val id: Long
}