import com.jessecorbett.diskord.api.websocket.events.CreatedGuild
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class GuildsTest {

    @Disabled("not working as emoji fail")
    @Test
    fun `check guild created message can be deserilized`() {
        val json = String(this::class.java.getResource("/guilds.json").readBytes())
        val createdGuild = Json.nonstrict.parse(CreatedGuild.serializer(), json)
        Assertions.assertThat(createdGuild).isNotNull
    }

}