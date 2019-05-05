package legends.events

import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import legends.model.Member

@Serializable
data class TypingStart(
    @SerialName("user_id") val userId: String,
    @SerialName("timestamp") val timestamp: String,
    @SerialName("channel_id") val channelId: String,
    @SerialName("member") val member: Member,
    @SerialName("nick") val nick: String? = "",
    @SerialName("roles") val roles: List<String>? = emptyList(),
    @SerialName("mute") val mute: Boolean? = false,
    @SerialName("joined_at") val joinedAt: String? = "",
    @Optional @SerialName("guild_id") val guildId: String? = null
)
