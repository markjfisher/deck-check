package legends

import com.jessecorbett.diskord.api.rest.Embed
import com.jessecorbett.diskord.api.rest.EmbedAuthor
import com.jessecorbett.diskord.api.rest.EmbedImage
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

    fun show(
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
