package io.github.nerd.discordkt.discord.util.cache

import io.github.nerd.discordkt.discord.entity.user.User
import io.github.nerd.discordkt.discord.model.entity.ModelUser
import io.github.nerd.discordkt.discord.util.IdentifiableCache

/**
 * @author ashley
 * @since 5/19/17 6:47 PM
 */
class UserCache : IdentifiableCache<User>() {
	internal fun fromModel(modelUser: ModelUser?): User? {
		return when {
			modelUser == null -> null
			this.has(modelUser.id) -> this[modelUser.id]
			else -> {
				val entity = modelUser.toEntity()
				this + entity
				entity
			}
		}
	}
}