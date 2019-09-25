package legends

import io.elderscrollslegends.Card
import io.elderscrollslegends.CardCache
import io.elderscrollslegends.Deck
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
import java.net.URL
import javax.imageio.ImageIO

class DeckImageTest {

    @Test
    fun `column lengths for given counts`() {
        assertThat(DeckImage.calculateColumnLengths(1,4)).isEqualTo(listOf(1, 0, 0, 0))
        assertThat(DeckImage.calculateColumnLengths(2,4)).isEqualTo(listOf(1, 1, 0, 0))
        assertThat(DeckImage.calculateColumnLengths(3,4)).isEqualTo(listOf(1, 1, 1, 0))
        assertThat(DeckImage.calculateColumnLengths(4,4)).isEqualTo(listOf(1, 1, 1, 1))
        assertThat(DeckImage.calculateColumnLengths(5,4)).isEqualTo(listOf(2, 1, 1, 1))
        assertThat(DeckImage.calculateColumnLengths(25,3)).isEqualTo(listOf(9, 8, 8))
    }

    @Test
    @Disabled("while experimenting")
    fun `write png`() {
        val c1 = Card(id = "1", name = "abcdefghij abcdefghij abcdefghij abcdefghij", cost = 0)
        val c2 = Card(id = "2", name = "b named card here", cost = 0)
        val c3 = Card(id = "3", name = "c named card here", cost = 0)
        val c4 = Card(id = "4", name = "d named card here", cost = 0)
        val c5 = Card(id = "5", name = "e named card here", cost = 1)
        val c6 = Card(id = "6", name = "f named card here", cost = 1)
        val c7 = Card(id = "7", name = "g named card here", cost = 1)
        val c8 = Card(id = "8", name = "h named card here", cost = 1)
        val c9 = Card(id = "9", name = "i named card here", cost = 2)
        val c10 = Card(id = "10", name = "j named card here", cost = 2)
        val c11 = Card(id = "11", name = "k named card here", cost = 2)
        val c12 = Card(id = "12", name = "l named card here", cost = 2)
        val c13 = Card(id = "13", name = "m named card here", cost = 2)
        val c14 = Card(id = "14", name = "n named card here", cost = 2)
        val c15 = Card(id = "15", name = "o named card here", cost = 2)
        val c16 = Card(id = "16", name = "p named card here", cost = 3)
        val c17 = Card(id = "17", name = "q named card here", cost = 3)
        val c18 = Card(id = "18", name = "r named card here", cost = 3)
        val c19 = Card(id = "19", name = "s named card here", cost = 3)
        val c20 = Card(id = "20", name = "t named card here", cost = 3)
        val c21 = Card(id = "21", name = "u named card here", cost = 4)
        val c22 = Card(id = "22", name = "v named card here", cost = 4)
        val c23 = Card(id = "23", name = "w named card here", cost = 4)
        val c24 = Card(id = "24", name = "x named card here", cost = 7)
        val c25 = Card(id = "25", name = "y named card here", cost = 12)
        val deck = Deck(cards = listOf(c1, c1, c2, c2, c3, c4, c5, c5, c5, c6, c7, c8, c9, c10, c10, c11, c12, c12, c13, c13, c14, c15, c16, c17, c17, c18, c18, c19, c20, c21, c22, c22, c23, c24, c25))
        DeckImage.from(deck, "foo", "bar")
    }

    @Test
    @Disabled
    fun `real deck image loading`() {
        println("loading cards...")
        CardCache.load()
        println("... finished")
        val deck = Deck.importCode("SPAMsIrggOqolHhFnNajuxqktDeiADmooEcOATuznAbDcxgstmqyhnnwqNfBfPkveDlYqBlImlqT")
        DeckImage.from(deck, "foo", "bar")

    }

    @Test
    @Disabled("One off for getting the images into project")
    fun `download images to resource dir`() {
        println("Reading cards....")
        val cards = Card.all()
        println("... done")
        println("writing images...")
        cards.forEach { card ->
            println("saving ${card.name}")
            val path = File("/home/markf/dev/personal/gaming/deck-check/cli/src/test/resources/images/cards/${sanitize(card.name)}.png")
            if (!path.exists()) {
                val image = ImageIO.read(URL(card.imageUrl))
                if (image != null) {
                    ImageIO.write(image, "PNG", path)
                } else {
                    println ("ERROR: No image for card: $card")
                }
            }
        }
        println("... done")
    }

    private fun sanitize(s: String) = s.replace("/", "_")
}