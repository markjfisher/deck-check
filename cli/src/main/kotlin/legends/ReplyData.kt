package legends

import com.jessecorbett.diskord.api.rest.Embed

data class ReplyData(
    val text: List<String>,
    val embed: Embed? = null
)