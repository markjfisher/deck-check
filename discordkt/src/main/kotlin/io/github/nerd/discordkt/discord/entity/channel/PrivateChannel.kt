package io.github.nerd.discordkt.discord.entity.channel

import io.github.nerd.discordkt.discord.Discord

/**
 * @author ashley
 * @since 5/25/17 7:33 PM
 */
abstract class PrivateChannel internal constructor(id: Long,
                                                   discord: Discord) : TextChannel(id, discord)