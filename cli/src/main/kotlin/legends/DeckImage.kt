package legends

import io.elderscrollslegends.Deck

object DeckImage {
    fun from(deck: Deck, mention: String, username: String): ByteArray {
        val da = DeckAnalysis(deck)
        val numCards = da.totalUnique
        val columnLengths: List<Int> = calculateColumnLengths(numCards, 4)

        val orderedCards = deck.cards

        return ByteArray(0)
    }

    fun calculateColumnLengths(total: Int, columnCount: Int): List<Int> {
        return (0 until columnCount).map { i ->
            total / columnCount + if (i < total % columnCount) 1 else 0
        }
    }
}
