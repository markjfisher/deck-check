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

        val analysis = DeckAnalysis(DeckFixes.fix(deck))

        val valid = TournamentCommands.Valid()
        val facts = Facts()
        facts.put("commonCount", analysis.commonCount)
        facts.put("rareCount", analysis.rareCount)
        facts.put("epicCount", analysis.epicCount)
        facts.put("legendaryCount", analysis.legendaryCount)
        facts.put("actionsCount", analysis.actionsCount)
        facts.put("itemsCount", analysis.itemsCount)
        facts.put("supportsCount", analysis.supportsCount)
        facts.put("of1Count", analysis.c1)
        facts.put("of2Count", analysis.c2)
        facts.put("of3Count", analysis.c3)
        facts.put("totalCards", analysis.totalCards)
        facts.put("uniqueCards", analysis.totalUnique)
        facts.put("deckClassName", analysis.deckClassName)
        facts.put("subtypes", WrappedList(analysis.subtypes))
        facts.put("analysis", analysis)

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