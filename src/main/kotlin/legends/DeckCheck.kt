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

            println("\ntotal unique cards: ${c1 + c2 + c3}, total cards: ${c1 + c2 * 2 + c3 * 3}")

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