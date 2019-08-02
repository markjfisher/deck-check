package legends

import io.elderscrollslegends.Deck
import legends.wrapped.WrappedDeck
import legends.wrapped.WrappedList
import org.jeasy.rules.api.Facts
import org.jeasy.rules.api.Rules
import org.jeasy.rules.core.DefaultRulesEngine
import org.jeasy.rules.mvel.MVELRule

object MVELEngine {
    fun checkRules(tournament: Tournament, deck: Deck): List<String> {
        val mvelRules = tournament.rules.mapIndexed { index, ruleString ->
            MVELRule()
                .name("Rule $index: '$ruleString'")
                .description("This is rule $index that will fire for '$ruleString'")
                .`when`(ruleString)
                .then("valid.addPass($index);")
        }.toSet()

        val analysed = DeckAnalysis(deck)

        val valid = TournamentCommands.Valid()
        val facts = Facts()
        facts.put("commonCount", analysed.commonCount)
        facts.put("rareCount", analysed.rareCount)
        facts.put("epicCount", analysed.epicCount)
        facts.put("legendaryCount", analysed.legendaryCount)
        facts.put("actionsCount", analysed.actionsCount)
        facts.put("itemsCount", analysed.itemsCount)
        facts.put("supportsCount", analysed.supportsCount)
        facts.put("of1Count", analysed.c1)
        facts.put("of2Count", analysed.c2)
        facts.put("of3Count", analysed.c3)
        facts.put("totalCards", analysed.totalCards)
        facts.put("uniqueCards", analysed.totalUnique)
        facts.put("deckClassName", analysed.deckClassName)
        facts.put("subtypes", WrappedList(analysed.subtypes))

        facts.put("valid", valid)
        facts.put("deck", WrappedDeck(deck))

        val rules = Rules(mvelRules)
        val rulesEngine = DefaultRulesEngine()
        rulesEngine.fire(rules, facts)

        // check which rules didn't pass
        val allCorrectSet = (0 until tournament.rules.size).toSet()
        val failedRulesSet = allCorrectSet - valid.passes
        return failedRulesSet.map {
            tournament.rules[it]
        }
    }

}