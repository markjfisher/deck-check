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
    fun `creatures actions items supports are filtered correctly`() {
        val legendaryCreature = Card(name = "creature 1", subtypes = listOf("Nord"), type = "Creature", rarity = "Legendary")
        val commonAction1 = Card(name = "common action 1", type = "Action", rarity = "Common")
        val commonAction2 = Card(name = "common action 2", type = "Action", rarity = "Common")
        val rareAction1 = Card(name = "rare action 1", type = "Action", rarity = "Rare")
        val commonItem1 = Card(name = "common item 1", type = "Item", rarity = "Common")
        val commonItem2 = Card(name = "common item 2", type = "Item", rarity = "Common")
        val epicItem1 = Card(name = "epic item 1", type = "Item", rarity = "Epic")
        val rareSupport1 = Card(name = "rare support 1", type = "Support", rarity = "Rare")

        val deck = Deck(cards = listOf(legendaryCreature, commonAction1, commonAction1, commonAction2, rareAction1, commonItem1, commonItem2, commonItem2, epicItem1, rareSupport1, rareSupport1))
        val da = DeckAnalysis(deck)

        assertThat(da.creatures).containsOnly(legendaryCreature)
        assertThat(da.actions).containsOnly(commonAction1, commonAction2, rareAction1)
        assertThat(da.items).containsOnly(commonItem1, commonItem2, epicItem1)
        assertThat(da.supports).containsOnly(rareSupport1)

        assertThat(da.creaturesByRarity("Common")).isEmpty()
        assertThat(da.creaturesByRarity("Rare")).isEmpty()
        assertThat(da.creaturesByRarity("Epic")).isEmpty()
        assertThat(da.creaturesByRarity("Legendary")).containsOnly("creature 1")

        assertThat(da.actionsByRarity("Common")).containsOnly("common action 1", "common action 2")
        assertThat(da.actionsByRarity("Rare")).containsOnly("rare action 1")
        assertThat(da.actionsByRarity("Epic")).isEmpty()
        assertThat(da.actionsByRarity("Legendary")).isEmpty()

        assertThat(da.itemsByRarity("Common")).containsOnly("common item 1", "common item 2")
        assertThat(da.itemsByRarity("Rare")).isEmpty()
        assertThat(da.itemsByRarity("Epic")).containsOnly("epic item 1")
        assertThat(da.itemsByRarity("Legendary")).isEmpty()

        assertThat(da.supportsByRarity("Common")).isEmpty()
        assertThat(da.supportsByRarity("Rare")).containsOnly("rare support 1")
        assertThat(da.supportsByRarity("Epic")).isEmpty()
        assertThat(da.supportsByRarity("Legendary")).isEmpty()

        assertThat(da.raritiesOfType("Creature")).containsOnly("Legendary")
        assertThat(da.raritiesOfType("Action")).containsOnly("Common", "Rare")
        assertThat(da.raritiesOfType("Item")).containsOnly("Common", "Epic")
        assertThat(da.raritiesOfType("Support")).containsOnly("Rare")
    }

    @Test
    fun `creaturesOfSubtype retrieves correct cards`() {
        val creature1 = Card(name = "creature 1", subtypes = listOf("Nord"), type = "Creature")
        val creature2 = Card(name = "creature 2", subtypes = listOf("Nord"), type = "Creature")
        val creature3 = Card(name = "creature 3", subtypes = listOf("Factotum"), type = "Creature")
        val creature4 = Card(name = "creature 4", subtypes = listOf("Vampire"), type = "Creature")
        val deck = Deck(cards = listOf(creature1, creature1, creature2, creature3, creature3, creature4))
        val da = DeckAnalysis(deck)

        assertThat(da.creaturesOfSubtype("Nord")).containsOnly("creature 1", "creature 2")
        assertThat(da.creaturesOfSubtype("Factotum")).containsOnly("creature 3")
        assertThat(da.creaturesOfSubtype("Vampire")).containsOnly("creature 4")
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