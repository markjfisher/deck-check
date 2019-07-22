package legends

import io.elderscrollslegends.Deck
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import org.jeasy.rules.api.Facts
import org.jeasy.rules.api.Rules
import org.jeasy.rules.core.DefaultRulesEngine
import org.jeasy.rules.mvel.MVELRule

enum class TournamentCommands(val cmd: String) {
    CMD_HELP("help") {
        override fun run(args: List<String>, mention: String, username: String): ReplyData {
            return ReplyData(text = listOf(helpTournaments()))
        }
    },
    CMD_LIST("list") {
        override fun run(args: List<String>, mention: String, username: String): ReplyData {
            return ReplyData(text = listOf(listTournaments()))
        }
    },
    CMD_CREATE("create") {
        override fun run(args: List<String>, mention: String, username: String): ReplyData {
            return ReplyData(text = listOf(createTournament(args, username)))
        }
    },
    CMD_DELETE("delete") {
        override fun run(args: List<String>, mention: String, username: String): ReplyData {
            return ReplyData(text = listOf(deleteTournament(args, username)))
        }
    },
    CMD_ADD_RULE("addRule") {
        override fun run(args: List<String>, mention: String, username: String): ReplyData {
            return ReplyData(text = listOf(addTournamentRule(args, username)))
        }
    },
    CMD_DEL_RULE("delRule") {
        override fun run(args: List<String>, mention: String, username: String): ReplyData {
            return ReplyData(text = listOf(delTournamentRule(args, username)))
        }
    },
    CMD_CHECK("check") {
        override fun run(args: List<String>, mention: String, username: String): ReplyData {
            return ReplyData(text = listOf(checkTournamentDeck(args, mention, username)))
        }
    },
    CMD_SAVE("save") {
        override fun run(args: List<String>, mention: String, username: String): ReplyData {
            return ReplyData(text = listOf(saveTournaments(username)))
        }
    },
    CMD_LOAD("load") {
        override fun run(args: List<String>, mention: String, username: String): ReplyData {
            return ReplyData(text = listOf(loadTournaments(args, username)))
        }
    }
    ;

    abstract fun run(args: List<String>, mention: String, username: String): ReplyData

    fun helpTournaments(): String {
        return """
            |```!tournament [help|list|create|delete|addRule|delRule|check|save|load] <args>
            |   help - information about commands
            |   list - list known tournaments
            |   create id "tournament description/name" - create new tournament
            |   delete id - delete tournament
            |   addRule id "rule text" - add rule to a tournament
            |   delRule id "rule text" - remove a rule from tournament
            |   check id SP_code - check a deck against tournament rules
            |   save - outputs serialized value for the tournaments
            |   load "data" - loads serialized data
            |
            |Rules can contain boolean statements, e.g.
            |   rareCount == 1
            |   totalCards == 75
            |
            |Rules can be compound using &&, ||
            |   (epicCount == 0 && legendaryCount == 1) || (epicCount == 1 && legendaryCount == 0)
            |
            |Rules can either be on different lines, or separated by asemi-colon.
            |Note these are 2 rules, not a single combined rule.
            |   rareCount == 1; totalCards == 75
            |
            |The following variables are available to check:
            |   commonCount, rareCount, epicCount, legendaryCount
            |   actionsCount, itemsCount, supportsCount
            |   of1Count, of2Count, of3Count, totalCards, uniqueCards
            |   deckClassName
            |
            |Rules can also contain deck checks using:
            |   deck.hasCard("Marked Man")
            |   deck.cardCount("Marked Man") == 3
            |
            |Note: No quotations should be used around any text rules.
            |```""".trimMargin()
    }

    fun createTournament(args: List<String>, username: String): String {
        if (!isAdmin(username)) {
            return "Create tournament can only be run by admins: ${BotCheck.admins.joinToString(",")}. You are: $username"
        }

        if (args.size < 2) {
            return "Invalid create tournament command, please supply an ID and Description"
        }
        val tid = args[0]
        val existingTournament = BotCheck.tournaments.find { it.id == tid }
        if (existingTournament != null) return "Error: Tournament with id $tid already exists: $existingTournament"

        val tournament = Tournament(id = args[0], description = endArgsAsString(args.drop(1)))
        BotCheck.tournaments.add(tournament)
        return "Created tournament:\n```$tournament```"
    }

    fun deleteTournament(args: List<String>, username: String): String {
        if (!isAdmin(username)) {
            return "Delete tournament can only be run by admins: ${BotCheck.admins.joinToString(",")}. You are: $username"
        }
        if (args.size != 1) {
            return "Invalid delete tournament command, please supply an ID"
        }

        val tid = args[0]
        BotCheck.tournaments.find { it.id == tid } ?: return "Unknown tournament id: $tid"
        BotCheck.tournaments.removeAll { it.id == tid }
        return "Removed tournament $tid"
    }

    fun addTournamentRule(args: List<String>, username: String): String {
        if (!isAdmin(username)) {
            return "Add tournament rule can only be run by admins: ${BotCheck.admins.joinToString(",")}. You are: $username"
        }
        if (args.size < 2) {
            return "Invalid addRule tournament command, please supply an ID and New Rule"
        }
        val tid = args[0]
        val tournament =
            BotCheck.tournaments.find { it.id == tid } ?: return "Error: Cannot add rule. Tournament with id $tid does not exist"

        val newRule = endArgsAsString(args.drop(1))
        tournament.rules.add(newRule)
        return "Tournament updated:\n```$tournament```"
    }

