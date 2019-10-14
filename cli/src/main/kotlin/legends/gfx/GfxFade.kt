package legends.gfx

import java.awt.*
import java.awt.Point
import java.awt.image.BufferedImage
import kotlin.math.max

object GfxFade {
    fun combine(
        image1: BufferedImage,
        image2: BufferedImage,
        width: Int,
        height: Int,
        mergePoint: Float = 0.5f,
        mergePercent: Float = 0.2f,
        initialAlpha: Int = 0xff,
        xa: Int = 0,
        ya: Int = height / 2,
        xb: Int = width,
        yb: Int = height / 2
    ): BufferedImage {
        // IMAGE 1
        val bi1 = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g1 = bi1.createGraphics()
        g1.drawImage(image1, 0, 0, null)
        g1.dispose()

        val colors2 = listOf(
            Color(0, 0, 0, initialAlpha),
            Color(0, 0, 0, initialAlpha),
            Color(0, 0, 0, 0x00),
            Color(0, 0, 0, 0x00)
        ).toTypedArray()

        val gradient2 = LinearGradientPaint(
            Point(xa, ya),
            Point(xb, yb),
            floatArrayOf(0.0f, mergePoint - mergePercent/2.0f, mergePoint + mergePercent/2.0f, 1.0f),
            colors2
        )

        // IMAGE 2 - WITH GRADIENT
        val bi2 = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2 = bi2.createGraphics()
        g2.drawImage(image2, width - image2.width, 0, null)
        g2.paint = gradient2
        // The Composite goes here!!
        g2.composite = AlphaComposite.DstOut
        g2.fillRect(0, 0, width, height)
        g2.dispose()

        // NOW MERGE THE 2
        return combine(bi1, bi2)
    }

    fun createColourFade(c1: Color, c2: Color, width: Int, height: Int, mergePercent: Float = 0.2f, additionalWidth: Int = 0): BufferedImage {
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

        return addExtraWidth(width = width, height = height, paint = gradientPaint, additionalWidth = additionalWidth)
    }

    private fun combine(image1: BufferedImage, image2: BufferedImage): BufferedImage {
        val width = max(image1.width, image2.width)
        val height = max(image1.height, image2.height)

        val newImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = newImage.createGraphics()
        g.drawImage(image1, 0, 0, null)
        g.drawImage(image2, 0, 0, null)
        g.dispose()

        return newImage
    }

    private fun paint(width: Int, height: Int, paint: Paint? = null, composite: Composite? = null, x: Int = 0, y: Int = 0): BufferedImage {
        val newImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = newImage.createGraphics()
        if (paint != null) g.paint = paint
        if (composite != null) g.composite = composite
        g.fillRect(x, y, width, height)
        g.dispose()
        return newImage
    }

    fun createColourFade(c1: Color, c2: Color, c3: Color, width: Int, height: Int, mergePercent: Float = 0.1f, additionalWidth: Int = 0): BufferedImage {
        val colors = listOf(c1, c1, c2, c2, c3, c3).toTypedArray()

        val gradientPaint = LinearGradientPaint(
            Point(width*3/24, 0),
            Point(width*21/24, height),
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

        return addExtraWidth(width = width, height = height, paint = gradientPaint, additionalWidth = additionalWidth)
    }

    private fun addExtraWidth(width: Int, height: Int, paint: Paint, additionalWidth: Int): BufferedImage {
        val fullWidth = width + additionalWidth

        val bi1 = paint(width = fullWidth, height = height, paint = paint)

        // Create a dissolve for the colour, so it's 100% at the width point, then fades to 0 over additionalWidth
        val colors2 = listOf(
            Color(0, 0, 0, 0xff),
            Color(0, 0, 0, 0xff),
            Color(0, 0, 0, 0x00)
        ).toTypedArray()

        val f = width.toFloat()/fullWidth.toFloat() - 0.00001f
        val gradient = LinearGradientPaint(
            Point(0, height / 2),
            Point(fullWidth, height / 2),
            floatArrayOf(0.0f, f, 1.0f),
            colors2
        )

        val bi2 = paint(width = fullWidth, height = height, composite = AlphaComposite.DstOut, paint = gradient)

        // Merge
        return combine(bi1, bi2)
    }

}
