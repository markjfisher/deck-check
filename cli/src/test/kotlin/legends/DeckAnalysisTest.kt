package legends

import io.elderscrollslegends.Card
import io.elderscrollslegends.CardCache
import io.elderscrollslegends.Deck
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class DeckAnalysisTest {
    @Test
    fun `rarity counts`() {
        val commonCards = listOf(Card(name = "common card 1", rarity = "Common"))
        val rareCards = listOf(Card(name = "common card 2", rarity = "Rare"))
        val epicCards = listOf(Card(name = "common card 3", rarity = "Epic"))
        val legendaryCards = listOf(Card(name = "common card 3", rarity = "Legendary"))

        val deck = Deck(cards = listOf(commonCards, rareCards, epicCards, legendaryCards).flatten())
        val da = DeckAnalysis(deck)
        assertThat(da.byRarity["Common"]).isEqualTo(commonCards)
        assertThat(da.byRarity["Rare"]).isEqualTo(rareCards)
        assertThat(da.byRarity["Epic"]).isEqualTo(epicCards)
        assertThat(da.byRarity["Legendary"]).isEqualTo(legendaryCards)
    }

    @Test
    fun `all subtypes of a deck`() {
        val nord1 = Card(name = "nord card 1", subtypes = listOf("Nord"))
        val nord2 = Card(name = "nord card 2", subtypes = listOf("Nord"))
        val vamp = Card(name = "vampire card 1", subtypes = listOf("Vampire"))
        val charus = Card(name = "Chaurus card 1", subtypes = listOf("Chaurus"))

        val deck = Deck(cards = listOf(nord1, nord2, vamp, charus))
        val da = DeckAnalysis(deck)

        assertThat(da.subtypes).containsAll(listOf("Nord", "Vampire", "Chaurus"))
    }

    @Test
    @Disabled("Runs against live API")
    fun `missing cards test`() {
        CardCache.load()
        val deck = Deck.importCode("SPABvMAAAA")
        val da = DeckAnalysis(deck)

        assertThat(da.creatureCount).isEqualTo(1)
    }

    // TODO: more tests
}