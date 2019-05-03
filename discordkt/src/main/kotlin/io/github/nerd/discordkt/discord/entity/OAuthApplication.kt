package io.github.nerd.discordkt.discord.entity

import io.github.nerd.discordkt.discord.entity.base.Identifiable
import io.github.nerd.discordkt.discord.entity.user.User

/**
 * @author ashley
 * @since 6/22/17 8:50 PM
 */
class OAuthApplication internal constructor(override val id: Long,
                                            val name: String,
                                            val icon: String?,
                                            val description: String?,
                                            val rpcOriginUrls: Array<String>,
                                            val isPublic: Boolean,
                                            val requiresOAuthCodeGrant: Boolean,
                                            val owner: User) : Identifiable {
	val iconURL
		get() = "https://cdn.discordapp.com/app-icons/$id/$icon.png"
}