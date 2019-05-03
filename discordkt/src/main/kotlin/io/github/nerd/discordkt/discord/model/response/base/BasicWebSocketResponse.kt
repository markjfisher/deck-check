package io.github.nerd.discordkt.discord.model.response.base

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author ashley
 * @since 5/14/17 10:17 PM
 */
@JsonIgnoreProperties(ignoreUnknown = true)
internal open class BasicWebSocketResponse {
	@JsonProperty("t") val type: String? = null
	@JsonProperty("s") val sequenceNumber: Long = -1
	@JsonProperty("op") val opCode: Byte = -1
}