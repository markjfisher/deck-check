package io.github.nerd.discordkt.discord.util.cache

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.entity.emoji.DefaultEmoji
import io.github.nerd.discordkt.discord.entity.emoji.Emoji
import io.github.nerd.discordkt.discord.model.entity.ModelEmoji
import io.github.nerd.discordkt.discord.util.EmojiCategories
import io.github.nerd.discordkt.discord.util.IdentifiableCache
import io.github.nerd.discordkt.discord.util.SkinTone
import io.github.nerd.discordkt.mapper

/**
 * @author ashley
 * @since 5/26/17 8:12 PM
 */
class EmojiCache : IdentifiableCache<Emoji>() {
	init {
		val emojis = mapper.readValue<HashMap<EmojiCategories, List<ModelEmoji>>>(Discord::class.java.getResourceAsStream("/emoji.json"))
		for (category in emojis.keys) {
			emojis[category]?.forEach {
				this + DefaultEmoji(it.names, category, it.surrogate, it.isDiverse ?: false)
			}
		}
	}

	fun has(string: String) = filter { it.names.contains(string) || it.mention.equals(string, ignoreCase = true) }.isNotEmpty()

	operator fun get(category: EmojiCategories) = values.filterIsInstance<DefaultEmoji>().filter { it.category == category }

	operator fun get(string: String): Emoji? {
		if (string.contains(Regex("[a-zA-Z0-9]")))
			return getEmojiByAlias(string)
		else return getEmojiByUnicode(string)
	}

	/**
	 * @param name The alias to search for
	 * *
	 * @return an emoji object that shares an alias with the provided name
	 */
	fun getEmojiByAlias(name: String): Emoji? {
		var skinTone = SkinTone.NONE
		var name1 = name
		if (SkinTone.skinToneAliasPattern.containsMatchIn(name)) {
			skinTone = SkinTone.fromAlias(SkinTone.skinTonePattern.find(name)?.value ?: "")
			name1 = name.replace(SkinTone.skinToneAliasPattern, "")
		}

		val opt = filter { it.names.contains(name1) }.firstOrNull()
		if (opt != null) return opt.withSkinTone(skinTone)
		return opt
	}

	/**
	 * @param unicode The unicode to search for
	 * *
	 * @return an emoji object that shares unicode representation with the provided unicode
	 */
	fun getEmojiByUnicode(unicode: String): Emoji? {
		var skinTone = SkinTone.NONE
		var unicode1 = unicode
		if (SkinTone.skinTonePattern.containsMatchIn(unicode)) {
			skinTone = SkinTone.fromUnicode(SkinTone.skinTonePattern.find(unicode)?.value ?: "")
			unicode1 = unicode.replace(SkinTone.skinTonePattern, "")
		}
		val opt = filter { it.mention.equals(unicode1, ignoreCase = true) }.firstOrNull()
		if (opt != null) return opt.withSkinTone(skinTone)
		return opt
	}
}