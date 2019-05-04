package legends

import io.elderscrollslegends.Card
import io.elderscrollslegends.Deck

class DeckCheck {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            // An example deck if one isn't provided
            val code = "SPACgRnUAAAQdYkYdeeQlLmgpDhUkzkNrWaUcMfvoRrY"

            val importCode = if (args.isEmpty()) code else args[0]
            val deck = Deck.importCode(importCode)

            val da = DeckAnalysis(deck)

            val line1 = String.format("%10s: %-5d   %10s: %-5d", "Common", da.commonCount, "Actions", da.actionsCount)
            val line2 = String.format("%10s: %-5d   %10s: %-5d", "Rare", da.rareCount, "Items", da.itemsCount)
            val line3 = String.format("%10s: %-5d   %10s: %-5d", "Epic", da.epicCount, "Support", da.supportsCount)
            val line4 = String.format("%10s: %-5d   %10s: %-5d", "Legendary", da.legendaryCount, "Creatures", da.creatureCount)

            println("""
Summary:
$line1
$line2
$line3
$line4

Class    : ${da.deckClassName} [${da.attributesText}]
Keywords : ${da.keywordsText}

Unique   : ${da.totalUnique}
Total    : ${da.totalCards} (1s ${da.c1}, 2s ${da.c2}, 3s ${da.c3})

Mana Curve
${da.createManaString()}""".trimIndent())

            // Creatures
            printTypeData("Action", deck.cards)
            printTypeData("Item", deck.cards)
            printTypeData("Support", deck.cards)
            printTypeData("Creature", deck.cards)

        }

        private fun printTypeData(type: String, cards: List<Card>) {
            print("\n$type")
            cards
                .filter { it.type == type }
                .also {
                    println(", count: ${it.size}")
                }
                .sortedBy { it.name }
                .groupBy { it.name }
                .forEach { (name, typeCards) ->
                    val first = typeCards.first()
                    val cost = first.cost
                    val power = if (first.power >= 0) "${first.power}" else "-"
                    val health = if (first.health >= 0) "${first.health}" else "-"
                    println("  $name, count: ${typeCards.size}, [$cost/$power/$health], attr: [${typeCards.first().attributes.joinToString(", ")}]")
                }

        }
    }
}