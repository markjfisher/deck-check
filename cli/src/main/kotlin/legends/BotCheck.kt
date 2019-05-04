package legends

import com.natpryce.konfig.*
import io.elderscrollslegends.Deck
import io.github.nerd.discordkt.discord.auth.Authentication
import io.github.nerd.discordkt.discord.discord
import io.github.nerd.discordkt.discord.events.MessageRecvEvent

object BotCheck {

    private val token = Key("deck-check.bot.token", stringType)

    private val config = ConfigurationProperties.systemProperties() overriding
            EnvironmentVariables() overriding
            ConfigurationProperties.fromResource("deck-check.properties")

    @JvmStatic
    fun main(args: Array<String>) {
        val discord = discord(Authentication.bot(config[token])).get()

        discord.on<MessageRecvEvent> { event ->
            if (event.content.startsWith("!ping", ignoreCase = true)) {
                event.reply("Pong! ${discord.emojis["ping_pong"]}")
            }

            val content = event.content
            when {
                content.startsWith("!deck ") -> deckCheck(content, event)
            }
        }
    }

    private fun deckCheck(content: String, event: MessageRecvEvent) {
        val args = content.split(" ")
        if (args.size < 2) {
            DeckCommands.CMD_HELP.run(listOf(), event)
            return
        }

        val deckCommand = DeckCommands.values().find { it.cmd == args[1] } ?: BotCheck.DeckCommands.CMD_HELP
        deckCommand.run(args.subList(2, args.size), event)
    }

    enum class DeckCommands(val cmd: String) {
        CMD_HELP("help") {
            override fun run(args: List<String>, event: MessageRecvEvent) {
                val allCommands = values().map { it.cmd }
                event.reply("help: Known commands: ${allCommands.joinToString(", ")}")
            }
        },
        CMD_INFO("info") {
            override fun run(args: List<String>, event: MessageRecvEvent) {
                show(args, event, "info")
            }
        },
        CMD_DETAIL("detail") {
            override fun run(args: List<String>, event: MessageRecvEvent) {
                show(args, event, "detail")
            }
        };

        abstract fun run(args: List<String>, event: MessageRecvEvent)
    }

    private fun show(
        args: List<String>,
        event: MessageRecvEvent,
        type: String
    ) {
        val deckCode = args[0]
        println("User: ${event.author.username} asked for info on deck $deckCode")
        val deck = Deck.importCode(deckCode)
        val da = DeckAnalysis(deck)

        val reply = when (type) {
            "info", "detail" -> {
                val line1 = String.format("%10s: %-5d   %10s: %-5d", "Common", da.commonCount, "Actions", da.actionsCount)
                val line2 = String.format("%10s: %-5d   %10s: %-5d", "Rare", da.rareCount, "Items", da.itemsCount)
                val line3 = String.format("%10s: %-5d   %10s: %-5d", "Epic", da.epicCount, "Support", da.supportsCount)
                val line4 = String.format("%10s: %-5d   %10s: %-5d", "Legendary", da.legendaryCount, "Creatures", da.creatureCount)

                """
                ${event.author.mention} : $deckCode
                ```
                $line1
                $line2
                $line3
                $line4

                Class    : ${da.deckClassName} [${da.attributesText}]
                Keywords : ${da.keywordsText}

                Unique   : ${da.totalUnique}
                Total    : ${da.totalCards} (1s ${da.c1}, 2s ${da.c2}, 3s ${da.c3})

                Mana Curve
                ${da.createManaString()}
                ```""".trimIndent()
            }

            else -> "Unknown type: $type"
        }

        event.reply(reply)
    }
}