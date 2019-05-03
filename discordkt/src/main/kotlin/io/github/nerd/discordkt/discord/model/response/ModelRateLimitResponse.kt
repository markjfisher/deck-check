package io.github.nerd.discordkt.discord.model.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.nerd.discordkt.discord.model.base.Model

/**
 * @author ashley
 * @since 6/1/17 11:21 PM
 */
class ModelRateLimitResponse(@JsonProperty("message") val message: String,
                             @JsonProperty("retry_after") val retryAfter: Long,
                             @JsonProperty("global") val global: Boolean) : Model