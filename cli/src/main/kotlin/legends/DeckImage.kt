package legends

import legends.DeckAnalysis.ClassAbility
import legends.gfx.*
import tesl.model.Card
import tesl.model.Deck
import java.awt.*
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL
import javax.imageio.ImageIO
import kotlin.math.min

object DeckImage {
    private const val fontName = "FreeSans"

    fun from(deck: Deck, mention: String, username: String): ByteArray {
        val circRadius = 25
        val nameWidth = 250
        val leftMargin = 10
        val leftSpace = 10
        val topBlockHeight = 182
        val heightGap = 5
        val bottomMargin = 5
        val summaryTitleHeight = 50

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

        val width = 4 * (leftMargin + circRadius*4 + nameWidth) - leftMargin + circRadius - leftSpace // for right side circle
        val height = topBlockHeight + columnLengths[0] * (2 * circRadius + heightGap) + bottomMargin

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
                val x1 = i * (leftMargin + 4*circRadius + nameWidth) + circRadius + leftSpace
                val x2 = x1 + nameWidth + circRadius*2
                val y = topBlockHeight + j * (circRadius*2 + heightGap) + circRadius

                ////////////////////////////////////////////////////////////////////////////////////
                // IMAGES
                ////////////////////////////////////////////////////////////////////////////////////
                val imageData = getImageData(card)
                val cutdownImage = copySubImage(imageData, 70, 140, imageData.width - 150, 110)
                val scaledImage = scaleImage(cutdownImage, 0.5, 0.5)

                // CLOUD on left, CARD on right, dissolve between the two

                // CLOUD
                val cloudResource = this::class.java.classLoader.getResource("images/cloud-grey.png")
                val cloudImage = ImageIO.read(cloudResource)

                // Merge
                val mergedImage = GfxFade.mergeImages(cloudImage, scaledImage, x2-x1, circRadius*2 - 4, 0.65f)

                ////////////////////////////////////////////////////////////////////////////////////
                // FILLED BOX WITH CARD COLOURS
                // The alpha channel of the colours determine the opacity of the colour
                val cc = card.attributes
                    .map { ClassAbility.valueOf(it.toUpperCase()).classColour }
                    .sortedBy { it.name }

                val colourBoxWidth = (x2 - x1) * 14 / 20
                val colourBoxHeight = circRadius * 2 - 4
                val colourOverlay: BufferedImage = when(cc.size) {
                    1 -> GfxFade.createColourFade(cc[0].hexColor, cc[0].hexColor, colourBoxWidth, colourBoxHeight)
                    2 -> GfxFade.createColourFade(cc[0].hexColor, cc[1].hexColor, colourBoxWidth, colourBoxHeight,0.1f)
                    else -> GfxFade.createColourFade(cc[0].hexColor, cc[1].hexColor, cc[2].hexColor, colourBoxWidth, colourBoxHeight, 0.1f)
                }

                // now combine colourImage and mergedImage
                val finalImage = GfxFade.mergeImages(colourOverlay, mergedImage, x2-x1, circRadius*2 - 4, 0.55f, mergePercent = 0.15f)

                ig2.clipRect(x1, y-circRadius+2, x2-x1, circRadius*2 - 4)
                ig2.drawImage(finalImage, x1, y-circRadius+2, null)

                ig2.clip = null

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
                } else {
                    if (card.unique) {
                        val uniqueResource = this::class.java.classLoader.getResource("images/unique-28.png")
                        val uniqueImage = ImageIO.read(uniqueResource)
                        ig2.drawImage(uniqueImage, x2 - 13, y - 12, null)
                    }
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

                ////////////////////////////////////////////////////////////////////////////////////
                // NAME
                ////////////////////////////////////////////////////////////////////////////////////
                val nameMessage = card.name.substring(0, min(card.name.length, 26))
                ig2.font = Font(fontName, Font.PLAIN, 20)
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

        displayUser(ig2, username)

        // Summary details
        displayDeckDetailValue(ig2, "Creatures:", "${da.creatureCount}", 2, 0, 5, width, summaryTitleHeight)
        displayDeckDetailValue(ig2, "Soulgems:", "${da.soulGemCost}", 2, 1, 5, width, summaryTitleHeight)
        displayDeckDetailValue(ig2, "Count:", "${da.totalCards}", 2, 2, 5, width, summaryTitleHeight)

        displayDeckDetailValue(ig2, "Actions:", "${da.actionsCount}", 3, 0, 5, width, summaryTitleHeight)
        displayDeckDetailValue(ig2, "Items:", "${da.itemsCount}", 3, 1, 5, width, summaryTitleHeight)
        displayDeckDetailValue(ig2, "Supports:", "${da.supportsCount}", 3, 2, 5, width, summaryTitleHeight)
        displayDeckDetailValue(ig2, "Prophecies:", "${da.prophecyCount}", 3, 3, 5, width, summaryTitleHeight)

        displayDeckDetailValue(ig2, "Commons:", "${da.commonCount}", 4, 0, 5, width, summaryTitleHeight)
        displayDeckDetailValue(ig2, "Rares:", "${da.rareCount}", 4, 1, 5, width, summaryTitleHeight)
        displayDeckDetailValue(ig2, "Epics:", "${da.epicCount}", 4, 2, 5, width, summaryTitleHeight)
        displayDeckDetailValue(ig2, "Legendaries:", "${da.legendaryCount}", 4, 3, 5, width, summaryTitleHeight)

        // DECK CLASS
        // First get the abilities into the correct order as per the class colour order
        val attr = da.deckClass.classColours.map { DeckAnalysis.ClassAbility.fromColour(it) }.toMutableList()
        if (da.attributes.containsKey("Neutral")) attr.add(ClassAbility.NEUTRAL)

        val attributeCount = attr.map {
            it.name.toLowerCase() to da.attributes[it.name.toLowerCase().capitalize()]
        }.toMap()

        ig2.font = Font(fontName, Font.PLAIN, 30)
        var attIndex = 0
        attributeCount.forEach { (attribute, count) ->
            val iconResource = this::class.java.classLoader.getResource("images/${attribute}-50.png")
            val iconImage = ImageIO.read(iconResource)
            val x = 5 + attIndex++ * 108
            val y = topBlockHeight - 60
            ig2.drawImage(iconImage, x, y, null)

            ig2.paint = Color(0xd2, 0xcb, 0xfe)
            ig2.drawString("$count", x + 55, y + 35)
        }

        // Deck class name
        if (da.totalCards > 0) {
            ig2.font = Font(fontName, Font.PLAIN, 28)
            ig2.paint = Color(0xd2, 0xcb, 0xfe)
            ig2.drawString("(${da.deckClassName})", attIndex * 108 - 8, topBlockHeight - 26)
        }

        val baos = ByteArrayOutputStream()
        ImageIO.write(bi, "PNG", baos)
        // ImageIO.write(bi, "PNG", File("/tmp/out1.png"))

        return baos.toByteArray()
    }

