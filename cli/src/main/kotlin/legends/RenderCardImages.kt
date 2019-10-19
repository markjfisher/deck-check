package legends

import legends.gfx.GfxFade
import tesl.model.CardCache
import java.io.File
import javax.imageio.ImageIO

class RenderCardImages {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            RenderCardImages().render()
        }
    }

    fun render() {
        CardCache.all().forEach { card ->
            println("creating image for ${card.name}")
            val imageData = DeckImage.getImageData(card)
            val cutdownImage = DeckImage.copySubImage(imageData, 60, 140, imageData.width - 130, 110)
            val scaledImage = DeckImage.scaleImage(cutdownImage)

            val cloudResource = this::class.java.classLoader.getResource("images/cloud-grey.png")
            val cloudImage = ImageIO.read(cloudResource)

            val boxHeight = DeckImage.circRadius * 2 - 4
            val boxWidth = DeckImage.nameWidth + DeckImage.circRadius *2
            val mergedImage = GfxFade.combine(
                image1 = cloudImage,
                image2 = scaledImage,
                width = boxWidth,
                height = boxHeight,
                mergePoint = 0.59f,
                mergePercent = 0.13f
            )

            val colourFade = DeckImage.createColourFade(card, boxWidth * 14 / 20, boxHeight)

            val finalImage = GfxFade.combine(
                image1 = colourFade,
                image2 = mergedImage,
                width = boxWidth,
                height = boxHeight,
                mergePoint = 0.45f,
                mergePercent = 0.08f,
                initialAlpha = 0x7f
            )

            // Write it to a dir
            val fileName = DeckImage.fileNameFromCardName(card.name)
            ImageIO.write(finalImage, "PNG", File("/home/markf/dev/personal/gaming/deck-check/cli/src/main/resources/images/rendered/${fileName}.png"))
        }
    }

}