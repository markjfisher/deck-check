package legends

import legends.DeckAnalysis.ClassAbility
import legends.gfx.*
import tesl.model.Card
import tesl.model.Deck
import java.awt.*
import java.awt.geom.AffineTransform
import java.awt.geom.RoundRectangle2D
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.IOException
import java.net.URL
import javax.imageio.ImageIO
import kotlin.math.min

object DeckImage {
    private const val fontName = "FreeSans"
    private const val numCols = 7
    private const val circRadius = 25
    private const val nameWidth = 250
    private const val leftMargin = 10
    private const val leftSpace = 10
    private const val topBlockHeight = 320
    private const val heightGap = 8
    private const val bottomMargin = 5
    private const val width = 4 * (leftMargin + circRadius*4 + nameWidth) - leftMargin + circRadius - leftSpace
    private const val scaleX: Double = 0.5
    private const val scaleY: Double = 0.5
    private const val allBoxTop = 5
    private const val allBoxHeight = topBlockHeight - 15
    private const val summaryTitleLeft = width * 50 / 100
    private const val summaryTitleWidth = width * 15 / 100
    private const val manaBoxLeft = summaryTitleLeft + summaryTitleWidth + 5
    private const val manaBoxWidth = width - manaBoxLeft - 5
    private const val mainPanelLeft = 1
    private const val mainPanelWidth = summaryTitleLeft - 5 - mainPanelLeft
    private const val classImageLeft = mainPanelWidth / 2 + 58
    private const val classImageWidth = mainPanelWidth - classImageLeft
    private const val classTop = topBlockHeight - 120
    private const val classFontSize = 35
    private const val numFontSize = 25

    private const val manaFillDark = 0x21a2ff
    private const val manaFillLight = 0x3fccff
    private const val manaDarkLine = 0x3169d5
    private const val manaBoundingBox = 0x16202a
    private const val manaBackgroundGrey = 0x131516

