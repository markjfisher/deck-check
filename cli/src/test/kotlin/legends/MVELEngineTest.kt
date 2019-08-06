package legends

import io.elderscrollslegends.Card
import io.elderscrollslegends.Deck
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MVELEngineTest {
    @Test
    fun `wrapped list rules`() {
        var failures: List<String>
        val tournament = Tournament(id = "t1")

        val nord1 = Card(name = "nord card 1", subtypes = listOf("Nord"))
        val nord2 = Card(name = "nord card 2", subtypes = listOf("Nord"))
        val vamp = Card(name = "vampire card 1", subtypes = listOf("Vampire"))
        val orc = Card(name = "Orc card 1", subtypes = listOf("Orc"))

        val deck1 = Deck(cards = listOf(nord1, nord2, vamp, orc))

        //////////////////////////////////////////
        // contains(item...)
        //////////////////////////////////////////

        // A single element
        tournament.rules.clear()
        tournament.rules.add("subtypes.contains('Vampire')")
        failures = MVELEngine.checkRules(tournament, deck1)
        assertThat(failures).isEmpty()

        // ... and subsets
        tournament.rules.clear()
        tournament.rules.add("subtypes.contains('Vampire', 'Nord')")
        failures = MVELEngine.checkRules(tournament, deck1)
        assertThat(failures).isEmpty()

        // ... or the full list
        tournament.rules.clear()
        tournament.rules.add("subtypes.contains('Vampire', 'Nord', 'Orc')")
        failures = MVELEngine.checkRules(tournament, deck1)
        assertThat(failures).isEmpty()

        // ... in any order
        tournament.rules.clear()
        tournament.rules.add("subtypes.contains('Nord', 'Orc', 'Vampire')")
        failures = MVELEngine.checkRules(tournament, deck1)
        assertThat(failures).isEmpty()

        // ... but not when not in set
        tournament.rules.clear()
        tournament.rules.add("subtypes.contains('Fish')")
        failures = MVELEngine.checkRules(tournament, deck1)
        assertThat(failures).containsOnly("subtypes.contains('Fish')")

        // ... or with ones that are included
        tournament.rules.clear()
        tournament.rules.add("subtypes.contains('Vampire', 'Fish')")
        failures = MVELEngine.checkRules(tournament, deck1)
        assertThat(failures).containsOnly("subtypes.contains('Vampire', 'Fish')")

        //////////////////////////////////////////
        // containsOnly(item...)
        //////////////////////////////////////////

        // Then contains only tests all elements exactly
        tournament.rules.clear()
        tournament.rules.add("subtypes.containsOnly('Vampire', 'Nord', 'Orc')")
        failures = MVELEngine.checkRules(tournament, deck1)
        assertThat(failures).isEmpty()

        // ... and doensn't matter about the order
        tournament.rules.clear()
        tournament.rules.add("subtypes.containsOnly('Nord', 'Orc', 'Vampire')")
        failures = MVELEngine.checkRules(tournament, deck1)
        assertThat(failures).isEmpty()

        // But all elements must be present
        tournament.rules.clear()
        tournament.rules.add("subtypes.containsOnly('Vampire', 'Nord')")
        failures = MVELEngine.checkRules(tournament, deck1)
        assertThat(failures).containsOnly("subtypes.containsOnly('Vampire', 'Nord')")

        // elements not in the list fail
        tournament.rules.clear()
        tournament.rules.add("subtypes.containsOnly('Fish', 'Nord', 'Vampire', 'Orc')")
        failures = MVELEngine.checkRules(tournament, deck1)
        assertThat(failures).containsOnly("subtypes.containsOnly('Fish', 'Nord', 'Vampire', 'Orc')")

    }

    @Test
    fun `by rarity tests`() {
        val legendaryCreature = Card(name = "creature 1", subtypes = listOf("Nord"), type = "Creature", rarity = "Legendary")
        val commonAction1 = Card(name = "common action 1", type = "Action", rarity = "Common")
        val commonAction2 = Card(name = "common action 2", type = "Action", rarity = "Common")
        val rareAction1 = Card(name = "rare action 1", type = "Action", rarity = "Rare")
        val commonItem1 = Card(name = "common item 1", type = "Item", rarity = "Common")
        val commonItem2 = Card(name = "common item 2", type = "Item", rarity = "Common")
        val epicItem1 = Card(name = "epic item 1", type = "Item", rarity = "Epic")
        val rareSupport1 = Card(name = "rare support 1", type = "Support", rarity = "Rare")

        val deck = Deck(cards = listOf(legendaryCreature, commonAction1, commonAction1, commonAction2, rareAction1, commonItem1, commonItem2, commonItem2, epicItem1, rareSupport1, rareSupport1))

        var failures: List<String>
        val tournament = Tournament(id = "t1")

        tournament.rules.add("analysis.creaturesByRarity('Common').size() == 0")
        tournament.rules.add("analysis.creaturesByRarity('Rare').size() == 0")
        tournament.rules.add("analysis.creaturesByRarity('Epic').size() == 0")
        tournament.rules.add("analysis.creaturesByRarity('Legendary').size() == 1")
        tournament.rules.add("analysis.creaturesByRarity('Legendary').containsAll(['creature 1'])")
        tournament.rules.add("analysis.raritiesOfType('Creature').containsAll(['Legendary'])")

        tournament.rules.add("analysis.actionsByRarity('Common').size() == 2")
        tournament.rules.add("analysis.actionsByRarity('Common').containsAll(['common action 2', 'common action 1'])")
        tournament.rules.add("analysis.actionsByRarity('Rare').size() == 1")
        tournament.rules.add("analysis.actionsByRarity('Rare').containsAll(['rare action 1'])")
        tournament.rules.add("analysis.actionsByRarity('Epic').size() == 0")
        tournament.rules.add("analysis.actionsByRarity('Legendary').size() == 0")
        tournament.rules.add("analysis.raritiesOfType('Action').containsAll(['Common', 'Rare'])")
        tournament.rules.add("['Common', 'Rare', 'Legendary'].containsAll(analysis.raritiesOfType('Action'))")

        tournament.rules.add("analysis.itemsByRarity('Common').size() == 2")
        tournament.rules.add("analysis.itemsByRarity('Common')containsAll(['common item 1', 'common item 2'])")
        tournament.rules.add("analysis.itemsByRarity('Rare').size() == 0")
        tournament.rules.add("analysis.itemsByRarity('Epic').size() == 1")
        tournament.rules.add("analysis.itemsByRarity('Epic')containsAll(['epic item 1'])")
        tournament.rules.add("analysis.itemsByRarity('Legendary').size() == 0")
        tournament.rules.add("analysis.raritiesOfType('Item').containsAll(['Common', 'Epic'])")

        tournament.rules.add("analysis.supportsByRarity('Common').size() == 0")
        tournament.rules.add("analysis.supportsByRarity('Rare').size() == 1")
        tournament.rules.add("analysis.supportsByRarity('Rare')containsAll(['rare support 1'])")
        tournament.rules.add("analysis.supportsByRarity('Epic').size() == 0")
        tournament.rules.add("analysis.supportsByRarity('Legendary').size() == 0")
        tournament.rules.add("analysis.raritiesOfType('Support').containsAll(['Rare'])")
        failures = MVELEngine.checkRules(tournament, deck)
        assertThat(failures).isEmpty()

    }

    @Test
    fun `is undead checks`() {
        val creature1 = Card(name = "Tenarr Zalviit Lurker", subtypes = listOf("Khajiit"), type = "Creature")
        val creature2 = Card(name = "Death Hound", subtypes = listOf("Beast"), type = "Creature")
        val creature3 = Card(name = "Reflective Automaton", subtypes = listOf("Factotum"), type = "Creature")
        val creature4 = Card(name = "Skeletal Dragon", subtypes = listOf("Dragon"), type = "Creature")
        val creature5 = Card(name = "A vampire", subtypes = listOf("Vampire"), type = "Creature")
        val deck1 = Deck(cards = listOf(creature1, creature2, creature3, creature4, creature5))

        var failures: List<String>
        val tournament = Tournament(id = "t1")

        tournament.rules.add("analysis.isUndead()")
        failures = MVELEngine.checkRules(tournament, deck1)
        assertThat(failures).isEmpty()

        val creature6 = Card(name = "A Khajiit", subtypes = listOf("Khajiit"), type = "Creature")
        val deck2 = Deck(cards = listOf(creature1, creature2, creature3, creature4, creature5, creature6))

        failures = MVELEngine.checkRules(tournament, deck2)
        assertThat(failures).containsOnly("analysis.isUndead()")

    }
}