package io.github.nerd.discordkt.discord.entity.emoji

import io.github.nerd.discordkt.discord.entity.guild.Guild
import io.github.nerd.discordkt.discord.util.EmojiCategories
import io.github.nerd.discordkt.discord.util.SkinTone
import io.github.nerd.discordkt.discord.util.cache.RoleCache

/**
 * @author ashley
 * @since 5/26/17 9:51 PM
 */
class GuildEmoji(id: Long,
                 name: String,
                 val requiresColons: Boolean,
                 val isManaged: Boolean,
                 val guild: Guild) : Emoji(id, arrayOf(name), EmojiCategories.CUSTOM) {
	val roles = RoleCache()

	override fun withSkinTone(skinTone: SkinTone) = this
	override val mention = "<:${names[0]}:$id>"
}