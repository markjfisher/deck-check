package io.github.nerd.discordkt.discord.entity.channel

import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.entity.user.User

/**
 * @author ashley
 * @since 6/24/17 3:07 AM
 */
class DirectPrivateChannel internal constructor(id: Long,
                                                val recipient: User,
                                                discord: Discord) : PrivateChannel(id, discord)