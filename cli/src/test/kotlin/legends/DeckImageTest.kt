package legends

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DeckImageTest {

    @Test
    fun `column lengths for given counts`() {
        assertThat(DeckImage.calculateColumnLengths(1,4)).isEqualTo(listOf(1, 0, 0, 0))
        assertThat(DeckImage.calculateColumnLengths(2,4)).isEqualTo(listOf(1, 1, 0, 0))
        assertThat(DeckImage.calculateColumnLengths(3,4)).isEqualTo(listOf(1, 1, 1, 0))
        assertThat(DeckImage.calculateColumnLengths(4,4)).isEqualTo(listOf(1, 1, 1, 1))
        assertThat(DeckImage.calculateColumnLengths(5,4)).isEqualTo(listOf(2, 1, 1, 1))
        assertThat(DeckImage.calculateColumnLengths(25,3)).isEqualTo(listOf(9, 8, 8))
    }
}