package io.github.nerd.discordkt.discord.entity.channel

import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.entity.user.User

/**
 * @author ashley
 * @since 6/24/17 3:05 AM
 */
class GroupPrivateChannel internal constructor(id: Long,
                                               val recipients: List<User>,
                                               discord: Discord) : PrivateChannel(id, discord)