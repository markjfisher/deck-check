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
}