    private val colourLineLight = Color(0x50, 0x4e, 0x36)
    private val colourLineDark = Color(0x39, 0x37, 0x25)
    private val leftCircleFilledResource = this::class.java.classLoader.getResource("images/outer-blue-50.png")
    private val rightCircleHollowResource = this::class.java.classLoader.getResource("images/outer-50.png")
    private val at = AffineTransform.getScaleInstance(scaleX, scaleY)
    private val scaleOp = AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR)
    private val leftCircle = ImageIO.read(leftCircleFilledResource)

    fun from(deck: Deck, username: String): BufferedImage {
        val da = DeckAnalysis(deck)
        val columnLengths: List<Int> = calculateColumnLengths(da.totalUnique, 4)

        val cardsInColumns = listOf(
            da.cardCountSorted.take(columnLengths[0]),
            da.cardCountSorted.drop(columnLengths[0]).take(columnLengths[1]),
            da.cardCountSorted.drop(columnLengths[0] + columnLengths[1]).take(columnLengths[2]),
            da.cardCountSorted.drop(columnLengths[0] + columnLengths[1] + columnLengths[2]).take(columnLengths[3])
        )

        val height = topBlockHeight + columnLengths[0] * (2 * circRadius + heightGap) + bottomMargin

        val bi = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val ig2 = bi.createGraphics()
        ig2.color = Color.BLACK
        ig2.fillRect(0, 0, width, height)

        // background image for class
        val classNameGraphic = da.deckClassName.toLowerCase().replace(" ", "_")
        val classResource = this::class.java.classLoader.getResource("images/class-bg/${classNameGraphic}.png")
        if (classResource != null) {
            val classImage = ImageIO.read(classResource)
            ig2.clipRect(classImageLeft, allBoxTop, classImageWidth, allBoxHeight)
            ig2.drawImage(classImage, classImageLeft, allBoxTop, null)
            ig2.clip = null
        }

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
                val cutdownImage = copySubImage(imageData, 60, 140, imageData.width - 130, 110)
                val scaledImage = scaleImage(cutdownImage)

                // CLOUD on left, CARD on right, dissolve between the two

                // CLOUD
                val cloudResource = this::class.java.classLoader.getResource("images/cloud-grey.png")
                val cloudImage = ImageIO.read(cloudResource)

                // Merge
                val boxHeight = circRadius * 2 - 4
                val boxWidth = x2 - x1
                val mergedImage = GfxFade.combine(
                    image1 = cloudImage,
                    image2 = scaledImage,
                    width = boxWidth,
                    height = boxHeight,
                    mergePoint = 0.59f,
                    mergePercent = 0.13f
                )

                ////////////////////////////////////////////////////////////////////////////////////
                // FILLED BOX WITH CARD COLOURS
                // The alpha channel of the colours determine the opacity of the colour
                val cc = card.attributes
                    .map { ClassAbility.valueOf(it.toUpperCase()).classColour }

                val colourBoxWidth = boxWidth * 14 / 20
                val colourOverlay: BufferedImage = when(cc.size) {
                    1 -> GfxFade.createColourFade(
                        colours = listOf(cc[0].hexColor, cc[0].hexColor),
                        width = colourBoxWidth,
                        height = boxHeight
                    )
                    2 -> GfxFade.createColourFade(
                        colours = listOf(cc[0].hexColor, cc[1].hexColor),
                        width = colourBoxWidth * 9 / 13,
                        height = boxHeight,
                        mergePercent = 0.25f,
                        additionalWidth =  colourBoxWidth * 4 / 13
                    )
                    else -> GfxFade.createColourFade(
                        colours = listOf(cc[0].hexColor, cc[1].hexColor, cc[2].hexColor),
                        width = colourBoxWidth * 9 / 13,
                        height = boxHeight,
                        mergePercent = 0.2f,
                        additionalWidth = colourBoxWidth * 4 / 13
                    )
                }

                // now combine colourImage and mergedImage
                val finalImage = GfxFade.combine(
                    image1 = colourOverlay,
                    image2 = mergedImage,
                    width = boxWidth,
                    height = boxHeight,
                    mergePoint = 0.45f,
                    mergePercent = 0.08f,
                    initialAlpha = 0x3f
                )

                ig2.clipRect(x1, y-circRadius+2, boxWidth, circRadius*2 - 4)
                ig2.drawImage(finalImage, x1, y-circRadius+2, null)
                ig2.clip = null

                ////////////////////////////////////////////////////////////////////////////////////
                // CONNECTING PARALLEL LINES (will be overwritten by circles)
                ////////////////////////////////////////////////////////////////////////////////////
                ig2.color = colourLineDark
                ig2.drawLine(Point(x1, y - circRadius + 2), Point(x2, y - circRadius + 2))
                ig2.drawLine(Point(x1, y + circRadius - 2), Point(x2, y + circRadius - 2))
                ig2.color = colourLineLight
                ig2.drawLine(Point(x1, y - circRadius + 1), Point(x2, y - circRadius + 1))
                ig2.drawLine(Point(x1, y + circRadius - 1), Point(x2, y + circRadius - 1))

                ////////////////////////////////////////////////////////////////////////////////////
                // LEFT CIRCLE
                ig2.drawImage(leftCircle, x1 - circRadius, y - circRadius + 1, null)

                // RIGHT OUTER (OR LINE)
                if (count > 1) {
                    val rightCircle = ImageIO.read(rightCircleHollowResource)
                    ig2.drawImage(rightCircle, x2 - circRadius, y - circRadius + 1, null)
                } else {
                    ig2.drawLine(Point(x2, y - circRadius + 2), Point(x2, y + circRadius - 2))
                }

                // RIGHT INNER
                if (card.unique) {
                    val uniqueResource = this::class.java.classLoader.getResource("images/unique-28.png")
                    val uniqueImage = ImageIO.read(uniqueResource)
                    ig2.drawImage(uniqueImage, x2 - 13, y - 12, null)
                }

                ////////////////////////////////////////////////////////////////////////////////////
                // LEFT NUMBER = cost
                ////////////////////////////////////////////////////////////////////////////////////
                val costMessage = "${card.cost}"
                val (wCost, hCost) = setupToDrawNumber(ig2, costMessage, Color.BLACK)
                ig2.drawString(costMessage, if (costMessage.length == 1) x1 - 7 else x1 - wCost / 2 - 2, y + hCost / 2 - 1)

                ////////////////////////////////////////////////////////////////////////////////////
                // RIGHT NUMBER = COUNT (if > 1)
                ////////////////////////////////////////////////////////////////////////////////////
                if (count > 1) {
                    val countMessage = "$count"
                    val (wCount, hCount) = setupToDrawNumber(ig2, countMessage, Color.WHITE)
                    ig2.drawString(countMessage, x2 + wCount / 2 - 14, y + hCount / 2 - 1)
                }

                ////////////////////////////////////////////////////////////////////////////////////
                // NAME
                ////////////////////////////////////////////////////////////////////////////////////
                val nameMessage = card.name.substring(0, min(card.name.length, 26))
                val (_, hName) = setupToDrawNumber(ig2, nameMessage, Color.WHITE, Font.PLAIN, 20)
                ig2.drawString(nameMessage, x1 + 22 + circRadius/2, y + hName / 2 - 2)
            }
        }

        val usernameHeight = displayUser(ig2, username)

        // Summary details
        displayDeckDetailValue(ig2, "Creatures:", "${da.creatureCount}", 0, 0)
        displayDeckDetailValue(ig2, "Soulgems:", "${da.soulGemCost}", 0, 1)

        displayDeckDetailValue(ig2, "Actions:", "${da.actionsCount}", 0, 2)
        displayDeckDetailValue(ig2, "Items:", "${da.itemsCount}", 0, 3)

        displayDeckDetailValue(ig2, "Supports:", "${da.supportsCount}", 0, 4)
        displayDeckDetailValue(ig2, "Prophecies:", "${da.prophecyCount}", 0, 5)

        displayDeckDetailValue(ig2, "Commons:", "${da.commonCount}", 0, 6)
        displayDeckDetailValue(ig2, "Rares:", "${da.rareCount}", 0, 7)

        displayDeckDetailValue(ig2, "Epics:", "${da.epicCount}", 0, 8)
        displayDeckDetailValue(ig2, "Legendaries:", "${da.legendaryCount}", 0, 9)

        ///////////////////////////////////////////////////////////////////////////////
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
            val x = 10 + attIndex++ * 100
            val y = classTop
            ig2.drawImage(iconImage, x, y, null)

            ig2.paint = Color(0xd2, 0xcb, 0xfe)
            ig2.drawString("$count", x + 55, y + 35)
        }

        // Deck class name
        if (da.totalCards > 0) {
            ig2.font = Font(fontName, Font.PLAIN, classFontSize * 8 / 10)
            ig2.paint = Color(0xd2, 0xcb, 0xfe)
            val ofCount = if (da.deckClass.classColours.size < 3) 50 else 75
            ig2.drawString("${da.deckClassName} [${da.totalCards} / $ofCount]", leftMargin, classTop + circRadius * 2 + classFontSize + 10)
        }

        ///////////////////////////////////////////////////////////////////////////////
        // MANA CURVE

        ig2.color = Color(manaBackgroundGrey, false)
        ig2.fillRect(manaBoxLeft + 1, allBoxTop + 1, manaBoxWidth - 1, allBoxHeight - 1)

        val largestManaCount = da.manaCurve.values.max() ?: 0
        // draw the 8 circles
        (0..7).forEach { index ->
            val x = manaBoxLeft + index * (manaBoxWidth / 8) + manaBoxWidth / 16 - circRadius + 2
            val y = allBoxHeight - circRadius * 2
            ig2.drawImage(leftCircle, x, y, null)

            val manaNum = when(index) {
                0, 1, 2, 3, 4, 5, 6 -> "$index"
                else -> "7+"
            }

            val (wCost, hCost) = setupToDrawNumber(ig2, manaNum, Color.BLACK)
            val numX = if (manaNum.length == 1) x + circRadius - 7 else x - wCost / 2 + circRadius
            val numY = y + hCost / 2 - 1 + circRadius
            ig2.drawString(manaNum, numX, numY)

            // draw outline box above the number
            ig2.color = Color(manaBoundingBox, false)
            ig2.drawBox(Point(x, allBoxTop + 5), Point(x + circRadius * 2, allBoxHeight - circRadius * 2 - 10))
            ig2.color = Color.BLACK
            ig2.fillRect(x+1, allBoxTop + 6, circRadius * 2 - 2, allBoxHeight - allBoxTop - circRadius * 2 - 15 - 2)

            val boxHeight = (allBoxHeight - allBoxTop) - circRadius * 2 - 15 - 2
            val boxWidth = circRadius * 2 - 2
            val h1 = da.manaCurve[index] ?: 0
            if (h1 > 0 && largestManaCount > 0) {
                val drawHeight = h1 * boxHeight / largestManaCount - 1
                val boxYFrom = allBoxTop + boxHeight - drawHeight + 5
                val boxYTo = allBoxTop + boxHeight + 6

                val blueBlock = GfxFade.createColourFade(
                    colours = listOf(Color(manaFillDark, false), Color(manaFillLight, false)),
                    width = circRadius * 2 - 1,
                    height = boxYTo - boxYFrom + 1,
                    mergePercent = 0.49f,
                    px1 = (circRadius * 2 - 1) / 2,
                    py1 = 0,
                    px2 = (circRadius * 2 - 1) / 2,
                    py2 = boxYTo - boxYFrom
                )
                ig2.drawImage(blueBlock, x + 1, boxYFrom, null)

                // now a small inner border
                ig2.color = Color(manaDarkLine, false)
                ig2.drawBox(Point(x + 2, boxYFrom + 1), Point(x + circRadius * 2 - 2, boxYTo - 1))

            }
            // the actual count
            val countOfCurrentMana = "$h1"
            val (wManaCount, hManaCount) = setupToDrawNumber(ig2, countOfCurrentMana, Color.WHITE)
            ig2.drawString(countOfCurrentMana, x - wManaCount/2 + 25, allBoxTop + 32)
        }

        // PANEL BOXES
        ig2.color = Color.DARK_GRAY
        ig2.drawRect(mainPanelLeft, allBoxTop, mainPanelWidth, allBoxHeight)
        ig2.drawRect(summaryTitleLeft, allBoxTop, summaryTitleWidth, allBoxHeight)
        ig2.drawRect(manaBoxLeft, allBoxTop, manaBoxWidth, allBoxHeight)

        return bi
    }

    private fun setupToDrawNumber(
        ig2: Graphics2D,
        s: String,
        colour: Color,
        style: Int = Font.BOLD,
        fontSize: Int = numFontSize
    ): Pair<Int, Int> {
        ig2.font = Font(fontName, style, fontSize)
        val cost = ig2.fontMetrics
        val wCost = cost.stringWidth(s)
        val hCost = cost.ascent
        ig2.paint = colour
        ig2.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        )
        return Pair(wCost, hCost)
    }

    private fun copySubImage(image: BufferedImage, x: Int, y: Int, w: Int, h: Int): BufferedImage {
        val new = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
        val g = new.createGraphics()
        g.drawImage(image.getSubimage(x, y, w, h), 0, 0, null)
        g.dispose()
        return new
    }

    private fun scaleImage(image: BufferedImage): BufferedImage {
        val after = BufferedImage((image.width * scaleX).toInt(), (image.height * scaleY).toInt(), BufferedImage.TYPE_INT_ARGB)
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

    private fun displayUser(g: Graphics2D, name: String): Int {
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
        g.drawString(name, 10, 5 + hName)
        return hName
    }

    private fun displayDeckDetailValue(g: Graphics2D, title: String, text: String, x: Int, y: Int) {
        val fontSize = 20

        // Title
        g.font = Font(fontName, Font.PLAIN, fontSize)
        val fmTitle = g.fontMetrics
        val wTitle = fmTitle.stringWidth(title)
        val hTitle = fmTitle.ascent
        g.paint = Color(0x86, 0x86, 0x86)
        val x2 = x * width / numCols + (width / numCols) * 7 / 10 - wTitle - 10 + summaryTitleLeft
        val y2 = hTitle + y * (fontSize * 3 / 2) + allBoxTop + 5
        g.drawString(title, x2, y2)

        // Text
        g.font = Font(fontName, Font.BOLD, fontSize)
        val fmText = g.fontMetrics
        g.paint = Color(0xd2, 0xcb, 0xfe)
        val x3 = x2 + wTitle + 10
        g.drawString(text, x3, y2)

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
