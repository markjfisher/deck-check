package io.github.nerd.discordkt.discord.api

import io.github.nerd.discordkt.discord.model.channel.ModelPrivateChannel
import io.github.nerd.discordkt.discord.model.entity.ModelInvite
import io.github.nerd.discordkt.discord.model.entity.ModelMessage
import io.github.nerd.discordkt.discord.model.entity.ModelOAuthApplication
import io.github.nerd.discordkt.discord.model.request.*
import io.github.nerd.discordkt.discord.model.response.AuditLogResponse
import io.github.nerd.discordkt.discord.model.response.GatewayResponse
import okhttp3.MultipartBody
import retrofit2.http.*
import java.util.concurrent.CompletableFuture


/**
 * @author ashley
 * @since 5/24/17 11:22 PM
 */
internal interface DiscordAPI {
	@GET("gateway")
	fun obtainGateway(): CompletableFuture<GatewayResponse>

	@POST("channels/{channelID}/messages")
	@Multipart
	fun sendMessage(@Path("channelID") channelID: Long,
	                @Part(encoding = "UTF-8") payload: MultipartBody.Part,
	                @Part files: Array<MultipartBody.Part?>): CompletableFuture<ModelMessage>

	@DELETE("channels/{channelID}/messages/{messageID}")
	fun deleteMessage(@Path("channelID") channelID: Long,
	                  @Path("messageID") messageID: Long): CompletableFuture<Void>

	@PATCH("channels/{channelID}/messages/{messageID}")
	fun editMessage(@Path("channelID") channelID: Long,
	                @Path("messageID") messageID: Long,
	                @Body req: MessageEditRequest): CompletableFuture<Void>

	@GET("users/@me/channels")
	fun fetchPrivateChannels(): CompletableFuture<Array<ModelPrivateChannel>>

	@POST("users/@me/channels")
	fun createPrivateChannel(@Body req: PrivateChannelCreateRequest): CompletableFuture<ModelPrivateChannel>

	@GET("channels/{channelID}/messages/{messageID}")
	fun fetchMessage(@Path("channelID") channelID: Long,
	                 @Path("messageID") messageID: Long): CompletableFuture<ModelMessage>

	@PUT("guilds/{guildID}/bans/{userID}")
	fun banUser(@Path("guildID") guildID: Long,
	            @Path("userID") userID: Long,
	            @Query("delete-message-days") daysToClear: Int,
	            @Query("reason") reason: String): CompletableFuture<Void>

	@DELETE("guilds/{guildID}/bans/{userID}")
	fun unbanUser(@Path("guildID") guildID: Long,
	              @Path("userID") userID: Long): CompletableFuture<Void>

	@GET("channels/{channelID}/messages")
	fun fetchMessages(@Path("channelID") channelID: Long,
	                  @Query("limit") count: Int): CompletableFuture<List<ModelMessage>>

	@PATCH("guilds/{guildID}/members/{userID}/nick")
	fun changeNickname(@Path("guildID") guildID: Long,
	                   @Path("userID") userID: String,
	                   @Body nick: NicknameChangeRequestModel): CompletableFuture<Void>

	@PUT("channels/{channelID}/messages/{messageID}/reactions/{emoji}/@me")
	fun addReaction(@Path("channelID") channelID: Long,
	                @Path("messageID") messageID: Long,
	                @Path("emoji") emoji: String): CompletableFuture<Void>

	@DELETE("channels/{channelID}/messages/{messageID}/reactions/{emoji}/{userID}")
	fun deleteReaction(@Path("channelID") channelID: Long,
	                   @Path("messageID") messageID: Long,
	                   @Path("emoji") emoji: String,
	                   @Path("userID") userID: String): CompletableFuture<Void>

	@DELETE("channels/{channelID}/messages/{messageID}/reactions}")
	fun deleteAllReactions(@Path("channelID") channelID: Long,
	                       @Path("messageID") messageID: Long): CompletableFuture<Void>

	@PUT("channels/{channelID}/pins/{messageID}")
	fun pinMessage(@Path("channelID") channelID: Long,
	               @Path("messageID") messageID: Long): CompletableFuture<Void>

	@DELETE("channels/{channelID}/pins/{messageID}")
	fun unpinMessage(@Path("channelID") channelID: Long,
	                 @Path("messageID") messageID: Long): CompletableFuture<Void>

	@POST("channels/{channelID}/messages/bulk-delete")
	fun bulkDelete(@Path("channelID") channelID: Long,
	               @Body model: BulkDeleteRequestModel): CompletableFuture<Void>

	@GET("oauth2/applications/@me")
	fun getApplicationInfo(): CompletableFuture<ModelOAuthApplication>

	@PATCH("users/@me")
	fun updateUser(@Body updateRequest: UserUpdateRequest): CompletableFuture<Void>

	@DELETE("users/@me/guilds/{guildID}")
	fun leaveGuild(@Path("guildID") guildID: Long): CompletableFuture<Void>

	@DELETE("guilds/{guildID}/members/{userID}")
	fun kickMember(@Path("guildID") guildID: Long,
	               @Path("userID") userID: Long): CompletableFuture<Void>

	@GET("invites/{inviteID}")
	fun getInvite(@Path("inviteID") inviteID: String): CompletableFuture<ModelInvite>

	@POST("invites/{inviteID}")
	fun acceptInvite(@Path("inviteID") inviteID: String): CompletableFuture<Void>

	@GET("guilds/{guildID}/audit-logs")
	fun getAuditLogs(): CompletableFuture<AuditLogResponse>

	@PATCH("guilds/{guildID}/channels")
	fun updateChannel(@Path("guildID") guildID: Long, @Body updateRequest: GuildChannelUpdateRequest): CompletableFuture<Void>
}