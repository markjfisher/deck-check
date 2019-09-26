package legends.gfx

import java.awt.AlphaComposite
import java.awt.Color
import java.awt.LinearGradientPaint
import java.awt.Point
import java.awt.image.BufferedImage

object GfxFade {
    fun mergeImages(image1: BufferedImage, image2: BufferedImage, width: Int, height: Int, mergePoint: Float = 0.5f, mergePercent: Float = 0.2f): BufferedImage {
        // IMAGE 1
        val bi1 = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g1 = bi1.createGraphics()
        g1.drawImage(image1, 0, 0, null)
        g1.dispose()

        // IMAGE 2 - WITH GRADIENT
        val bi2 = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2 = bi2.createGraphics()
        g2.drawImage(image2, width - image2.width, 0, null)

        val colors2 = listOf(
            Color(0, 0, 0, 0xff),
            Color(0, 0, 0, 0xff),
            Color(0, 0, 0, 0x00),
            Color(0, 0, 0, 0x00)
        ).toTypedArray()

        val gradient2 = LinearGradientPaint(
            Point(0, height/2),
            Point(width, height/2),
            floatArrayOf(0.0f, mergePoint - mergePercent/2.0f, mergePoint + mergePercent/2.0f, 1.0f),
            colors2
        )

        g2.paint = gradient2
        // The Composite goes here!!
        g2.composite = AlphaComposite.DstOut
        g2.fillRect(0, 0, width, height)
        g2.dispose()

        // NOW MERGE THE 2
        val bi3 = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g3 = bi3.createGraphics()
        g3.drawImage(bi1, 0, 0, null)
        g3.drawImage(bi2, 0, 0, null)
        g3.dispose()

        return bi3
    }

    // Simpler example just merging two colours with default 20% overlap
    fun createColourFade(c1: Color, c2: Color, width: Int, height: Int, mergePercent: Float = 0.2f): BufferedImage {
        val colors = listOf(c1, c1, c2, c2).toTypedArray()

        val gradientPaint = LinearGradientPaint(
            Point(width/3, 0),
            Point(width*2/3, height),
            floatArrayOf(
                0.0f,
                0.5f - mergePercent/2.0f,
                0.5f + mergePercent/2.0f,
                1.0f
            ),
            colors
        )

        val bi = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = bi.createGraphics()
        g.paint = gradientPaint
        g.fillRect(0, 0, width, height)
        g.dispose()
        return bi
    }

    fun createColourFade(c1: Color, c2: Color, c3: Color, width: Int, height: Int, mergePercent: Float = 0.1f): BufferedImage {
        val colors = listOf(c1, c1, c2, c2, c3, c3).toTypedArray()

        val gradientPaint = LinearGradientPaint(
            Point(width/24, 0),
            Point(width*23/24, height),
            floatArrayOf(
                0.0f,
                0.333f - mergePercent/2.0f,
                0.333f + mergePercent/4.0f,
                0.666f - mergePercent/4.0f,
                0.666f + mergePercent/2.0f,
                1.0f
            ),
            colors
        )

        val bi = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = bi.createGraphics()
        g.paint = gradientPaint
        g.fillRect(0, 0, width, height)
        g.dispose()
        return bi
    }

}
