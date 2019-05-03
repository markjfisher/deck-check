package io.github.nerd.discordkt.discord

import io.github.nerd.discordkt.*
import io.github.nerd.discordkt.discord.api.DiscordAPI
import io.github.nerd.discordkt.discord.auth.AuthType
import io.github.nerd.discordkt.discord.auth.Authentication
import io.github.nerd.discordkt.discord.entity.OAuthApplication
import io.github.nerd.discordkt.discord.entity.channel.Channel
import io.github.nerd.discordkt.discord.entity.user.LocalUser
import io.github.nerd.discordkt.discord.entity.user.User
import io.github.nerd.discordkt.discord.model.channel.ModelDirectPrivateChannel
import io.github.nerd.discordkt.discord.model.channel.ModelGroupPrivateChannel
import io.github.nerd.discordkt.discord.util.DiscordInterceptor
import io.github.nerd.discordkt.discord.util.cache.*
import io.github.nerd.discordkt.discord.ws.DiscordWebSocketListener
import io.github.nerd.discordkt.event.Event
import io.github.nerd.discordkt.event.Events
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.adapter.java8.Java8CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.CompletableFuture

/**
 * @author ashley
 * @since 5/14/17 5:13 PM
 *
 * Main class. Defines a discord client.
 */
class Discord internal constructor(internal val auth: Authentication) {
	lateinit var me: LocalUser
		internal set
	internal val ws: DiscordWebSocketListener = DiscordWebSocketListener(this)

	private val httpClient = OkHttpClient.Builder()
			.addInterceptor(DiscordInterceptor(auth)).build()

	internal val api = Retrofit.Builder()
			.baseUrl(API_BASEURL)
			.client(httpClient)
			.addConverterFactory(JacksonConverterFactory.create(mapper))
			// .addCallAdapterFactory(Java8CallAdapterFactory.create())
			.build().create(DiscordAPI::class.java)

	init {
		discordLogger.info("{} version {} by {}", PROJ_NAME, PROJ_VER, PROJ_AUTHOR)
		if (auth.type == AuthType.USER)
			discordLogger.warn("User bots are not officially supported by Discord and may result in your account getting banned. Use at your own risk.")
	}

	/**
	 * A cache of all of the guilds this client is joined to
	 */
	val guilds = GuildCache()

	/**
	 * A cache of all channels available to this client
	 */
	val channels = ChannelCache<Channel>()

	/**
	 * A cache of all users seen by this client
	 */
	val users = UserCache()

	/**
	 * A cache of all emojis this client can use
	 */
	val emojis = EmojiCache()

	/**
	 * A cache of all private message conversation this client has
	 */
	val privateChannels = PrivateChannelCache(this)

	val events = Events()

	var application: OAuthApplication? = null
		private set

	/**
	 * Connects to the Discord websocket
	 */
	fun connect(): Discord {
		if (ws.isConnected) {
			discordLogger.error("Discord is already connected.")
		} else {
			httpClient.newWebSocket(Request.Builder().get().url(obtainGateway()).build(), ws)

			discordLogger.trace("WebSocket connection initiated.")

			api.fetchPrivateChannels().thenAccept {
				it.forEach {
					val recipientEntities = arrayListOf<User>()
					it.recipients.forEach {
						val user: User
						if (users.has(it.id)) {
							user = users[it.id]!!
						} else {
							user = it.toEntity()
							users + user
						}
						recipientEntities.add(user)
					}

					if (it is ModelGroupPrivateChannel || it is ModelDirectPrivateChannel) {
						val entity = it.toEntity(this)
						discordLogger.debug("Received private channel.")
						channels + entity
					} else {
						discordLogger.warn("Unknown private channel type")
					}
				}
			}

			if (auth.type == AuthType.BOT) {
				api.getApplicationInfo().thenApply {
					val owner: User
					if (this.users.has(it.owner.id)) {
						owner = this.users[it.owner.id]!!
					} else {
						owner = it.owner.toEntity()
						this.users + owner
					}
					OAuthApplication(it.id, it.name, it.icon,
							it.description, it.rpcOriginUrls ?: arrayOf(),
							it.isPublic, it.requireFullCodeGrant, owner)
				}.thenAccept { application = it }
			}
		}
		return this
	}

	/**
	 * Registers an event listener to this discord instance
	 */
	inline fun <reified T : Event> on(noinline exec: (event: T) -> Unit) = this.events.on(exec)

	/**
	 * @return The WebSocket gateway this discord instance will use
	 */
	private fun obtainGateway() = api.obtainGateway().get().url + "?encoding=json&v=6"
}

/**
 * Creates a Future containing a Discord instance that completes when discord has finished connecting.
 */
fun discord(auth: Authentication): CompletableFuture<Discord> =
		CompletableFuture.supplyAsync({ Discord(auth).connect() })