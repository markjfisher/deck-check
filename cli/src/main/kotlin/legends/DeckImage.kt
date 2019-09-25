package legends

import io.elderscrollslegends.Card
import io.elderscrollslegends.Deck
import legends.gfx.*
import legends.gfx.Point
import java.awt.*
import java.awt.color.ColorSpace
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import java.awt.image.ColorConvertOp
import java.awt.image.FilteredImageSource
import java.io.File
import java.io.IOException
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.GrayFilter
import kotlin.math.min

object DeckImage {
    fun from(deck: Deck, mention: String, username: String): ByteArray {
        val circRadius = 25
        val nameWidth = 250
        val leftMargin = 10
        val rightMargin = 30
        val topBlockHeight = 180
        val heightGap = 3
        val bottomMargin = 20

//        GraphicsEnvironment.getLocalGraphicsEnvironment().allFonts.forEach {
//            println(it)
//        }

        val fontName = "FreeSans"
        val da = DeckAnalysis(deck)
        val numCards = da.totalUnique
        val columnLengths: List<Int> = calculateColumnLengths(numCards, 4)

        val orderedCards = da.cardCountSorted

        val cardsInColumns = listOf(
            orderedCards.take(columnLengths[0]),
            orderedCards.drop(columnLengths[0]).take(columnLengths[1]),
            orderedCards.drop(columnLengths[0] + columnLengths[1]).take(columnLengths[2]),
            orderedCards.drop(columnLengths[0] + columnLengths[1] + columnLengths[2]).take(columnLengths[3])
        )

        val width = 4 * (leftMargin + circRadius*4 + nameWidth)
        val height = topBlockHeight + columnLengths[0] * (2 * circRadius + heightGap) + bottomMargin + 5

        val bi = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val ig2 = bi.createGraphics()
        ig2.color = Color.BLACK
        ig2.fillRect(0, 0, width, height)

        ig2.color = Color.DARK_GRAY
        ig2.drawBox(Point(1, 1), Point(width - 1, height - 1))
        ig2.drawLine(Point(5, topBlockHeight - 5), Point(width - 5, topBlockHeight - 5))

        cardsInColumns.forEachIndexed { i, list ->
            list.forEachIndexed { j, (count, card) ->
                ig2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                )
                val x1 = i * (leftMargin + 4*circRadius + nameWidth) + rightMargin
                val x2 = x1 + nameWidth + circRadius*2
                val y = topBlockHeight + 5 + circRadius + j * (circRadius*2 + 5)

                ////////////////////////////////////////////////////////////////////////////////////
                // IMAGE
                ////////////////////////////////////////////////////////////////////////////////////
                val imageData = getImageData(card)
                val cutdownImage = imageData.getSubimage(55, 162, 300, 60)

                // method a
                val gray = ColorSpace.getInstance(ColorSpace.CS_GRAY)
                val op = ColorConvertOp(gray, null)
                val grayImage = op.filter(cutdownImage, null)

                // method b
                val grayFilter = GrayFilter(true, 15)
                val producer = FilteredImageSource(cutdownImage.source, grayFilter)
                val grayImage2 = Toolkit.getDefaultToolkit().createImage(producer)

                // method c
                // val grayImage3 = Greyscale.toGray305911(cutdownImage)

                ig2.clipRect(x1, y-circRadius+2, x2-x1, circRadius*2 - 4)
                ig2.drawImage(grayImage2, x1 + ((x2-x1)/3.5).toInt(), y-circRadius+2, null)

                ig2.clip = null

                ////////////////////////////////////////////////////////////////////////////////////
                // FILLED BOX WITH CARD COLOUR
                val cc = card.attributes
                    .map { DeckAnalysis.ClassAbility.valueOf(it.toUpperCase()).classColour }
                    .sortedBy { it.name }

                val p1 = Point2D.Float(x1.toFloat(), y.toFloat())
                val p2 = Point2D.Float(x2.toFloat(), y.toFloat())
                val gradientPaint: Paint = when(cc.size) {
                    1 -> {
                        cc[0].hexColor
                    }
                    2 -> {
                        val dist = floatArrayOf(0.0f, 1.0f)
                        val colors = listOf(cc[0].hexColor, cc[1].hexColor).toTypedArray()
                        LinearGradientPaint(p1, p2, dist, colors)
                    }
                    else -> {
                        val dist = floatArrayOf(0.0f, 0.5f, 1.0f)
                        val colors = listOf(cc[0].hexColor, cc[1].hexColor, cc[2].hexColor).toTypedArray()
                        LinearGradientPaint(p1, p2, dist, colors)
                    }
                }
                ig2.paint = gradientPaint
                // ig2.fillRect(x1, y - circRadius + 2, x2 - x1, circRadius * 2 - 4)

                ////////////////////////////////////////////////////////////////////////////////////
                // CONNECTING PARALLEL LINES (will be overwritten by circles)
                ////////////////////////////////////////////////////////////////////////////////////
                ig2.color = Color.BLUE
                ig2.drawLine(Point(x1, y - circRadius + 2), Point(x2, y - circRadius + 2))
                ig2.drawLine(Point(x1, y + circRadius - 2), Point(x2, y + circRadius - 2))

                ////////////////////////////////////////////////////////////////////////////////////
                // LEFT OUTER CIRCLE
                ig2.fillCircle(Point(x1, y), circRadius.toDouble())
                // RIGHT OUTER (OR LINE)
                if (count > 1) {
                    ig2.fillCircle(Point(x2, y), circRadius.toDouble())
                } else {
                    ig2.drawLine(Point(x2, y - circRadius + 2), Point(x2, y + circRadius - 2))
                }

                ////////////////////////////////////////////////////////////////////////////////////
                // LEFT INNER
                ig2.color = Color.CYAN
                ig2.fillCircle(Point(x1, y), (circRadius - 2).toDouble())
                // RIGHT INNER
                if (count > 1) {
                    ig2.color = Color.BLACK
                    ig2.fillCircle(Point(x2, y), (circRadius - 2).toDouble())
                }

                ////////////////////////////////////////////////////////////////////////////////////
                // LEFT NUMBER = cost
                ////////////////////////////////////////////////////////////////////////////////////
                val costMessage = "${card.cost}"
                ig2.font = Font(fontName, Font.BOLD, 25)
                val fmCost = ig2.fontMetrics
                val wCost = fmCost.stringWidth(costMessage)
                val hCost = fmCost.ascent
                ig2.paint = Color.BLACK
                ig2.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                )
                ig2.drawString(costMessage, if (costMessage.length == 1) x1 - 7 else x1 - wCost / 2 - 2, y + hCost / 2 - 2)

                ////////////////////////////////////////////////////////////////////////////////////
                // RIGHT NUMBER = COUNT (if > 1)
                ////////////////////////////////////////////////////////////////////////////////////
                if (count > 1) {
                    val countMessage = "$count"
                    val fmCount = ig2.fontMetrics
                    val wCount = fmCount.stringWidth(countMessage)
                    val hCount = fmCount.ascent
                    ig2.paint = Color.WHITE
                    ig2.drawString(countMessage, x2 + wCount / 2 - 14, y + hCount / 2 - 2)
                }

                val nameMessage = card.name.substring(0, min(card.name.length, 26))
                ig2.font = Font(fontName, Font.BOLD, 20)
                val fmName = ig2.fontMetrics
                val hName = fmName.ascent
                ig2.paint = Color.WHITE
                ig2.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                )
                ig2.drawString(nameMessage, x1 + 22 + circRadius/2, y + hName / 2 - 2)
            }
        }

        ImageIO.write(bi, "PNG", File("/tmp/out1.png"))

        return ByteArray(0)
    }

    private fun getImageData(card: Card): BufferedImage {
        val sanitisedName = card.name.replace("/", "_")
        val localResource = this::class.java.classLoader.getResource("images/cards/${sanitisedName}.png")
        return if (localResource != null) {
            println("reading image from file resource for ${card.name}")
            ImageIO.read(localResource)
        } else if (card.imageUrl != "") {
            println("reading image from externam url ${card.imageUrl}")
            ImageIO.read(URL(card.imageUrl))
        } else {
            throw IOException("No URL available for card $card")
        }
    }

    fun calculateColumnLengths(total: Int, columnCount: Int): List<Int> {
        return (0 until columnCount).map { i ->
            total / columnCount + if (i < total % columnCount) 1 else 0
        }
    }
}
