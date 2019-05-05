import com.jessecorbett.diskord.api.model.Role
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RolesTest {

    @Test
    fun `check roles can be deserilized`() {
        val json = String(this::class.java.getResource("/test-roles.json").readBytes())
        val roles = Json.parse(Roles.serializer(), json)
        assertThat(roles.roles.size).isEqualTo(16)
    }
}

@Serializable
data class Roles(
    @SerialName("roles") val roles: List<Role>
)