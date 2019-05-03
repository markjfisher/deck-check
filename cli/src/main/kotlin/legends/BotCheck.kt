package legends

import io.elderscrollslegends.Card
import io.elderscrollslegends.Deck
import io.github.nerd.discordkt.discord.auth.Authentication
import io.github.nerd.discordkt.discord.discord
import io.github.nerd.discordkt.discord.events.MessageRecvEvent


object BotCheck {
    val deckCommands = listOf(BotCheck.DeckCommands.CMD_HELP, BotCheck.DeckCommands.CMD_INFO)

    @JvmStatic
    fun main(args: Array<String>) {
        val discord = discord(Authentication.bot("NTczNzkwMDM2NzM5NDg5ODIy.XMv_zw.dTaEuRU8ad47GW-l9HZ2VkLGIKI")).get()

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
        val byRarity = deck.cards
            .sortedBy { it.name }
            .groupBy { it.rarity }
            .map { (rarity, cards) ->
                println("  $rarity, count: ${cards.size}")
                rarity to cards
            }
            .toMap()

        val legendaryCount = byRarity["Legendary"]?.size ?: 0
        val epicCount = byRarity["Epic"]?.size ?: 0
        val rareCount = byRarity["Rare"]?.size ?: 0
        val commonCount = byRarity["Common"]?.size ?: 0

        val creatures = byType("Creature", deck)
        val actions = byType("Action", deck)
        val items = byType("Item", deck)
        val supports = byType("Support", deck)

        val c1 = deck.of(1).size
        val c2 = deck.of(2).size
        val c3 = deck.of(3).size

        val totalUnique = c1 + c2 + c3
        val totalCards = c1 + c2 * 2 + c3 * 3

        val reply = when(type) {
            "info", "detail" ->
                """
                ${event.author.mention} : $deckCode
                ```Common   : $commonCount
                Rare     : $rareCount
                Epic     : $epicCount
                Legendary: $legendaryCount

                Actions  : ${actions.size}
                Items    : ${items.size}
                Support  : ${supports.size}
                Creatures: ${creatures.size}

                Unique   : $totalUnique
                Total    : $totalCards```
                """.trimIndent()

            else -> "Unknown type: $type"
        }

        event.reply(reply)
    }

    private fun byType(type: String, deck: Deck): Map<String, Card> {
        println("$type:")
        return deck.cards
            .filter { it.type == type }
            .sortedBy { it.name }
            .groupBy { it.name }
            .map { (name, typeCards) ->
                val first = typeCards.first()
                val cost = first.cost
                val power = if (first.power >= 0) "${first.power}" else "-"
                val health = if (first.health >= 0) "${first.health}" else "-"
                println(
                    "  $name, count: ${typeCards.size}, [$cost/$power/$health], attr: [${first.attributes.joinToString(
                        ", "
                    )}]"
                )
                name to first
            }
            .toMap()
    }

}