package io.github.nerd.discordkt.discord.model.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.nerd.discordkt.discord.model.Embed
import io.github.nerd.discordkt.discord.model.base.Model
import io.github.nerd.discordkt.discord.util.Status

/**
 * @author ashley
 * @since 6/4/17 1:35 AM
 */
internal class BulkDeleteRequestModel(@JsonProperty("messages") val messageIDs: Array<Long>) : Model

internal class NicknameChangeRequestModel(@JsonProperty("nick") val nick: String) : Model

@JsonInclude(JsonInclude.Include.NON_NULL)
internal class MessageEditRequest(@JsonProperty("content") val content: String? = null,
                                  @JsonProperty("embed") val embed: Embed? = null) : Model

internal class PresenceChangeRequest(@JsonProperty("status") val status: Status,
                                     @JsonProperty("since") val since: Long,
                                     @JsonProperty("game") val game: Game?,
                                     @JsonProperty("afk") val afk: Boolean) : Model {

	internal class Game(@JsonProperty("name") val name: String,
	                    @JsonProperty("type") val type: Byte,
	                    @JsonProperty("url") val streamURL: String?) : Model
}

@JsonInclude(JsonInclude.Include.NON_NULL)
internal class UserUpdateRequest(@JsonProperty("username") val username: String?,
                                 @JsonProperty("avatar") val avatarData: String?) : Model

internal class GatewayResumeRequest(@JsonProperty("token") val token: String,
                                    @JsonProperty("session_id") val sessionID: String,
                                    @JsonProperty("seq") val sequence: Long) : Model

internal class PrivateChannelCreateRequest(@JsonProperty("recipients") val recipients: Array<Long>) : Model

internal class GuildChannelUpdateRequest(@JsonProperty("id") val id: Long,
                                         @JsonProperty("position") val position: Int,
                                         @JsonProperty("parent_id") val parentID: Long?) : Model