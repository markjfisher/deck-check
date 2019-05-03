package io.github.nerd.discordkt.discord.ws

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.handler.*
import io.github.nerd.discordkt.discord.model.request.GatewayResumeRequest
import io.github.nerd.discordkt.discord.model.request.WebSocketRequest
import io.github.nerd.discordkt.discord.model.response.base.BasicWebSocketResponse
import io.github.nerd.discordkt.discord.util.DiscordDisconnectCodes
import io.github.nerd.discordkt.discord.util.DiscordOpCodes
import io.github.nerd.discordkt.discord.util.cache.WebSocketHandlerCache
import io.github.nerd.discordkt.discordLogger
import io.github.nerd.discordkt.mapper
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

/**
 * @author ashley
 * @since 5/14/17 5:33 PM
 */
internal class DiscordWebSocketListener(val discord: Discord) : WebSocketListener() {
	private val handlers = WebSocketHandlerCache(helloHandler, ReadyHandler(), guildCreateHandler,
			MessageCreateHandler(), MessageDeleteHandler(), MessageUpdateHandler(), PresenceUpdateHandler(),
			ChannelCreateHandler(), GuildMemberUpdateHandler(), MessageReactionAddHandler(),
			MessageReactionRemoveHandler(), GuildRoleCreateHandler(), GuildRoleUpdateHandler(), ChannelUpdateHandler(),
			UserUpdateHandler(), GuildMemberRemoveHandler())

	var isConnected = false
	var lastSequence = 0L

	var sessionID: String? = null

	private lateinit var webSocket: WebSocket

	override fun onMessage(webSocket: WebSocket?, text: String?) {
		if (null != text) {
			try {
				discordLogger.trace("WebSocket message: {}", text.replace(discord.auth.token, "--token--"))
				val basicResponse = mapper.readValue(text, BasicWebSocketResponse::class.java)
				lastSequence = maxOf(basicResponse.sequenceNumber, lastSequence)

				handlers.filter {
					it.doesHandle(basicResponse, discord)
				}.forEach {
					try {
						val s = mapper.readTree(text)
						if (s is ObjectNode && s.has("d")) {
							val d = s.path("d")
							it.handle(it.handledClass.cast(mapper.readValue(d.toString(), it.handledClass)), discord)
						} else {
							discordLogger.error("Error deserializing event ${basicResponse.type} (no data object?)")
						}
					} catch (e: Exception) {
						discordLogger.error("Exception caught while handling event ${basicResponse.type}", e)
					}
				}
			} catch (e: Exception) {
				discordLogger.error("Exception caught handling websocket message", e)
			}
		}
	}

	override fun onOpen(webSocket: WebSocket?, response: Response?) {
		if (null != webSocket) {
			this.webSocket = webSocket
			if (this.sessionID != null || !this.resume()) {
				this.identify()
			}
			// todo sharding and compression
			this.isConnected = true
			discordLogger.info("WebSocket connected.")
		}
	}

	override fun onFailure(webSocket: WebSocket?, t: Throwable?, response: Response?) {
		discordLogger.error("WebSocket failure", t)
		this.webSocket.close(1000, "")
		this.isConnected = false
		//todo discord.connect() //lol
	}

	override fun onClosed(webSocket: WebSocket?, code: Int, reason: String?) {
		discordLogger.info("WebSocket connection closed with code {} ({}) and reason {}",
				code, DiscordDisconnectCodes.forCode(code.toShort())?.name ?: "Unknown code", reason)
		isConnected = false
	}

	private fun identify(payload: IdentifyPayload = IdentifyPayload(discord.auth.token)) {
		this.lastSequence = 0
		this.send(WebSocketRequest(DiscordOpCodes.IDENTIFY, IdentifyPayload(discord.auth.token)))
	}

	private fun resume(): Boolean {
		val session = sessionID ?: return false
		this.send(WebSocketRequest(DiscordOpCodes.RESUME, GatewayResumeRequest(discord.auth.token, session,
				lastSequence)))
		return true
	}

	fun send(wsr: WebSocketRequest<*>) {
		send(mapper.writeValueAsString(wsr))
	}

	fun send(str: String) {
		discordLogger.trace("Sending WebSocket payload: {}", str.replace(discord.auth.token, "--token--"))
		webSocket.send(str)
	}

	// todo allow user to customize these values
	class IdentifyPayload(@JsonProperty("token") val token: String,
	                      @JsonProperty("properties") val properties: IdentifyProperties = IdentifyPayload.IdentifyProperties(),
	                      @JsonProperty("compress") val compress: Boolean = false,
	                      @JsonProperty("large_threshold") val largeThreshold: Int = 250) {

		class IdentifyProperties(@JsonProperty("\$os") val os: String = "linux",
		                         @JsonProperty("\$browser") val browser: String = "discord.kt",
		                         @JsonProperty("\$device") val device: String = "discord.kt",
		                         @JsonProperty("\$referrer") val referrer: String = "",
		                         @JsonProperty("\$referring_domain") val referringDomain: String = "")
	}
}