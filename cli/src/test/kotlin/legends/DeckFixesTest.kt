package legends

import io.elderscrollslegends.Card
import io.elderscrollslegends.Deck
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DeckFixesTest {
    @Test
    fun `Moon Bishop is a khajiit`() {
        val cards = listOf(Card(name = "Moon Bishop"))
        val fixedDeck = DeckFixes.fix(Deck(cards))
        val card = fixedDeck.cards.first()
        assertThat(card.name).isEqualTo("Moon Bishop")
        assertThat(card.subtypes).containsOnly("Khajiit")
    }

    @Test
    fun `Gavel is a rare`() {
        val cards = listOf(Card(name = "Gavel of the Ordinator"))
        val fixedDeck = DeckFixes.fix(Deck(cards))
        val card = fixedDeck.cards.first()
        assertThat(card.name).isEqualTo("Gavel of the Ordinator")
        assertThat(card.soulSummon).isEqualTo("100")
        assertThat(card.soulTrap).isEqualTo("20")
        assertThat(card.rarity).isEqualTo("Rare")
    }

    @Test
    fun `Suthay Bootlegger is a khajiit`() {
        val cards = listOf(Card(name = "Suthay Bootlegger"))
        val fixedDeck = DeckFixes.fix(Deck(cards))
        val card = fixedDeck.cards.first()
        assertThat(card.name).isEqualTo("Suthay Bootlegger")
        assertThat(card.subtypes).containsOnly("Khajiit")
    }
}