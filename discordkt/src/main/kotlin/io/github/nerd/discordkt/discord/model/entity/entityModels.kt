package io.github.nerd.discordkt.discord.model.entity

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.entity.*
import io.github.nerd.discordkt.discord.entity.channel.TextChannel
import io.github.nerd.discordkt.discord.entity.user.LocalUser
import io.github.nerd.discordkt.discord.entity.user.User
import io.github.nerd.discordkt.discord.model.Embed
import io.github.nerd.discordkt.discord.model.base.Model
import io.github.nerd.discordkt.discord.model.channel.ModelGuildChannel
import io.github.nerd.discordkt.discord.util.Permissions
import io.github.nerd.discordkt.discord.util.PermissionsDeserializer
import java.time.ZonedDateTime

/**
 * @author ashley
 * @since 6/4/17 1:38 AM
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
internal class ModelBasicMessage(@JsonProperty("content") val content: String,
                                 @JsonProperty("channel_id") internal val channelID: Long,
                                 @JsonProperty("tts") val tts: Boolean? = false,
                                 @JsonProperty("embed") val embed: Embed? = null) : Model {

	constructor(content: String,
	            guildTextChannel: TextChannel,
	            tts: Boolean? = false,
	            embed: Embed? = null) : this(content, guildTextChannel.id, tts, embed)
}

internal class ModelAttachment @JsonCreator constructor(@JsonProperty("id") val id: Long,
                                                        @JsonProperty("filename") val fileName: String,
                                                        @JsonProperty("size") val size: Int,
                                                        @JsonProperty("url") val url: String,
                                                        @JsonProperty("proxy_url") val proxiedURL: String,
                                                        @JsonProperty("height") val height: Int?,
                                                        @JsonProperty("width") val width: Int?) : Model

internal class ModelEmoji @JsonCreator constructor(@JsonProperty("names") val names: Array<String>,
                                                   @JsonProperty("surrogates") val surrogate: String,
                                                   @JsonProperty("hasDiversity") val isDiverse: Boolean? = false) : Model

internal class ModelGuildEmoji @JsonCreator constructor(@JsonProperty("id") val id: Long,
                                                        @JsonProperty("name") val name: String,
                                                        @JsonProperty("roles") val roles: Array<Long>,
                                                        @JsonProperty("require_colons") val requiresColons: Boolean,
                                                        @JsonProperty("managed") val isManaged: Boolean) : Model

internal class ModelGuildMember @JsonCreator constructor(@JsonProperty("user") val user: ModelUser,
                                                         @JsonProperty("roles") val roles: List<Long>,
                                                         @JsonProperty("mute") val mute: Boolean,
                                                         @JsonProperty("deaf") val deaf: Boolean,
                                                         @JsonProperty("nick") val nick: String?,
                                                         @JsonProperty("joined_at") var joinDate: ZonedDateTime) : Model

@JsonIgnoreProperties(ignoreUnknown = true)
internal class ModelIdentifiable @JsonCreator constructor(@JsonProperty("id") val id: Long) : Model

internal class ModelLocalUser @JsonCreator constructor(@JsonProperty("id") val id: Long,
                                                       @JsonProperty("username") val username: String,
                                                       @JsonProperty("discriminator") val discriminator: String?,
                                                       @JsonProperty("avatar") val avatar: String? = "https://cdn.discordapp.com/avatars/${id % 5}.png",
                                                       @JsonProperty("bot") val isBot: Boolean?,
                                                       @JsonProperty("verified") val verified: Boolean,
                                                       @JsonProperty("mfa_enabled") val mfaEnabled: Boolean,
                                                       @JsonProperty("email") val email: String?,
                                                       @JsonProperty("premium") val premium: Boolean?,
                                                       @JsonProperty("phone") val phone: String?,
                                                       @JsonProperty("mobile") val mobile: Boolean?,
                                                       @JsonProperty("flags") val flags: Int?) : Model {
	fun toEntity(discord: Discord) = LocalUser(this.id, this.username,
			this.discriminator ?: "", this.avatar ?: "", this.isBot ?: false, this.verified, this.mfaEnabled, this.email ?: "", discord)
}

@JsonIgnoreProperties(ignoreUnknown = true)
internal class ModelPresence @JsonCreator constructor(@JsonProperty("user") val user: ModelIdentifiable,
                                                      @JsonProperty("status") val status: String,
                                                      @JsonProperty("game") val game: ModelGame?) : Model {

    @JsonIgnoreProperties(ignoreUnknown = true)
	internal class ModelGame(@JsonProperty("name") val name: String,
	                         @JsonProperty("type") val type: Byte,
	                         @JsonProperty("id") val id: String,
	                         @JsonProperty("url") val streamURL: String?) : Model
}

@JsonIgnoreProperties(ignoreUnknown = true)
internal class ModelRole @JsonCreator constructor(@JsonProperty("position") val position: Short = -1,
                                                  @JsonProperty("permissions") @JsonDeserialize(using = PermissionsDeserializer::class) val permissions: Array<Permissions>,
                                                  @JsonProperty("name") val name: String = "",
                                                  @JsonProperty("mentionable") val mentionable: Boolean = false,
                                                  @JsonProperty("managed") val managed: Boolean = false,
                                                  @JsonProperty("id") val id: Long,
                                                  @JsonProperty("hoist") val hoist: Boolean = false,
                                                  @JsonProperty("color") val color: Int = -1) : Model

@JsonIgnoreProperties(ignoreUnknown = true)
internal class ModelGuild @JsonCreator constructor(@JsonProperty("id") val id: Long,
                                                   @JsonProperty("verification_level") val verificationLevel: Int?,
                                                   @JsonProperty("unavailable") val unavailable: Boolean,
                                                   @JsonProperty("splash") val splash: String?,
                                                   @JsonProperty("roles") val roles: ArrayList<ModelRole>?,
                                                   @JsonProperty("region") val region: String?,
                                                   @JsonProperty("presences") val presences: ArrayList<ModelPresence>?,
                                                   @JsonProperty("owner_id") val ownerID: Long?,
                                                   @JsonProperty("name") val name: String?,
                                                   @JsonProperty("mfa_level") val mfaLevel: Byte?,
                                                   @JsonProperty("members") val members: ArrayList<ModelGuildMember>?,
                                                   @JsonProperty("member_count") var memberCount: Int?,
                                                   @JsonProperty("channels") val channels: ArrayList<ModelGuildChannel>?,
                                                   @JsonProperty("emojis") val emojis: Array<ModelGuildEmoji>?,
                                                   @JsonProperty("icon") val icon: String?) : Model

@JsonIgnoreProperties(ignoreUnknown = true)
internal open class ModelUser @JsonCreator constructor(@JsonProperty("id") val id: Long,
                                                       @JsonProperty("username") val username: String?,
                                                       @JsonProperty("discriminator") val discriminator: String?,
                                                       @JsonProperty("avatar") val avatar: String?,
                                                       @JsonProperty("bot") val isBot: Boolean?) : Model {
	fun toEntity()
			= User(this.id, this.username ?: "", this.discriminator ?: "",
			this.avatar ?: "https://cdn.discordapp.com/avatars/${id % 5}.png", this.isBot ?: false)

}

@JsonIgnoreProperties(ignoreUnknown = true)
internal class ModelMessage @JsonCreator constructor(@JsonProperty("id") val id: Long,
                                                     @JsonProperty("channel_id") val channelID: Long,
                                                     @JsonProperty("author") val author: ModelUser?,
                                                     @JsonProperty("content") val content: String?,
                                                     @JsonProperty("tts") val tts: Boolean?,
                                                     @JsonProperty("timestamp") val timestamp: ZonedDateTime?,
                                                     @JsonProperty("edited_timestamp") val editedTime: ZonedDateTime?,
                                                     @JsonProperty("mention_everyone") val mentionsEveryone: Boolean?,
                                                     @JsonProperty("embeds") val embeds: Array<Embed>?) : Model {

	fun toEntity(discord: Discord) = Message(this.id, this.content ?: "",
			when {
				discord.users.has(this.author?.id ?: -1) -> discord.users[this.author?.id ?: -1]!!
				this.author == null -> discord.me
				else -> this.author.toEntity()
			},
			this.channelID, this.timestamp ?: ZonedDateTime.now(), this.editedTime, discord)
}

internal class ModelOAuthApplication @JsonCreator constructor(@JsonProperty("id") val id: Long,
                                                              @JsonProperty("name") val name: String,
                                                              @JsonProperty("icon") val icon: String?,
                                                              @JsonProperty("description") val description: String?,
                                                              @JsonProperty("rpc_origins") val rpcOriginUrls: Array<String>?,
                                                              @JsonProperty("bot_public") val isPublic: Boolean,
                                                              @JsonProperty("bot_require_code_grant") val requireFullCodeGrant: Boolean,
                                                              @JsonProperty("owner") val owner: ModelUser) : Model

internal class ModelInvite @JsonCreator constructor(@JsonProperty("code") val code: String,
                                                    @JsonProperty("guild") val guild: ModelGuild,
                                                    @JsonProperty("channel") val channel: ModelGuildChannel,
                                                    @JsonProperty("inviter") val inviter: ModelUser?) : Model {
	fun toEntity(discord: Discord) = Invite(code, InviteGuild(guild.id, guild.name ?: "", guild.splash, guild.icon),
			InviteChannel(channel.id, channel.name,
					if (channel.type.rem(2) == 0) InviteChannelType.TEXT else InviteChannelType.VOICE), discord.users.fromModel(inviter), discord)
}

internal class ModelInviteMetadata @JsonCreator constructor(@JsonProperty("inviter") val inviter: ModelUser,
                                                            @JsonProperty("uses") val timesUsed: Int,
                                                            @JsonProperty("max_uses") val maxUses: Int,
                                                            @JsonProperty("max_age") val timeRemaining: Int,
                                                            @JsonProperty("temporary") val temporaryMembership: Boolean,
                                                            @JsonProperty("created_at") val createdAt: ZonedDateTime,
                                                            @JsonProperty("revoked") val isRevoked: Boolean) : Model