    fun delTournamentRule(args: List<String>, username: String): String {
        if (!isAdmin(username)) {
            return "Delete tournament rule can only be run by admins: ${BotCheck.admins.joinToString(", ")}. You are: $username"
        }
        if (args.size < 2) {
            return "Invalid delRule tournament command, please supply an ID and Rule text"
        }
        val tid = args[0]
        val tournament = BotCheck.tournaments.find { it.id == tid }
            ?: return "Error: Cannot delete rule. Tournament with id $tid does not exist"

        val rule = endArgsAsString(args.drop(1))
        tournament.rules.removeAll { it == rule }
        return "Tournament updated:\n```$tournament```"

    }

    fun checkTournamentDeck(args: List<String>, mention: String, username: String): String {
        if (args.size != 2) {
            return "Invalid check tournament command, please supply an ID and deck code"
        }
        val tid = args[0]
        val tournament = BotCheck.tournaments.find { it.id == tid }
            ?: return "Error: Cannot check deck code. Tournament with id $tid does not exist"

        val deckCode = args[1]
        val deck = Deck.importCode(deckCode)

        val mvelRules = tournament.rules.mapIndexed { index, ruleString ->
            MVELRule()
                .name("Rule $index: '$ruleString'")
                .description("This is rule $index that will fire for '$ruleString'")
                .`when`(ruleString)
                .then("valid.addPass($index);")
        }.toSet()

        val analysed = DeckAnalysis(deck)
        val commonCount = analysed.commonCount
        val rareCount = analysed.rareCount
        val epicCount = analysed.epicCount
        val legendaryCount = analysed.legendaryCount
        val actionsCount = analysed.actionsCount
        val itemsCount = analysed.itemsCount
        val supportsCount = analysed.supportsCount
        val of1Count = analysed.c1
        val of2Count = analysed.c2
        val of3Count = analysed.c3
        val totalCards = analysed.totalCards
        val uniqueCards = analysed.totalUnique
        val deckClassName = analysed.deckClassName

        val valid = Valid()
        val facts = Facts()
        facts.put("commonCount", commonCount)
        facts.put("rareCount", rareCount)
        facts.put("epicCount", epicCount)
        facts.put("legendaryCount", legendaryCount)
        facts.put("epicCount", epicCount)
        facts.put("actionsCount", actionsCount)
        facts.put("itemsCount", itemsCount)
        facts.put("supportsCount", supportsCount)
        facts.put("of1Count", of1Count)
        facts.put("of2Count", of2Count)
        facts.put("of3Count", of3Count)
        facts.put("totalCards", totalCards)
        facts.put("uniqueCards", uniqueCards)
        facts.put("deckClassName", deckClassName)
        facts.put("valid", valid)
        facts.put("deck", WrappedDeck(deck))

        val rules = Rules(mvelRules)
        val rulesEngine = DefaultRulesEngine()
        rulesEngine.fire(rules, facts)

        // check which rules didn't pass
        val allCorrectSet = (0 until tournament.rules.size).toSet()
        val failedRulesSet = allCorrectSet - valid.passes
        val failedRules = failedRulesSet.map {
            tournament.rules[it]
        }
        return if (failedRulesSet.isNotEmpty()) """
            |$mention
            |Deck: `$deckCode`
            |Tournament: `${tournament.description}`
            |Failed on rules:
            |```${failedRules.joinToString("\n")}
            |```""".trimMargin()
        else "$mention deck `$deckCode` passes all rules for tournament `${tournament.description}`"

    }

    class Valid {
        var passes: MutableSet<Int> = mutableSetOf()

        fun addPass(ruleNumber: Int) {
            passes.add(ruleNumber)
        }
    }

    class WrappedDeck(private val deck: Deck) {
        fun hasCard(name: String): Boolean {
            return deck.cards.find { it.name == name } != null
        }

        fun cardCount(name: String): Int {
            return deck.cards.count { it.name == name }
        }
    }

    private fun endArgsAsString(args: List<String>) = args.joinToString(" ")

    fun listTournaments(): String {
        return if (BotCheck.tournaments.isNotEmpty()) BotCheck.tournaments.joinToString("\n") { "```$it```" } else "No tournaments defined."
    }

    fun saveTournaments(username: String): String {
        if (!isAdmin(username)) {
            return "Save tournament can only be run by admins: ${BotCheck.admins.joinToString(", ")}. You are: $username"
        }

        val json = Json(JsonConfiguration.Stable)
        return "```${json.stringify(Tournament.serializer().list, BotCheck.tournaments)}```"
    }

    fun loadTournaments(args: List<String>, username: String): String {
        if (!isAdmin(username)) {
            return "Load tournament can only be run by admins: ${BotCheck.admins.joinToString(", ")}. You are: $username"
        }
        if (args.isEmpty()) {
            return "Invalid load tournament command, please supply serialized tournament string"
        }
        val data = endArgsAsString(args)
        val json = Json(JsonConfiguration.Stable)
        val importData = json.parse(Tournament.serializer().list, data)
        BotCheck.tournaments.clear()
        BotCheck.tournaments.addAll(importData)
        return "Loaded ${importData.size} tournaments"
    }

    private fun isAdmin(name: String) = BotCheck.admins.contains(name)


}
