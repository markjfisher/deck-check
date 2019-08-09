package legends

import io.elderscrollslegends.Deck
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import legends.MVELEngine.checkRules

enum class TournamentCommands(val cmd: String) {
    CMD_HELP("help") {
        override fun run(args: List<String>, mention: String, username: String): ReplyData {
            return ReplyData(text = helpTournaments())
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
    CMD_REGISTER("register") {
        override fun run(args: List<String>, mention: String, username: String): ReplyData {
            return ReplyData(text = listOf(registerPlayer(args, mention)))
        }
    },
    CMD_REMOVE("remove") {
        override fun run(args: List<String>, mention: String, username: String): ReplyData {
            return ReplyData(text = listOf(removePlayer(args, username)))
        }
    },
    CMD_CHECK("check") {
        override fun run(args: List<String>, mention: String, username: String): ReplyData {
            return ReplyData(text = listOf(checkTournamentDeck(args, mention)))
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

    fun helpTournaments(): List<String> {
        return listOf("""
            |```!tournament [help|list|create|delete|addRule|delRule|register|remove|check|save|load] <args>
            | help - information about commands
            | list - list known tournaments
            | create id description - create new tournament
            | delete id - delete tournament
            | addRule id rule - add rule to a tournament
            | delRule id rule - remove a rule from tournament
            | register id IGN DeckCode - register with IGN and deck code
            | remove id IGN - remove IGN from registered players
            | check id DeckCode - check a deck against tournament rules
            | save - outputs serialized value for the tournaments
            | load "data" - loads serialized data
            |
            |Only registered admins can change tournaments
            |```""".trimMargin(),

            """
            |```# DEFINING RULES
            |The rules engine uses MVEL, go look it up for full details.
            |
            |Rules added to tournaments are boolean statements that must all return true for a
            |deck to be valid, e.g.
            | rareCount == 1
            | totalCards == 75
            | rareCount == 1; totalCards == 75
            |
            |Rules can be compound using &&, ||
            | (epicCount == 0 && legendaryCount == 1) || (epicCount == 1 && legendaryCount == 0)
            |
            |Rules can also contain deck checks using:
            | deck.hasCard("Marked Man")
            | deck.cardCount("Marked Man") == 3
            |
            |Sub types of creature (Vampire, Orc, etc) can be checked with:
            |CONTAINS TEST:
            | subtypes.contains('Vampire')          // must have at least one Vampire
            | subtypes.contains('Vampire', 'Nord')  // must have at least one Vampire and Nord
            | subtypes.contains('Fish')             // if you don't have a Fish, this will fail
            |
            |FULL SET TEST:
            | subtypes.containsAll('Orc', 'Nord')   // You must have only Orc and Nords
            |
            |SUBSET TEST:
            | # check your creatures all are any of the given list
            | subtypes.subsetOf('Skeleton', 'Spirit', 'Vampire', 'Mummy')
            |```""".trimMargin(),

            """
            |```Analysis Object
            | # An 'analysis' object is exposed via 'a'
            |
            | # types by rarity gives a list of names of the cards.
            | a.creaturesByRarity(rarity): List<String>
            | a.actionsByRarity(rarity): List<String>
            | a.itemsByRarity(rarity): List<String>
            | a.supportsByRarity(rarity): List<String>
            | (rarities are: Common, Rare, Epic, Legendary)
            |
            | e.g
            | a.creaturesByRarity('Common').size() == 1
            | a.creaturesByRarity('Rare').containsAll(['Dwarven Dynamo'])
            |
            | # creaturesOfSubtype
            | a.creaturesOfSubtype('Factotum').containsAll(['Reflective Automaton'])
            |
            | # cost functions
            | a.costToCards[n]: List<Card> // gives list of unique cards that cost n
            |
            | a.countByCost[n]: Int // get just the count of cards that cost n.
            |
            | a.costs: List<Int> // All the costs that exist in the deck, e.g. if made up of only 0,1,2 cost
            |```""".trimMargin(),

            """
            |```The following variables are available to check:
            | commonCount, rareCount, epicCount, legendaryCount
            | actionsCount, itemsCount, supportsCount
            | of1Count, of2Count, of3Count, totalCards, uniqueCards
            | deckClassName
            |
            |```""".trimMargin())
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
            BotCheck.tournaments.find { it.id == tid }
                ?: return "Error: Cannot add rule. Tournament with id $tid does not exist"

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

    fun registerPlayer(args: List<String>, mention: String): String {
        if (args.size != 3) {
            return "Invalid register tournament command, please supply: ID, IGN, Deck Code"
        }
        val tid = args[0]
        val tournament = BotCheck.tournaments.find { it.id == tid }
            ?: return "Error: Cannot register player. Tournament with id $tid does not exist"

        val ign = args[1]
        val deckCode = args[2]

        val deck = DeckFixes.fix(Deck.importCode(deckCode))

        val failedRules = checkRules(tournament, deck)
        if (failedRules.isEmpty()) {
            tournament.addPlayer(ign = ign, deck = deckCode)
            return """
                |$mention
                |Deck: `$deckCode`
                |Tournament: `${tournament.description}`
                |Passed all rules, player $ign registered.
                |""".trimMargin()
        } else {
            return """
                |$mention
                |Deck: `$deckCode`
                |Tournament: `${tournament.description}`
                |Failed on rules:
                |```${failedRules.joinToString("\n")}
                |```""".trimMargin()
        }
    }

    fun removePlayer(args: List<String>, username: String): String {
        if (!isAdmin(username)) {
            return "Remove Player can only be run by admins: ${BotCheck.admins.joinToString(", ")}. You are: $username"
        }
        if (args.size != 2) {
            return "Invalid remove player tournament command, please supply: ID, IGN"
        }
        val tid = args[0]
        val tournament = BotCheck.tournaments.find { it.id == tid }
            ?: return "Error: Cannot remove player. Tournament with id $tid does not exist"

        val ign = args[1]
        return if (tournament.hasPlayer(ign)) {
            tournament.removePlayer(ign)
            "Player $ign successfully removed from tournament: ${tournament.description}"
        } else {
            "Could not find IGN $ign in tournament: ${tournament.description}"
        }
    }

    fun checkTournamentDeck(args: List<String>, mention: String): String {
        if (args.size != 2) {
            return "Invalid check tournament command, please supply an ID and deck code"
        }
        val tid = args[0]
        val tournament = BotCheck.tournaments.find { it.id.toLowerCase() == tid.toLowerCase() }
            ?: return "$mention Error: Cannot check deck code. Tournament with id $tid does not exist"

        val deckCode = args[1]
        val deck = DeckFixes.fix(Deck.importCode(deckCode))

        val failedRules = checkRules(tournament, deck)
        return if (failedRules.isNotEmpty()) """
            |$mention
            |Deck: `$deckCode`
            |Tournament: `${tournament.description}`
            |Failed on rules:
            |```${failedRules.joinToString("\n")}
            |```""".trimMargin()
        else """
            |$mention
            |Deck: `$deckCode`
            |Tournament: `${tournament.description}`
            |Passed all rules!
            |""".trimMargin()

    }

    class Valid {
        var passes: MutableSet<Int> = mutableSetOf()

        fun addPass(ruleNumber: Int) {
            passes.add(ruleNumber)
        }
    }

    private fun endArgsAsString(args: List<String>) = args.joinToString(" ")

    private fun isAdmin(name: String) = BotCheck.admins.contains(name)

}