    private fun copySubImage(image: BufferedImage, x: Int, y: Int, w: Int, h: Int): BufferedImage {
        // imageData.getSubimage(20, 130, imageData.width - 100, 110)
        val new = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
        val g = new.createGraphics()
        g.drawImage(image.getSubimage(x, y, w, h), 0, 0, null)
        g.dispose()
        return new
    }

    private fun scaleImage(image: BufferedImage, scaleX: Double, scaleY: Double): BufferedImage {
        val after = BufferedImage((image.width * scaleX).toInt(), (image.height * scaleY).toInt(), BufferedImage.TYPE_INT_ARGB)
        val at = AffineTransform.getScaleInstance(scaleX, scaleY)
        val scaleOp = AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR)
        return scaleOp.filter(image, after)
    }

    private fun Image.toBufferedImage(): BufferedImage {
        if (this is BufferedImage) {
            return this
        }
        val bufferedImage = BufferedImage(this.getWidth(null), this.getHeight(null), BufferedImage.TYPE_INT_ARGB)

        val graphics2D = bufferedImage.createGraphics()
        graphics2D.drawImage(this, 0, 0, null)
        graphics2D.dispose()

        return bufferedImage
    }

    private fun displayUser(g: Graphics2D, name: String) {
        val nameLen = name.length
        val fontSize = when {
            nameLen < 10 -> 85
            nameLen < 15 -> 70
            nameLen < 20 -> 56
            nameLen < 25 -> 44
            nameLen < 30 -> 36
            nameLen < 40 -> 27
            else -> 18
        }
        g.font = Font(fontName, Font.PLAIN, fontSize)
        val fm = g.fontMetrics
        val hName = fm.ascent
        g.paint = Color(0xc6, 0xc6, 0xc6)
        g.drawString(name, 5, 5 + hName)
    }

    private fun displayDeckDetailValue(g: Graphics2D, title: String, text: String, x: Int, y: Int, numCols: Int, width: Int, titleMargin: Int) {
        val fontSize = 30

        // Title
        g.font = Font(fontName, Font.PLAIN, fontSize)
        val fmTitle = g.fontMetrics
        val wTitle = fmTitle.stringWidth(title)
        val hTitle = fmTitle.ascent
        g.paint = Color(0x86, 0x86, 0x86)
        g.drawString(title, x*width/numCols + (width/numCols)*7/10 - wTitle - 10, hTitle + y * (fontSize + 15) + titleMargin - 45)

        // Text
        g.font = Font(fontName, Font.BOLD, fontSize)
        val fmText = g.fontMetrics
        val wText = fmText.stringWidth(text)
        val hText = fmText.ascent
        g.paint = Color(0xd2, 0xcb, 0xfe)
        g.drawString(text, x*width/numCols + (width/numCols)*7/10 + 10, hText + y * (fontSize + 15) + titleMargin - 45)

    }

    private fun getImageData(card: Card): BufferedImage {
        val sanitisedName = card.name.replace("/", "_")
        val localResource = this::class.java.classLoader.getResource("images/cards/${sanitisedName}.png")
        return when {
            localResource != null -> {
                ImageIO.read(localResource)
            }
            card.imageUrl != "" -> {
                ImageIO.read(URL(card.imageUrl))
            }
            else -> throw IOException("No URL available for card $card")
        }
    }

    fun calculateColumnLengths(total: Int, columnCount: Int): List<Int> {
        return (0 until columnCount).map { i ->
            total / columnCount + if (i < total % columnCount) 1 else 0
        }
    }
}
