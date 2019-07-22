package legends

import com.jessecorbett.diskord.api.rest.CreateMessage
import com.jessecorbett.diskord.api.rest.Embed
import com.jessecorbett.diskord.api.rest.EmbedAuthor
import com.jessecorbett.diskord.api.rest.EmbedImage
import com.jessecorbett.diskord.dsl.bot
import com.jessecorbett.diskord.dsl.command
import com.jessecorbett.diskord.dsl.commands
import com.jessecorbett.diskord.util.mention
import com.jessecorbett.diskord.util.words
import com.natpryce.konfig.*
import io.elderscrollslegends.CardCache
import io.elderscrollslegends.Deck
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.UnstableDefault
import mu.KotlinLogging
import org.jeasy.rules.api.Facts
import org.jeasy.rules.api.Rules
import org.jeasy.rules.core.DefaultRulesEngine
import org.jeasy.rules.mvel.MVELRule

private val logger = KotlinLogging.logger {}

object BotCheck {

    private val token = Key("deck-check.bot.token", stringType)
    private val adminsKey = Key("deck-check.bot.admins", stringType)

    private val config = ConfigurationProperties.systemProperties() overriding
            EnvironmentVariables() overriding
            ConfigurationProperties.fromResource("deck-check.properties")

    private val admins = mutableListOf<String>()

    private val tournaments = mutableListOf<Tournament>()

    @UnstableDefault
    @JvmStatic
    fun main(args: Array<String>) {
        admins.addAll(config[adminsKey].split(","))

        runBlocking {
            logger.info {"Loading card cache..."}
            CardCache.load()
            logger.info {"... complete loading."}
            bot(config[token]) {
                commands(prefix = "!") {
                    command(command = "deck") {
                        val deckArgs = words.drop(1)
                        val deckCommand =
                            DeckCommands.values().find { it.cmd == deckArgs[0] } ?: BotCheck.DeckCommands.CMD_HELP
                        val replyData = deckCommand.run(deckArgs.drop(1), author.mention, author.username)

                        replyData.text.forEach { text ->
                            channel.createMessage(
                                CreateMessage(
                                    content = text,
                                    embed = replyData.embed
                                    // fileContent = fileData
                                )
                            )
                            // reply(text = replyData.text, embed = replyData.embed)
                        }
                    }

                    command(command = "tournament") {
                        val tournamentArgs = words.drop(1)
                        val tournamentCommand =
                            TournamentCommands.values().find { it.cmd == tournamentArgs[0] }
                                ?: BotCheck.TournamentCommands.CMD_HELP
                        val replyData = tournamentCommand.run(tournamentArgs.drop(1), author.mention, author.username)

                        replyData.text.forEach { text ->
                            channel.createMessage(
                                CreateMessage(
                                    content = text,
                                    embed = replyData.embed
                                    // fileContent = fileData
                                )
                            )
                            // reply(text = replyData.text, embed = replyData.embed)
                        }
                    }
                }

                started {
                    logger.info { "started with sessionId: ${it.sessionId}" }
                }
            }
        }
    }

    enum class DeckCommands(val cmd: String) {
        CMD_HELP("help") {
            override fun run(args: List<String>, mention: String, username: String): ReplyData {
                val allCommands = values().map { it.cmd }
                return ReplyData(text = listOf("help: Known commands: ${allCommands.joinToString(", ")}"))
            }
        },
        CMD_INFO("info") {
            override fun run(args: List<String>, mention: String, username: String): ReplyData {
                return ReplyData(text = show(args, mention, username, "info"))
            }
        },
        CMD_DETAIL("detail") {
            override fun run(args: List<String>, mention: String, username: String): ReplyData {
                return ReplyData(text = show(args, mention, username, "detail"))
            }
        },

        CMD_TEST("test") {
            override fun run(args: List<String>, mention: String, username: String): ReplyData {
                val embed = Embed(
                    title = "embed title",
                    description = "embed description",
                    author = EmbedAuthor(name = username),
                    // image = EmbedImage(url = "https://vignette.wikia.nocookie.net/elderscrolls/images/2/25/Assembled_Conduit.png/revision/latest")
                    image = EmbedImage(url = "attachment:///facepalm_tiny.png")
                )
                return ReplyData(text = listOf("An image"), embed = embed)
            }
        };

