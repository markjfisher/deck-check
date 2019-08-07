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
        tournament.reasons.clear()
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
        tournament.reasons.clear()
        tournament.reasons.add("You must have a Fish type. You have: @foreach{type: subtypes}@{type}@end{', '}")
        failures = MVELEngine.checkRules(tournament, deck1)
        assertThat(failures).containsOnly("You must have a Fish type. You have: Nord, Orc, Vampire")

        // ... or with ones that are included
        tournament.rules.clear()
        tournament.reasons.clear()
        tournament.rules.add("subtypes.contains('Vampire', 'Fish')")
        tournament.reasons.add("You must have a Fish and a Vampire. You have: @foreach{type: subtypes}@{type}@end{', '}")
        failures = MVELEngine.checkRules(tournament, deck1)
        assertThat(failures).containsOnly("You must have a Fish and a Vampire. You have: Nord, Orc, Vampire")

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
        tournament.reasons.clear()
        tournament.rules.add("subtypes.containsOnly('Vampire', 'Nord')")
        tournament.reasons.add("You must have only Nord and Vampire types. You have: @foreach{type: subtypes}@{type}@end{', '}")
        failures = MVELEngine.checkRules(tournament, deck1)
        assertThat(failures).containsOnly("You must have only Nord and Vampire types. You have: Nord, Orc, Vampire")

        // elements not in the list fail
        tournament.rules.clear()
        tournament.reasons.clear()
        tournament.rules.add("subtypes.containsOnly('Fish', 'Nord', 'Vampire', 'Orc')")
        tournament.reasons.add("You must have only Fish, Nord Vampire, and Orc types. You have: @foreach{type: subtypes}@{type}@end{', '}")
        failures = MVELEngine.checkRules(tournament, deck1)
        assertThat(failures).containsOnly("You must have only Fish, Nord Vampire, and Orc types. You have: Nord, Orc, Vampire")

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

        tournament.rules.add("a.creaturesByRarity('Common').size() == 0")
        tournament.rules.add("a.creaturesByRarity('Rare').size() == 0")
        tournament.rules.add("a.creaturesByRarity('Epic').size() == 0")
        tournament.rules.add("a.creaturesByRarity('Legendary').size() == 1")
        tournament.rules.add("a.creaturesByRarity('Legendary').containsAll(['creature 1'])")
        tournament.rules.add("a.raritiesOfType('Creature').containsAll(['Legendary'])")

        tournament.rules.add("a.actionsByRarity('Common').size() == 2")
        tournament.rules.add("a.actionsByRarity('Common').containsAll(['common action 2', 'common action 1'])")
        tournament.rules.add("a.actionsByRarity('Rare').size() == 1")
        tournament.rules.add("a.actionsByRarity('Rare').containsAll(['rare action 1'])")
        tournament.rules.add("a.actionsByRarity('Epic').size() == 0")
        tournament.rules.add("a.actionsByRarity('Legendary').size() == 0")
        tournament.rules.add("a.raritiesOfType('Action').containsAll(['Common', 'Rare'])")
        tournament.rules.add("['Common', 'Rare', 'Legendary'].containsAll(a.raritiesOfType('Action'))")

        tournament.rules.add("a.itemsByRarity('Common').size() == 2")
        tournament.rules.add("a.itemsByRarity('Common')containsAll(['common item 1', 'common item 2'])")
        tournament.rules.add("a.itemsByRarity('Rare').size() == 0")
        tournament.rules.add("a.itemsByRarity('Epic').size() == 1")
        tournament.rules.add("a.itemsByRarity('Epic')containsAll(['epic item 1'])")
        tournament.rules.add("a.itemsByRarity('Legendary').size() == 0")
        tournament.rules.add("a.raritiesOfType('Item').containsAll(['Common', 'Epic'])")

        tournament.rules.add("a.supportsByRarity('Common').size() == 0")
        tournament.rules.add("a.supportsByRarity('Rare').size() == 1")
        tournament.rules.add("a.supportsByRarity('Rare')containsAll(['rare support 1'])")
        tournament.rules.add("a.supportsByRarity('Epic').size() == 0")
        tournament.rules.add("a.supportsByRarity('Legendary').size() == 0")
        tournament.rules.add("a.raritiesOfType('Support').containsAll(['Rare'])")
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

        tournament.rules.add("a.isUndead()")
        tournament.reasons.add("You must have only undead creatures.")
        failures = MVELEngine.checkRules(tournament, deck1)
        assertThat(failures).isEmpty()

        val creature6 = Card(name = "A Khajiit", subtypes = listOf("Khajiit"), type = "Creature")
        val deck2 = Deck(cards = listOf(creature1, creature2, creature3, creature4, creature5, creature6))

        failures = MVELEngine.checkRules(tournament, deck2)
        assertThat(failures).containsOnly("You must have only undead creatures.")
    }

    @Test
    fun `when a reason is not given the rule definition is returned`() {
        val failures: List<String>
        val tournament = Tournament(id = "t1")

        tournament.rules.add("a.isUndead()")

        val creature = Card(name = "A Khajiit", subtypes = listOf("Khajiit"), type = "Creature")
        val deck2 = Deck(cards = listOf(creature))

        failures = MVELEngine.checkRules(tournament, deck2)
        assertThat(failures).containsOnly("a.isUndead()")
    }

    @Test
    fun `multiple reasons are rendered`() {
        val failures: List<String>
        val tournament = Tournament(id = "t1")

        tournament.rules.add("rareCount == 1")
        tournament.rules.add("epicCount == 1")
        tournament.reasons.add("You must have exactly 1 rare card, you had @{rareCount}")
        tournament.reasons.add("You must have exactly 1 epic card, you had @{epicCount}")

        val creature = Card(name = "A Khajiit", subtypes = listOf("Khajiit"), type = "Creature", rarity = "Common")
        val deck2 = Deck(cards = listOf(creature))

        failures = MVELEngine.checkRules(tournament, deck2)
        assertThat(failures).containsExactlyInAnyOrder("You must have exactly 1 rare card, you had 0", "You must have exactly 1 epic card, you had 0")
    }
}