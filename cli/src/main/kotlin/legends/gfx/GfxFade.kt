package legends.gfx

import java.awt.AlphaComposite
import java.awt.Color
import java.awt.LinearGradientPaint
import java.awt.Point
import java.awt.image.BufferedImage

object GfxFade {
    fun createColourFade(c1: Color, c2: Color, width: Int, height: Int): BufferedImage {
        // IMAGE 1
        val bi1 = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g1 = bi1.createGraphics()
        g1.color = c1
        g1.fillRect(0, 0, width, height)

        val colors1 = listOf(
            Color(0, 0, 0, 0),
            Color(0, 0, 0, 0xff)
        ).toTypedArray()

        val gradient1 = LinearGradientPaint(
            Point(0, height/2),
            Point(width, height/2),
            floatArrayOf(0.0f, 1.0f),
            colors1
        )
        g1.paint = gradient1
        g1.fillRect(0, 0, width, height)
        g1.dispose()

        // IMAGE 2 - WITH GRADIENT
        val bi2 = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2 = bi2.createGraphics()
        g2.color = c2
        g2.fillRect(0, 0, width, height)

        val colors2 = listOf(
            Color(0, 0, 0, 0xff),
            Color(0, 0, 0, 0)
        ).toTypedArray()

        val gradient2 = LinearGradientPaint(
            Point(0, height/2),
            Point(width, height/2),
            floatArrayOf(0.0f, 1.0f),
            colors2
        )

        g2.paint = gradient2
        g2.fillRect(0, 0, width, height)
        g2.dispose()

        // NOW MERGE THE 2
        val bi3 = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g3 = bi3.createGraphics()
        g3.drawImage(bi1, 0, 0, null)
        g3.composite = AlphaComposite.DstIn
        g3.drawImage(bi2, 0, 0, null)
        g3.dispose()

        return bi3
    }

}
