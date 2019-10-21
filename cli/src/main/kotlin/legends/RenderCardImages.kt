package legends

import legends.gfx.GfxFade
import legends.gfx.GraphicsUtilities
import tesl.model.Card
import tesl.model.CardCache
import java.awt.AlphaComposite
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.net.URL
import javax.imageio.ImageIO

class RenderCardImages {
    companion object {
        const val scaleX: Double = 0.5
        const val scaleY: Double = 0.5

        @JvmStatic
        fun main(args: Array<String>) {
            RenderCardImages().render()
        }
    }

    private val at = AffineTransform.getScaleInstance(scaleX, scaleY)
    private val scaleOp = AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR)

    fun render() {
        CardCache.all().forEach { card ->
            // if (card.name != "Emeric, Covenant King") return@forEach
            println("creating image for ${card.name}")
            val imageData = getImageData(card)
            val cutdownImage = copySubImage(imageData, 60, 140, imageData.width - 130, 110)
            val scaledImage = scaleImage(cutdownImage)

            val boxHeight = DeckImage.circRadius * 2 - 4
            val boxWidth = DeckImage.nameWidth + DeckImage.circRadius * 2
            val colourFade = createColourFade(card, boxWidth * 31 / 40, boxHeight)

            val colourAndCard = GfxFade.combine(
                image1 = colourFade,
                image2 = scaledImage,
                width = boxWidth,
                height = boxHeight,
                mergePoint = 0.65f,
                mergePercent = 0.1f,
                initialAlpha = 0xff,
                xa = boxWidth * 5 / 20,
                ya = 0,
                xb = boxWidth * 15 / 20,
                yb = boxHeight
            )

            val cloudResource = this::class.java.classLoader.getResource("images/cloud-grey.png")
            val cloudImage = ImageIO.read(cloudResource)

            //////////////////////////////////////////////////////////////////////////////////////
            // COMBINE VIA BLENDER
            val withNoise = BufferedImage(boxWidth, boxHeight, BufferedImage.TYPE_INT_ARGB)
            val g = withNoise.createGraphics()

            g.composite = AlphaComposite.Src
            g.drawImage(GraphicsUtilities.toCompatibleImage(cloudImage), 0, 0, null)
            g.composite = AlphaComposite.DstAtop
            g.drawImage(GraphicsUtilities.toCompatibleImage(colourAndCard), 0, 0, null)
            g.dispose()
            //////////////////////////////////////////////////////////////////////////////////////

            // Write it to a dir
            val fileName = DeckImage.fileNameFromCardName(card.name)
            ImageIO.write(
                withNoise,
                "PNG",
                File("/home/markf/dev/personal/gaming/deck-check/cli/src/main/resources/images/rendered/${fileName}.png")
            )
        }
    }

    private fun createColourFade(card: Card, w: Int, h: Int): BufferedImage {
        val cc = card.attributes
            .map { DeckAnalysis.ClassAbility.valueOf(it.toUpperCase()).classColour }

        return when (cc.size) {
            1 -> GfxFade.createColourFade(
                colours = listOf(cc[0].hexColor, cc[0].hexColor),
                width = w,
                height = h
            )
            2 -> GfxFade.createColourFade(
                colours = listOf(cc[0].hexColor, cc[1].hexColor),
                width = w * 10 / 13,
                height = h,
                mergePercent = 0.15f,
                additionalWidth = w * 3 / 13
            )
            else -> GfxFade.createColourFade(
                colours = listOf(cc[0].hexColor, cc[1].hexColor, cc[2].hexColor),
                width = w * 10 / 13,
                height = h,
                mergePercent = 0.15f,
                additionalWidth = w * 3 / 13
            )
        }
    }

    private fun getImageData(card: Card): BufferedImage {
        val sanitisedName = card.name.replace("/", "_")
        val localResource = this::class.java.classLoader.getResource("images/cards/${sanitisedName}.png")
        return when {
            localResource != null -> {
                ImageIO.read(localResource)
            }
            card.imageUrl != "" -> {
                println("Downloading ${card.imageUrl}")
                ImageIO.read(URL(card.imageUrl))
            }
            else -> throw IOException("No URL available for card $card")
        }
    }

    private fun copySubImage(image: BufferedImage, x: Int, y: Int, w: Int, h: Int): BufferedImage {
        val new = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
        val g = new.createGraphics()
        g.drawImage(image.getSubimage(x, y, w, h), 0, 0, null)
        g.dispose()
        return new
    }

    private fun scaleImage(image: BufferedImage): BufferedImage {
        val after =
            BufferedImage((image.width * scaleX).toInt(), (image.height * scaleY).toInt(), BufferedImage.TYPE_INT_ARGB)
        return scaleOp.filter(image, after)
    }

}