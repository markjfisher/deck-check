import com.jessecorbett.diskord.api.model.Emoji
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class EmojisTest {

    @Test
    fun `check emoji with roles`() {
        val json = String(this::class.java.getResource("/emoji-with-role.json").readBytes())
        val emoji = Json.nonstrict.parse(Emoji.serializer(), json)
        Assertions.assertThat(emoji).isNotNull
    }

    @Test
    fun `check emoji without roles`() {
        val json = String(this::class.java.getResource("/emoji-empty-roles.json").readBytes())
        val emoji = Json.nonstrict.parse(Emoji.serializer(), json)
        Assertions.assertThat(emoji).isNotNull
    }
}

