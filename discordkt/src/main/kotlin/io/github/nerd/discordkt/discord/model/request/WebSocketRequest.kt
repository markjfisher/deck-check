package io.github.nerd.discordkt.discord.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.nerd.discordkt.discord.util.DiscordOpCodes

/**
 * @author ashley
 * @since 5/22/17 5:42 PM
 */
internal class WebSocketRequest<out T>(@JsonProperty("op") val opCode: DiscordOpCodes, @JsonProperty("d") val data: T)