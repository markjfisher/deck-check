package legends

import com.jessecorbett.diskord.dsl.block
import com.jessecorbett.diskord.dsl.bot
import com.jessecorbett.diskord.dsl.command
import com.jessecorbett.diskord.dsl.commands
import com.jessecorbett.diskord.util.mention
import com.jessecorbett.diskord.util.words
import com.natpryce.konfig.*
import io.elderscrollslegends.Deck

object BotCheck {

    private val token = Key("deck-check.bot.token", stringType)

    private val config = ConfigurationProperties.systemProperties() overriding
            EnvironmentVariables() overriding
            ConfigurationProperties.fromResource("deck-check.properties")

    @JvmStatic
    fun main(args: Array<String>) {
        //runBlocking {}
        bot(config[token]) {
            commands(prefix = "!") {
                command(command = "deck") {
                    val deckArgs = words.drop(1)
                    val deckCommand = DeckCommands.values().find { it.cmd == deckArgs[0] } ?: BotCheck.DeckCommands.CMD_HELP
                    val replyString = deckCommand.run(deckArgs.drop(1), author.mention, author.username)
                    reply(replyString)
                }
            }
            started {
                println("started with sessionId: ${it.sessionId}")
            }
            this.anyEvent { event, json ->
                println("${event.name} -> $json")
            }

            block()
        }
    }

    enum class DeckCommands(val cmd: String) {
        CMD_HELP("help") {
            override fun run(args: List<String>, mention: String, username: String): String {
                val allCommands = values().map { it.cmd }
                return "help: Known commands: ${allCommands.joinToString(", ")}"
            }
        },
        CMD_INFO("info") {
            override fun run(args: List<String>, mention: String, username: String): String {
                return show(args, mention, username, "info")
            }
        },
        CMD_DETAIL("detail") {
            override fun run(args: List<String>, mention: String, username: String): String {
                return show(args, mention, username, "detail")
            }
        };

        abstract fun run(args: List<String>, mention: String, username: String): String
    }

    private fun show(
        args: List<String>,
        mention: String,
        username: String,
        type: String
    ): String {
        if (args.size != 1) {
            return "Please supply a single deck code."
        }
        val deckCode = args[0]
        println("User: $username asked for info on deck $deckCode")
        val deck = Deck.importCode(deckCode)
        val da = DeckAnalysis(deck)

        return when (type) {
            "info", "detail" -> {
                val line1 = String.format("%-10s: %-5d   %-10s: %-5d", "Common", da.commonCount, "Actions", da.actionsCount)
                val line2 = String.format("%-10s: %-5d   %-10s: %-5d", "Rare", da.rareCount, "Items", da.itemsCount)
                val line3 = String.format("%-10s: %-5d   %-10s: %-5d", "Epic", da.epicCount, "Support", da.supportsCount)
                val line4 = String.format("%-10s: %-5d   %-10s: %-5d", "Legendary", da.legendaryCount, "Creatures", da.creatureCount)

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
    }
}