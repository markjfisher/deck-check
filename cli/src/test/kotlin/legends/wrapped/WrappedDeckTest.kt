package legends.wrapped

import legends.Card
import tesl.model.Deck
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class WrappedDeckTest {

    @Test
    fun `hasCard matches a name in lowercase`() {
        val nord1 = Card(name = "Nord Card 1", subtypes = listOf("Nord")).createCard()
        val nord2 = Card(name = "Nord Card 2", subtypes = listOf("Nord")).createCard()
        val vamp = Card(name = "Vampire Card", subtypes = listOf("Vampire")).createCard()
        val orc = Card(name = "Orc Card 1", subtypes = listOf("Orc")).createCard()

        val deck = Deck(cards = listOf(nord1, nord2, vamp, orc))
        val wd = WrappedDeck(deck)

        assertThat(wd.hasCard("nord card 1")).isTrue()
        assertThat(wd.hasCard("nord CARD 1")).isTrue()
        assertThat(wd.hasCard("nord card 3")).isFalse()
    }

    @Test
    fun `countCard matches a name in lowercase`() {
        val nord1 = Card(name = "Nord Card 1", subtypes = listOf("Nord")).createCard()
        val nord2 = Card(name = "Nord Card 2", subtypes = listOf("Nord")).createCard()

        val deck = Deck(cards = listOf(nord1, nord1, nord2))
        val wd = WrappedDeck(deck)

        assertThat(wd.cardCount("nord card 1")).isEqualTo(2)
        assertThat(wd.cardCount("NORD card 1")).isEqualTo(2)
        assertThat(wd.cardCount("nord CARD 2")).isEqualTo(1)
        assertThat(wd.cardCount("nord card 3")).isEqualTo(0)
    }

    @Test
    fun `of gives list of unique cards for that count`() {
        val nord1 = Card(name = "Nord Card 1", subtypes = listOf("Nord"), id = "1").createCard()
        val nord2 = Card(name = "Nord Card 2", subtypes = listOf("Nord"), id = "2").createCard()

        val deck = Deck(cards = listOf(nord1, nord1, nord2))
        val wd = WrappedDeck(deck)

        assertThat(wd.of(0)).isEmpty()
        assertThat(wd.of(1)).containsExactlyInAnyOrder(nord2)
        assertThat(wd.of(2)).containsExactlyInAnyOrder(nord1)
        assertThat(wd.of(3)).isEmpty()
    }
}