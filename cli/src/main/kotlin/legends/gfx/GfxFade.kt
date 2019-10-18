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
            floatArrayOf(0.0f, mergePoint - mergePercent / 2.0f, mergePoint + mergePercent / 2.0f, 1.0f),
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

    private fun paint(
        width: Int,
        height: Int,
        paint: Paint? = null,
        composite: Composite? = null,
        x: Int = 0,
        y: Int = 0
    ): BufferedImage {
        val newImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = newImage.createGraphics()
        if (paint != null) g.paint = paint
        if (composite != null) g.composite = composite
        g.fillRect(x, y, width, height)
        g.dispose()
        return newImage
    }

    fun createColourFade(
        colours: List<Color>,
        width: Int,
        height: Int,
        mergePercent: Float = 0.1f,
        additionalWidth: Int = 0,
        px1: Int = width * 3 / 24,
        py1: Int = 0,
        px2: Int = width * 21 / 24,
        py2: Int = height
    ): BufferedImage {
        val createTransitionFloats = createTransitionFloats(colours.size, mergePercent)
        val createColourBoundaryArray = createColourBoundaryArray(colours)
        val gradientPaint = LinearGradientPaint(
            Point(px1, py1),
            Point(px2, py2),
            createTransitionFloats,
            createColourBoundaryArray
        )
        return addExtraWidth(width = width, height = height, paint = gradientPaint, additionalWidth = additionalWidth)
    }

    private fun createColourBoundaryArray(colours: List<Color>) = colours
        .fold(mutableListOf<Color>()) { list, colour ->
            list.add(colour)
            list.add(colour)
            list
        }.toTypedArray()

    private fun createTransitionFloats(n: Int, mergePercent: Float): FloatArray {
        // we have (n-1)*2 middle points to add, and top and tail with 0/1
        val floats = mutableListOf<Float>()
        floats.add(0.0f)
        (0 until n-1).forEach { i ->
            val f = (i + 1).toFloat() / n.toFloat()
            floats.add(f - mergePercent / 2.0f)
            floats.add(f + mergePercent / 4.0f)
        }
        floats.add(1.0f)
        return floats.toFloatArray()
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

        val f = width.toFloat() / fullWidth.toFloat() - 0.00001f
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
