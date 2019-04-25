package legends

import io.elderscrollslegends.Card
import io.elderscrollslegends.Deck
import io.elderscrollslegends.Type


class DeckCheck {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            // An example deck if one isn't provided
            val code = "SPACgRnUAAAQdYkYdeeQlLmgpDhUkzkNrWaUcMfvoRrY"

            val importCode = if (args.isEmpty()) code else args[0]
            val deck = Deck.importCode(importCode)

            println("\nSet Types:")
            deck.cards.groupBy { it.set.name }.forEach { (setName, cardsInSet) ->
                println("  $setName: ${cardsInSet.size}")
            }

            println("\nRarity:")
            deck.cards
                .sortedBy { it.name }
                .groupBy { it.rarity }
                .forEach { (rarity, cards) ->
                    println("  $rarity, count: ${cards.size}")
                }

            Type.all().forEach { type ->
                printTypeData(type, deck.cards)
            }

            val c1 = deck.of(1).size
            val c2 = deck.of(2).size
            val c3 = deck.of(3).size

            println("\nTotal unique cards: ${c1 + c2 + c3}\nTotal cards: ${c1 + c2 * 2 + c3 * 3}")

            // Curve graph
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

            printBarGraph(costData)

        }

        private fun printBarGraph(data: Map<Int, Int>) {
            val maxValue = data.values.max()!!
            val maxValueLength = "$maxValue".length
            val increment = maxValue / 60.0
            val maxLabelLength = 4

            println("\nCost Curve\n")
            data.forEach { (cost, count) ->
                val barChunks = ((count * 8) / increment).toInt().div(8)
                val remainder = ((count * 8) / increment).toInt().rem(8)

                var bar = "█".repeat(barChunks)
                if (remainder > 0) {
                    bar += ('█'.toInt() + (8-remainder)).toChar()
                }
                if (bar == "") {
                    bar = "▏"
                }

                val costText = if (cost < 7) "$cost" else "7+"
                println(" ${costText.padEnd(maxLabelLength)}| ${count.toString().padEnd(maxValueLength)} $bar")
            }

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