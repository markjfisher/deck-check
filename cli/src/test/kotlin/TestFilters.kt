import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TestFilters {
    @Test
    fun `filters chars numbers and fullstop`() {
        assertThat("_-=+abc123 456/XYZ™.png".filter { it.isLetterOrDigit() || it == '.' || it == '-' }).isEqualTo("-abc123456XYZ.png")
    }
}