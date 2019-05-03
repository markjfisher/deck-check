package io.github.nerd.discordkt.discord.util.cache

import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.entity.channel.DirectPrivateChannel
import io.github.nerd.discordkt.discord.entity.channel.GroupPrivateChannel
import io.github.nerd.discordkt.discord.entity.channel.PrivateChannel
import io.github.nerd.discordkt.discord.entity.user.User
import io.github.nerd.discordkt.discord.model.request.PrivateChannelCreateRequest
import java.util.concurrent.CompletableFuture

/**
 * @author ashley
 * @since 6/22/17 2:55 AM
 */
class PrivateChannelCache(private val discord: Discord) : ChannelCache<PrivateChannel>() {
	val emptyCompletable = lazy {
		val cf = CompletableFuture<PrivateChannel?>(); cf.complete(null); cf
	}

	operator fun get(user: User): CompletableFuture<PrivateChannel?> =
			if (user.isBot) emptyCompletable.value else
				values.filterIsInstance<DirectPrivateChannel>().firstOrNull { it.recipient == user }?.let {
					val cf = CompletableFuture<PrivateChannel?>(); cf.complete(it); cf
				} ?:
						discord.api.createPrivateChannel(PrivateChannelCreateRequest(arrayOf(user.id)))
								.thenApply { it.toEntity(discord) }
								.exceptionally { null }

	operator fun get(vararg users: User) =
			if (users.size == 1) get(users[0]) else values.filterIsInstance<GroupPrivateChannel>().filter {
				it.recipients == users
			}.let {
				val cf = CompletableFuture<List<PrivateChannel>>(); cf.complete(it); cf
			}
}