        abstract fun run(args: List<String>, mention: String, username: String): ReplyData
    }

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
        }
        ;

        abstract fun run(args: List<String>, mention: String, username: String): ReplyData
    }

    private fun helpTournaments(): String {
        return """
            |```!tournament [help|list|create|delete|addRule|delRule|check] <args>
            |   help - information about commands
            |   list - list known tournaments
            |   create id "tournament description/name" - create new tournament
            |   delete id - delete tournament
            |   addRule id "rule text" - add rule to a tournament
            |   delRule id "rule text" - remove a rule from tournament
            |   check id SP_code - check a deck against tournament rules
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

    private fun createTournament(args: List<String>, username: String): String {
        if (!isAdmin(username)) {
            return "Create tournament can only be run by admins: ${admins.joinToString(",")}. You are: $username"
        }

        if (args.size < 2) {
            return "Invalid create tournament command, please supply an ID and Description"
        }
        val tid = args[0]
        val existingTournament = tournaments.find { it.id == tid }
        if (existingTournament != null) return "Error: Tournament with id $tid already exists: $existingTournament"

        val tournament = Tournament(id = args[0], description = endArgsAsString(args.drop(1)))
        tournaments.add(tournament)
        return "Created tournament:\n```$tournament```"
    }

    private fun deleteTournament(args: List<String>, username: String): String {
        if (!isAdmin(username)) {
            return "Delete tournament can only be run by admins: ${admins.joinToString(",")}. You are: $username"
        }
        if (args.size != 1) {
            return "Invalid delete tournament command, please supply an ID"
        }

        val tid = args[0]
        tournaments.find { it.id == tid } ?: return "Unknown tournament id: $tid"
        tournaments.removeAll { it.id == tid }
        return "Removed tournament $tid"
    }

    private fun addTournamentRule(args: List<String>, username: String): String {
        if (!isAdmin(username)) {
            return "Tournament commands can only be run by admins: ${admins.joinToString(",")}. You are: $username"
        }
        if (args.size < 2) {
            return "Invalid addRule tournament command, please supply an ID and New Rule"
        }
        val tid = args[0]
        val tournament =
            tournaments.find { it.id == tid } ?: return "Error: Cannot add rule. Tournament with id $tid does not exist"

        val newRule = endArgsAsString(args.drop(1))
        tournament.rules.add(newRule)
        return "Tournament updated:\n```$tournament```"
    }

    private fun delTournamentRule(args: List<String>, username: String): String {
        if (!isAdmin(username)) {
            return "Tournament commands can only be run by admins: ${admins.joinToString(", ")}. You are: $username"
        }
        if (args.size < 2) {
            return "Invalid delRule tournament command, please supply an ID and Rule text"
        }
        val tid = args[0]
        val tournament = tournaments.find { it.id == tid }
            ?: return "Error: Cannot delete rule. Tournament with id $tid does not exist"

        val rule = endArgsAsString(args.drop(1))
        tournament.rules.removeAll { it == rule }
        return "Tournament updated:\n```$tournament```"

    }

    private fun checkTournamentDeck(args: List<String>, mention: String, username: String): String {
        if (args.size != 2) {
            return "Invalid check tournament command, please supply an ID and deck code"
        }
        val tid = args[0]
        val tournament = tournaments.find { it.id == tid }
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

    private fun listTournaments(): String {
        return if (tournaments.isNotEmpty()) tournaments.joinToString("\n") { "```$it```" } else "No tournaments defined."
    }

    private fun isAdmin(name: String) = admins.contains(name)

    private fun show(
        args: List<String>,
        mention: String,
        username: String,
        type: String
    ): List<String> {
        if (args.size != 1) {
            return listOf("$mention: Please supply a single deck code.")
        }

        val deckCode = args[0]
        logger.info { "User: $username asked for $type for code: $deckCode" }
        val deck = Deck.importCode(deckCode)
        val da = DeckAnalysis(deck)

        val reply = when (type) {
            "info", "detail" -> {
                val line1 =
                    String.format("%-10s: %-5d   %-10s: %-5d", "Common", da.commonCount, "Actions", da.actionsCount)
                val line2 = String.format("%-10s: %-5d   %-10s: %-5d", "Rare", da.rareCount, "Items", da.itemsCount)
                val line3 =
                    String.format("%-10s: %-5d   %-10s: %-5d", "Epic", da.epicCount, "Support", da.supportsCount)
                val line4 = String.format(
                    "%-10s: %-5d   %-10s: %-5d",
                    "Legendary",
                    da.legendaryCount,
                    "Creatures",
                    da.creatureCount
                )

                """|$mention : $deckCode
                |```$line1
                |$line2
                |$line3
                |$line4
                |
                |Class    : ${da.deckClassName} [${da.attributesText}]
                |Keywords : ${da.keywordsText}
                |
                |Unique   : ${da.totalUnique}
                |Total    : ${da.totalCards} (1s ${da.c1}, 2s ${da.c2}, 3s ${da.c3})
                |
                |Mana Curve
                |${da.createManaString()}```
                |""".trimMargin(marginPrefix = "|")
            }

            else -> "Unknown type: $type"
        }
        return listOf(reply)
    }
}

