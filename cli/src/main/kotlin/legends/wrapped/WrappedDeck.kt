package legends.wrapped

import io.elderscrollslegends.Deck

class WrappedDeck(private val deck: Deck) {
    fun hasCard(name: String): Boolean {
        return deck.cards.find { it.name.toLowerCase() == name.toLowerCase() } != null
    }

    fun cardCount(name: String): Int {
        return deck.cards.count { it.name.toLowerCase() == name.toLowerCase() }
    }
}