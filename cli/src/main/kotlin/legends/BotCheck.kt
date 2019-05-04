package legends

import com.natpryce.konfig.*
import io.elderscrollslegends.Card
import io.elderscrollslegends.Deck
import io.github.nerd.discordkt.discord.auth.Authentication
import io.github.nerd.discordkt.discord.discord
import io.github.nerd.discordkt.discord.events.MessageRecvEvent
import legends.BotCheck.ClassColour.*

object BotCheck {

    private val token = Key("deck-check.bot.token", stringType)

    private val config = ConfigurationProperties.systemProperties() overriding
            EnvironmentVariables() overriding
            ConfigurationProperties.fromResource("defaults.properties")

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

    enum class ClassColour(val ability: String) {
        GREEN("Agility"),
        RED("Strength"),
        BLUE("Intelligence"),
        YELLOW("Willpower"),
        PURPLE("Endurance"),
        GREY("Neutral")
    }

    enum class ClassAbility(val classColour: ClassColour) {
        AGILITY(GREEN),
        STRENGTH(RED),
        INTELLIGENCE(BLUE),
        WILLPOWER(YELLOW),
        ENDURANCE(PURPLE),
        NEUTRAL(GREY)
    }

    enum class DeckClass(val classColours: Set<ClassColour>) {
        UNKNOWN(emptySet()),

        SINGLE_GREEN(setOf(GREEN)),
        SINGLE_RED(setOf(RED)),
        SINGLE_BLUE(setOf(BLUE)),
        SINGLE_YELLOW(setOf(YELLOW)),
        SINGLE_PURPLE(setOf(PURPLE)),

        ARCHER(setOf(GREEN, RED)),
        ASSASSIN(setOf(GREEN, BLUE)),
        BATTLEMAGE(setOf(BLUE, RED)),
        CRUSADER(setOf(RED, YELLOW)),
        MAGE(setOf(BLUE, YELLOW)),
        MONK(setOf(GREEN, YELLOW)),
        SCOUT(setOf(GREEN, PURPLE)),
        SORCERER(setOf(PURPLE, BLUE)),
        SPELLSWORD(setOf(PURPLE, YELLOW)),
        WARRIOR(setOf(PURPLE, RED)),

        HOUSE_DAGOTH(setOf(GREEN, BLUE, RED)),
        HOUSE_HLAALU(setOf(RED, YELLOW, GREEN)),
        HOUSE_REDORAN(setOf(RED, YELLOW, PURPLE)),
        HOUSE_TELVANNI(setOf(BLUE, GREEN, PURPLE)),
        TRIBUNAL_TEMPLE(setOf(BLUE, YELLOW, PURPLE)),

        ALDEMERI_DOMINION(setOf(BLUE, YELLOW, GREEN)),
        DAGGERFALL_COVENANT(setOf(PURPLE, RED, BLUE)),
        EBONHEART_PACT(setOf(GREEN, PURPLE, RED)),
        EMPIRE_OF_CYRODIIL(setOf(YELLOW, GREEN, PURPLE)),
        GUILDSWORN(setOf(RED, BLUE, YELLOW))
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

        val allClassColours = deck
            .cards
            .flatMap { it.attributes }
            .toSet()
            .map { BotCheck.ClassAbility.valueOf(it.toUpperCase()).classColour }
            .toSet()

        val withoutNeutral = allClassColours - GREY
        val deckClass = DeckClass
            .values()
            .find { it.classColours == withoutNeutral } ?: BotCheck.DeckClass.UNKNOWN

        val deckClassName = deckClass
            .name
            .replace("_", " ")
            .toLowerCase()
            .split(" ")
            .joinToString(" ") { it.capitalize() }

        val attributesText = groupAndCountAsText(deck) { it.attributes }
        val keywordsText = groupAndCountAsText(deck) { it.keywords }

