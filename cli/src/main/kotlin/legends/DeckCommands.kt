package legends

import com.jessecorbett.diskord.api.rest.Embed
import com.jessecorbett.diskord.api.rest.EmbedAuthor
import com.jessecorbett.diskord.api.rest.EmbedImage
import io.elderscrollslegends.Card
import io.elderscrollslegends.Deck
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

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

    fun show(args: List<String>, mention: String, username: String, type: String): List<String> {
        if (args.size != 1) {
            return listOf("$mention: Please supply a single deck code.")
        }

        val deckCode = args[0]
        logger.info { "User: $username asked for $type for code: $deckCode" }
        val deck = Deck.importCode(deckCode)
        val da = DeckAnalysis(deck)

        val reply = when (type) {
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
                |Subtypes : ${da.subtypes.joinToString(", ")}
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

        return if (type == "detail") {
            val creatureData = collectCardData(deck, "Creature")
            val actionData = collectCardData(deck, "Action")
            val itemData = collectCardData(deck, "Item")
            val supportData = collectCardData(deck, "Support")
            val detailCreatures = if (creatureData.isNotBlank()) "$mention\nCreatures:```$creatureData```" else null
            val detailActions = if (actionData.isNotBlank()) "$mention\nActions:```$actionData```" else null
            val detailItems = if (itemData.isNotBlank()) "$mention\nItems:```$itemData```" else null
            val detailSupports = if (supportData.isNotBlank()) "$mention\nSupports:```$supportData```" else null
            listOf(reply, detailCreatures, detailActions, detailItems, detailSupports).mapNotNull { it }
        } else {
            listOf(reply)
        }
    }

    private fun collectCardData(deck: Deck, type: String): String {
        val maxCardNameLength = deck.cards.fold(0) { max, c ->
            if (c.name.length > max) c.name.length else max
        }

        val of1Data = byType(deck.of(1), 1, type, maxCardNameLength)
        val of2Data = byType(deck.of(2), 2, type, maxCardNameLength)
        val of3Data = byType(deck.of(3), 3, type, maxCardNameLength)
        return listOf(of1Data, of2Data, of3Data).mapNotNull { if (it.isBlank()) null else it }.joinToString("\n")
    }

    private fun byType(
        cards: List<Card>,
        size: Int,
        type: String,
        maxCardNameLength: Int
    ): String {

        return cards
            .asSequence()
            .filter { it.type == type }
            .sortedBy { it.cost }
            .map { card ->
                val cost = card.cost
                val power = if (card.power >= 0) "${card.power}" else "-"
                val health = if (card.health >= 0) "${card.health}" else "-"
                val longest = maxCardNameLength.toString()
                val namesString = String.format("%-${longest}s", card.name.take(maxCardNameLength))
                val typesString = if (card.subtypes.isNotEmpty()) "| ${card.subtypes.joinToString(",")}" else ""
                val cphString = "[$cost/$power/$health]"
                val rarityString = String.format("%-6s", card.rarity.take(6))
                "$size x $namesString $cphString $rarityString $typesString"

            }
            .joinToString("\n")

    }

}
