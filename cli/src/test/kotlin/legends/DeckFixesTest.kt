package legends

import io.elderscrollslegends.Card
import io.elderscrollslegends.Deck
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DeckFixesTest {
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
    fun `khajiit subtypes`() {
        val cards = listOf(
            Card(name = "Moon Bishop"),
            Card(name = "Seeker of the Black Arts"),
            Card(name = "Rebellion General"),
            Card(name = "Suthay Bootlegger"),
            Card(name = "Pouncing Senche"),
            Card(name = "Queen's Captain")
        )
        val fixedDeck = DeckFixes.fix(Deck(cards))
        assertThat(fixedDeck.cards[0].name).isEqualTo("Moon Bishop")
        assertThat(fixedDeck.cards[1].name).isEqualTo("Seeker of the Black Arts")
        assertThat(fixedDeck.cards[2].name).isEqualTo("Rebellion General")
        assertThat(fixedDeck.cards[3].name).isEqualTo("Suthay Bootlegger")
        assertThat(fixedDeck.cards[4].name).isEqualTo("Pouncing Senche")
        assertThat(fixedDeck.cards[5].name).isEqualTo("Queen's Captain")
        assertThat(fixedDeck.cards.flatMap { it.subtypes }.toHashSet()).containsOnly("Khajiit")
    }

    @Test
    fun `imperial subtypes`() {
        val cards = listOf(
            Card(name = "Gravesinger"),
            Card(name = "Cauldron Keeper")
        )
        val fixedDeck = DeckFixes.fix(Deck(cards))
        assertThat(fixedDeck.cards[0].name).isEqualTo("Gravesinger")
        assertThat(fixedDeck.cards[0].keywords).isEmpty()
        assertThat(fixedDeck.cards[1].name).isEqualTo("Cauldron Keeper")
        assertThat(fixedDeck.cards.flatMap { it.subtypes }.toHashSet()).containsOnly("Imperial")
    }

    @Test
    fun `Dark Elf subtypes`() {
        val cards = listOf(
            Card(name = "Brotherhood Assassin"),
            Card(name = "Imposter")
        )
        val fixedDeck = DeckFixes.fix(Deck(cards))
        assertThat(fixedDeck.cards[0].name).isEqualTo("Brotherhood Assassin")
        assertThat(fixedDeck.cards[1].name).isEqualTo("Imposter")
        assertThat(fixedDeck.cards.flatMap { it.subtypes }.toHashSet()).containsOnly("Dark Elf")
    }

    @Test
    fun `Redguard subtypes`() {
        val cards = listOf(
            Card(name = "Master Swordsmith")
        )
        val fixedDeck = DeckFixes.fix(Deck(cards))
        assertThat(fixedDeck.cards[0].name).isEqualTo("Master Swordsmith")
        assertThat(fixedDeck.cards.flatMap { it.subtypes }.toHashSet()).containsOnly("Redguard")
    }

    @Test
    fun `Breton subtypes`() {
        val cards = listOf(
            Card(name = "Vigilant of Stendarr")
        )
        val fixedDeck = DeckFixes.fix(Deck(cards))
        assertThat(fixedDeck.cards[0].name).isEqualTo("Vigilant of Stendarr")
        assertThat(fixedDeck.cards.flatMap { it.subtypes }.toHashSet()).containsOnly("Breton")
    }

    @Test
    fun `Action type`() {
        val cards = listOf(
            Card(name = "Transmogrify")
        )
        val fixedDeck = DeckFixes.fix(Deck(cards))
        assertThat(fixedDeck.cards[0].name).isEqualTo("Transmogrify")
        assertThat(fixedDeck.cards.map { it.type }.toHashSet()).containsOnly("Action")
    }

    @Test
    fun `Item type`() {
        val cards = listOf(
            Card(name = "Duskfang")
        )
        val fixedDeck = DeckFixes.fix(Deck(cards))
        assertThat(fixedDeck.cards[0].name).isEqualTo("Duskfang")
        assertThat(fixedDeck.cards.map { it.type }.toHashSet()).containsOnly("Item")
    }

}