        val legendaryCount = byRarity["Legendary"]?.size ?: 0
        val epicCount = byRarity["Epic"]?.size ?: 0
        val rareCount = byRarity["Rare"]?.size ?: 0
        val commonCount = byRarity["Common"]?.size ?: 0

        val creatures = byType("Creature", deck)
        val actions = byType("Action", deck)
        val items = byType("Item", deck)
        val supports = byType("Support", deck)

        val creatureCount = creatures.flatMap { it.value }.toList().size
        val actionsCount = actions.flatMap { it.value }.toList().size
        val itemsCount = items.flatMap { it.value }.toList().size
        val supportsCount = supports.flatMap { it.value }.toList().size

        val c1 = deck.of(1).size
        val c2 = deck.of(2).size
        val c3 = deck.of(3).size

        val totalUnique = c1 + c2 + c3
        val totalCards = c1 + c2 * 2 + c3 * 3

        val reply = when (type) {
            "info", "detail" -> {
                val line1 = String.format("%10s: %-5d   %10s: %-5d", "Common", commonCount, "Actions", actionsCount)
                val line2 = String.format("%10s: %-5d   %10s: %-5d", "Rare", rareCount, "Items", itemsCount)
                val line3 = String.format("%10s: %-5d   %10s: %-5d", "Epic", epicCount, "Support", supportsCount)
                val line4 = String.format("%10s: %-5d   %10s: %-5d", "Legendary", legendaryCount, "Creatures", creatureCount)

                var s = """
                ${event.author.mention} : $deckCode
                ```
                $line1
                $line2
                $line3
                $line4

                Class    : $deckClassName [$attributesText]
                Keywords : $keywordsText

                Unique   : $totalUnique
                Total    : $totalCards (1s $c1, 2s $c2, 3s $c3)
                """.trimIndent()

                if (type == "detail") {
                    s = """$s

Mana Curve
${createManaString(deck)}
""".trimIndent()
                }
                s += "```"
                s
            }

            else -> "Unknown type: $type"
        }

        event.reply(reply)
    }

    private fun groupAndCountAsText(deck: Deck, mapper: (card: Card) -> List<String>): String {
        return deck
            .cards
            .flatMap { mapper(it) }
            .groupBy { it }
            .map { (k, v) -> k to v.size }
            .toMap()
            .map { (name, count) ->
                "$name: $count"
            }.joinToString(", ")
    }

    private fun byType(type: String, deck: Deck): Map<String, List<Card>> {
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
                name to typeCards
            }
            .toMap()
    }

    private fun createManaString(deck: Deck): String {
        val costToCountMap = deck.cards
            .groupBy { it.cost }
            .toSortedMap()
            .map { entry ->
                val cost = entry.key
                val count = entry.value
                    .groupBy { card -> card.name }
                    .map { it.value.first() }
                    .count()
                (cost to count)
            }
            .toMap()

        val costData = mutableMapOf<Int, Int>()
        (0..30).forEach { cost ->
            val x = if (cost < 8) cost else 7
            val y = costToCountMap[cost] ?: 0
            val sevenPlus = costData.getOrDefault(7, 0)
            costData[x] = sevenPlus + y
        }

        val maxValue = costData.values.max()!!
        val maxValueLength = "$maxValue".length
        val increment = maxValue / 20.0
        val maxLabelLength = 4

        return costData.map { (cost, count) ->
            val barChunks = ((count * 8) / increment).toInt().div(8)
            val remainder = ((count * 8) / increment).toInt().rem(8)

            var bar = "█".repeat(barChunks)
            if (remainder > 0) {
                bar += ('█'.toInt() + (8 - remainder)).toChar()
            }
            if (bar == "") {
                bar = "▏"
            }

            val costText = if (cost < 7) "$cost" else "7+"
            " ${costText.padEnd(maxLabelLength)}| ${count.toString().padEnd(maxValueLength)} $bar"
        }.joinToString("\n")

    }
}