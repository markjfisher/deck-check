package io.github.nerd.discordkt.discord.model.channel

import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.entity.channel.DirectPrivateChannel
import io.github.nerd.discordkt.discord.entity.channel.GroupPrivateChannel
import io.github.nerd.discordkt.discord.entity.channel.PrivateChannel
import io.github.nerd.discordkt.discord.entity.guild.Guild
import io.github.nerd.discordkt.discord.entity.guild.channel.*
import io.github.nerd.discordkt.discord.model.base.Model
import io.github.nerd.discordkt.discord.model.entity.ModelUser
import io.github.nerd.discordkt.discord.util.Permissions
import io.github.nerd.discordkt.discord.util.PermissionsDeserializer
import io.github.nerd.discordkt.discordLogger

/**
 * @author ashley
 * @since 6/4/17 1:43 AM
 *
 * annotation hell
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
		JsonSubTypes.Type(ModelGuildTextChannel::class, name = "0"),
		JsonSubTypes.Type(ModelDirectPrivateChannel::class, name = "1"),
		JsonSubTypes.Type(ModelGuildVoiceChannel::class, name = "2"),
		JsonSubTypes.Type(ModelGroupPrivateChannel::class, name = "3"),
		JsonSubTypes.Type(ModelGuildChannelCategory::class, name = "4")
)
open internal class ModelChannel(val id: Long, val type: Byte) : Model

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
		JsonSubTypes.Type(ModelDirectPrivateChannel::class, name = "1"),
		JsonSubTypes.Type(ModelGroupPrivateChannel::class, name = "3")
)
@JsonIgnoreProperties(ignoreUnknown = true)
abstract internal class ModelPrivateChannel
@JsonCreator
constructor(@JsonProperty("id") id: Long,
            @JsonProperty("recipients") val recipients: List<ModelUser>,
            @JsonProperty("last_message_id") val lastMessageID: Long,
            type: Byte) : ModelChannel(id, type) {
	abstract fun toEntity(discord: Discord): PrivateChannel
}

@JsonIgnoreProperties(ignoreUnknown = true)
internal class ModelDirectPrivateChannel
@JsonCreator
constructor(@JsonProperty("id") id: Long,
            @JsonProperty("recipients") recipients: List<ModelUser>,
            @JsonProperty("last_message_id") lastMessageID: Long) : ModelPrivateChannel(id, recipients, lastMessageID, 1) {
	override fun toEntity(discord: Discord) = DirectPrivateChannel(id, recipients.first().toEntity(), discord)
}

@JsonIgnoreProperties(ignoreUnknown = true)
internal class ModelGroupPrivateChannel
@JsonCreator
constructor(@JsonProperty("id") id: Long,
            @JsonProperty("recipients") recipients: List<ModelUser>,
            @JsonProperty("last_message_id") lastMessageID: Long) : ModelPrivateChannel(id, recipients, lastMessageID, 3) {
	override fun toEntity(discord: Discord) = GroupPrivateChannel(id, recipients.map { it.toEntity() }, discord)
}


@JsonIgnoreProperties(ignoreUnknown = true)
internal class ModelPermissionOverwrite
@JsonCreator
constructor(@JsonProperty("id") val id: Long,
            @JsonProperty("type") val type: String,
            @JsonProperty("allow") @JsonDeserialize(using = PermissionsDeserializer::class) val allowedPermissions: Array<Permissions>,
            @JsonProperty("deny") @JsonDeserialize(using = PermissionsDeserializer::class) val deniedPermissions: Array<Permissions>)

@JsonIgnoreProperties(ignoreUnknown = true)
internal open class ModelGuildChannel
@JsonCreator
constructor(@JsonProperty("id") id: Long,
            @JsonProperty("name") var name: String,
            @JsonProperty("position") var position: Short,
            @JsonProperty("guild_id") val guildID: Long,
            @JsonProperty("permission_overwrites") val permissionOverwrites: List<ModelPermissionOverwrite>?,
            @JsonProperty("parent_id") val parentID: Long?,
            type: Byte) : ModelChannel(id, type) {

	fun toEntity(guild: Guild, discord: Discord): GuildChannel? {
		val permissionOverwriteEntities = arrayListOf<PermissionOverwrite<*>>()
		this.permissionOverwrites?.forEach {
			if (it.type.toLowerCase().contentEquals("member")) {
				val member = guild.members[it.id]
				if (null != member)
					permissionOverwriteEntities.add(UserPermissionOverwrite(member,
							it.allowedPermissions.toList(), it.deniedPermissions.toList()))
			} else if (it.type.toLowerCase().contentEquals("role")) {
				val role = guild.roles[it.id]
				if (null != role)
					permissionOverwriteEntities.add(RolePermissionOverwrite(role,
							it.allowedPermissions.toList(), it.deniedPermissions.toList()))
			} else {
				discordLogger.debug("Unknown permission overwrite type \"{}\"", it.type)
			}
		}
		return when {
			this is ModelGuildTextChannel -> GuildTextChannel(this.id, this.topic,
					GuildChannelImpl(this.id, this.name, this.position, guild, permissionOverwriteEntities), discord)
			this is ModelGuildVoiceChannel -> // todo add support for voice channels and such
				null
			this is ModelGuildChannelCategory -> ChannelCategory(this.id, this.name, this.position, guild, permissionOverwriteEntities)
			else -> null
		}
	}
}

@JsonIgnoreProperties(ignoreUnknown = true)
internal open class ModelGuildChannelCategory
@JsonCreator
constructor(@JsonProperty("id") id: Long,
            @JsonProperty("name") name: String,
            @JsonProperty("position") position: Short,
            @JsonProperty("guild_id") guildID: Long,
            @JsonProperty("permission_overwrites") permissionOverwrites: List<ModelPermissionOverwrite>?,
            @JsonProperty("parent_id") parentID: Long?) :
		ModelGuildChannel(id, name, position, guildID, permissionOverwrites, parentID, 4)

internal class ModelGuildTextChannel
@JsonCreator
constructor(@JsonProperty("topic") val topic: String?,
            @JsonProperty("last_message_id") val lastMessageID: Long,
            @JsonProperty("id") id: Long,
            @JsonProperty("name") name: String,
            @JsonProperty("position") position: Short,
            @JsonProperty("guild_id") guildID: Long,
            @JsonProperty("permission_overwrites") permissionOverwrites: List<ModelPermissionOverwrite>?,
            @JsonProperty("parent_id") parentID: Long?) :
		ModelGuildChannel(id, name, position, guildID, permissionOverwrites, parentID, 0)

internal class ModelGuildVoiceChannel
@JsonCreator
constructor(@JsonProperty("id") id: Long,
            @JsonProperty("name") name: String,
            @JsonProperty("position") position: Short,
            @JsonProperty("bitrate") val bitrate: Int,
            @JsonProperty("guild_id") guildID: Long,
            @JsonProperty("permission_overwrites") permissionOverwrites: List<ModelPermissionOverwrite>,
            @JsonProperty("user_limit") val userLimit: Int,
            @JsonProperty("parent_id") parentID: Long?) :
		ModelGuildChannel(id, name, position, guildID, permissionOverwrites, parentID, 2)