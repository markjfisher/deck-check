package io.github.nerd.discordkt.discord.util

import io.github.nerd.discordkt.discord.entity.base.Identifiable

/**
 * @author ashley
 * @since 5/29/17 8:04 PM
 */
open class IdentifiableCache<T : Identifiable> internal constructor() : Cache<Long, T>()