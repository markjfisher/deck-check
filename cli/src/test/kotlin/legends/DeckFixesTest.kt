package legends

import io.elderscrollslegends.Card
import io.elderscrollslegends.Deck
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DeckFixesTest {
    @Test
    fun `Moonmoth Castellan is a nord`() {
        val cards = listOf(Card(name = "Moonmoth Castellan", rarity = "Common", subtypes = listOf("Imperial")))
        val fixedDeck = DeckFixes.fix(Deck(cards))
        val card = fixedDeck.cards.first()
        assertThat(card.name).isEqualTo("Moonmoth Castellan")
        assertThat(card.subtypes).containsOnly("Nord")
    }
}