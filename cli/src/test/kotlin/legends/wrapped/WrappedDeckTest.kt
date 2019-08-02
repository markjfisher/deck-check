package legends.wrapped

import io.elderscrollslegends.Card
import io.elderscrollslegends.Deck
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class WrappedDeckTest {

    @Test
    fun `hasCard matches a name in lowercase`() {
        val nord1 = Card(name = "Nord Card 1", subtypes = listOf("Nord"))
        val nord2 = Card(name = "Nord Card 2", subtypes = listOf("Nord"))
        val vamp = Card(name = "Vampire Card", subtypes = listOf("Vampire"))
        val orc = Card(name = "Orc Card 1", subtypes = listOf("Orc"))

        val deck = Deck(cards = listOf(nord1, nord2, vamp, orc))
        val wd = WrappedDeck(deck)

        assertThat(wd.hasCard("nord card 1")).isTrue()
        assertThat(wd.hasCard("nord CARD 1")).isTrue()
        assertThat(wd.hasCard("nord card 3")).isFalse()
    }

    @Test
    fun `countCard matches a name in lowercase`() {
        val nord1 = Card(name = "Nord Card 1", subtypes = listOf("Nord"))
        val nord2 = Card(name = "Nord Card 2", subtypes = listOf("Nord"))

        val deck = Deck(cards = listOf(nord1, nord1, nord2))
        val wd = WrappedDeck(deck)

        assertThat(wd.cardCount("nord card 1")).isEqualTo(2)
        assertThat(wd.cardCount("NORD card 1")).isEqualTo(2)
        assertThat(wd.cardCount("nord CARD 2")).isEqualTo(1)
        assertThat(wd.cardCount("nord card 3")).isEqualTo(0)
    }
}