package io.github.nerd.discordkt.discord.model.response

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.nerd.discordkt.discord.model.base.Model
import io.github.nerd.discordkt.discord.model.entity.ModelRole
import io.github.nerd.discordkt.discord.model.entity.ModelUser

/**
 * @author ashley
 * @since 6/7/17 6:13 AM
 */
internal class GatewayResponse @JsonCreator constructor(@JsonProperty("url") val url: String)

class HelloModel @JsonCreator constructor(@JsonProperty("heartbeat_interval") val heartbeatInterval: Long,
                                          @JsonProperty("_trace") val connectedServers: Array<String>) : Model

internal class GuildRoleResponseModel(@JsonProperty("role") val role: ModelRole,
                                      @JsonProperty("guild_id") val guildID: Long) : Model

internal class ReactionEventDataModel(@JsonProperty("user_id") val userID: Long,
                                      @JsonProperty("message_id") val messageID: Long,
                                      @JsonProperty("channel_id") val channelID: Long,
                                      @JsonProperty("emoji") val emoji: BasicEmojiModel) : Model {

	internal class BasicEmojiModel(@JsonProperty("name") val name: String,
	                               @JsonProperty("id") val id: Long?) : Model
}

@JsonIgnoreProperties(ignoreUnknown = true)
internal class AuditLogResponse(@JsonProperty("users") val users: List<ModelUser>,
                                @JsonProperty("audit_log_entries") val entries: List<AuditLogEntryModel>) : Model

internal class AuditLogEntryModel(@JsonProperty("target_id") val targetID: Long?,
                                  @JsonProperty("user_id") val userID: Long,
                                  @JsonProperty("changes") val changes: List<AuditLogChange>,
                                  @JsonProperty("action_type") val actionType: Int) : Model {

	internal class AuditLogChange(@JsonProperty("new_value") val newVal: String?,
	                              @JsonProperty("old_value") val oldVal: String?,
	                              @JsonProperty("key") val key: String) : Model
}