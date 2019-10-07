package legends.wrapped

import tesl.model.Card
import tesl.model.Deck

class WrappedDeck(private val deck: Deck) {
    fun hasCard(name: String): Boolean {
        return deck.cards.find { it.name.toLowerCase() == name.toLowerCase() } != null
    }

    fun cardCount(name: String): Int {
        return deck.cards.count { it.name.toLowerCase() == name.toLowerCase() }
    }

    fun of(count: Int): List<Card> = deck.of(count)
}