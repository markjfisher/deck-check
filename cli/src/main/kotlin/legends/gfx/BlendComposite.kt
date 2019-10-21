package legends.gfx

import java.awt.Composite
import java.awt.CompositeContext
import java.awt.RenderingHints
import java.awt.image.*
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.max


internal class BlendComposite private constructor(
    /**
     *
     * Returns the blending mode of this composite.
     *
     * @return the blending mode used by this object
     */
    val mode: BlendingMode,
    /**
     *
     * Returns the opacity of this composite. If no opacity has been defined,
     * 1.0 is returned.
     *
     * @return the alpha value, or opacity, of this object
     */
    val alpha: Float = 1.0f
) : Composite {
    /**
     *
     * A blending mode defines the compositing rule of a
     * [BlendComposite].
     *
     * @author Romain Guy <romain.guy></romain.guy>@mac.com>
     */
    enum class BlendingMode {
        AVERAGE,
        MULTIPLY,
        SCREEN,
        DARKEN,
        LIGHTEN,
        OVERLAY,
        HARD_LIGHT,
        SOFT_LIGHT,
        DIFFERENCE,
        NEGATION,
        EXCLUSION,
        COLOR_DODGE,
        INVERSE_COLOR_DODGE,
        SOFT_DODGE,
        COLOR_BURN,
        INVERSE_COLOR_BURN,
        SOFT_BURN,
        REFLECT,
        GLOW,
        FREEZE,
        HEAT,
        ADD,
        SUBTRACT,
        STAMP,
        RED,
        GREEN,
        BLUE,
        HUE,
        SATURATION,
        COLOR,
        LUMINOSITY
    }

    init {
        require(!(alpha < 0.0f || alpha > 1.0f)) { "alpha must be comprised between 0.0f and 1.0f" }
    }

    /**
     *
     * Returns a `BlendComposite` object that uses the specified
     * blending mode and this object's alpha value. If the newly specified
     * blending mode is the same as this object's, this object is returned.
     *
     * @param mode the blending mode defining the compositing rule
     * @return a `BlendComposite` object derived from this object,
     * that uses the specified blending mode
     */
    fun derive(mode: BlendingMode): BlendComposite {
        return if (this.mode == mode) this else BlendComposite(mode, alpha)
    }

    /**
     *
     * Returns a `BlendComposite` object that uses the specified
     * opacity, or alpha, and this object's blending mode. If the newly specified
     * opacity is the same as this object's, this object is returned.
     *
     * @param alpha the constant alpha to be multiplied with the alpha of the
     * source. `alpha` must be a floating point between 0.0 and 1.0.
     * @throws IllegalArgumentException if the opacity is less than 0.0 or
     * greater than 1.0
     * @return a `BlendComposite` object derived from this object,
     * that uses the specified blending mode
     */
    fun derive(alpha: Float): BlendComposite {
        return if (this.alpha == alpha) this else BlendComposite(mode, alpha)
    }

    /**
     * {@inheritDoc}
     */
    override fun hashCode(): Int {
        return java.lang.Float.floatToIntBits(alpha) * 31 + mode.ordinal
    }

    /**
     * {@inheritDoc}
     */
    override fun equals(obj: Any?): Boolean {
        if (obj !is BlendComposite) {
            return false
        }

        val bc = obj as BlendComposite?
        return mode == bc!!.mode && alpha == bc.alpha
    }

    /**
     * {@inheritDoc}
     */
    override fun createContext(
        srcColorModel: ColorModel,
        dstColorModel: ColorModel,
        hints: RenderingHints
    ): CompositeContext {
        if (!checkComponentsOrder(srcColorModel) || !checkComponentsOrder(dstColorModel)) {
            throw RasterFormatException("Incompatible color models")
        }

        return BlendingContext(this)
    }

    private class BlendingContext constructor(private val composite: BlendComposite) : CompositeContext {
        private val blender: Blender = Blender.getBlenderFor(composite)

        override fun dispose() {}

        override fun compose(src: Raster, dstIn: Raster, dstOut: WritableRaster) {
            val width = min(src.width, dstIn.width)
            val height = min(src.height, dstIn.height)

            val alpha = composite.alpha

            val result = IntArray(4)
            val srcPixel = IntArray(4)
            val dstPixel = IntArray(4)
            val srcPixels = IntArray(width)
            val dstPixels = IntArray(width)

            for (y in 0 until height) {
                src.getDataElements(0, y, width, 1, srcPixels)
                dstIn.getDataElements(0, y, width, 1, dstPixels)
                for (x in 0 until width) {
                    // pixels are stored as INT_ARGB
                    // our arrays are [R, G, B, A]
                    var pixel = srcPixels[x]
                    srcPixel[0] = pixel shr 16 and 0xFF
                    srcPixel[1] = pixel shr 8 and 0xFF
                    srcPixel[2] = pixel and 0xFF
                    srcPixel[3] = pixel shr 24 and 0xFF

                    pixel = dstPixels[x]
                    dstPixel[0] = pixel shr 16 and 0xFF
                    dstPixel[1] = pixel shr 8 and 0xFF
                    dstPixel[2] = pixel and 0xFF
                    dstPixel[3] = pixel shr 24 and 0xFF

                    blender.blend(srcPixel, dstPixel, result)

                    // mixes the result with the opacity
                    dstPixels[x] = (dstPixel[3] + (result[3] - dstPixel[3]) * alpha).toInt() and 0xFF shl 24 or (
                            (dstPixel[0] + (result[0] - dstPixel[0]) * alpha).toInt() and 0xFF shl 16) or (
                            (dstPixel[1] + (result[1] - dstPixel[1]) * alpha).toInt() and 0xFF shl 8) or (
                            (dstPixel[2] + (result[2] - dstPixel[2]) * alpha).toInt() and 0xFF)
                }
                dstOut.setDataElements(0, y, width, 1, dstPixels)
            }
        }
    }

    private abstract class Blender {
        abstract fun blend(src: IntArray, dst: IntArray, result: IntArray)

        companion object {

            fun getBlenderFor(composite: BlendComposite): Blender {
                when (composite.mode) {
                    BlendComposite.BlendingMode.ADD -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            result[0] = min(255, src[0] + dst[0])
                            result[1] = min(255, src[1] + dst[1])
                            result[2] = min(255, src[2] + dst[2])
                            result[3] = min(255, src[3] + dst[3])
                        }
                    }
                    BlendComposite.BlendingMode.AVERAGE -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            result[0] = src[0] + dst[0] shr 1
                            result[1] = src[1] + dst[1] shr 1
                            result[2] = src[2] + dst[2] shr 1
                            result[3] = min(255, src[3] + dst[3] - src[3] * dst[3] / 255)
                        }
                    }
                    BlendComposite.BlendingMode.BLUE -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            result[0] = dst[0]
                            result[1] = src[1]
                            result[2] = dst[2]
                            result[3] = min(255, src[3] + dst[3] - src[3] * dst[3] / 255)
                        }
                    }
                    BlendComposite.BlendingMode.COLOR -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            val srcHSL = FloatArray(3)
                            ColorUtilities.RGBtoHSL(src[0], src[1], src[2], srcHSL)
                            val dstHSL = FloatArray(3)
                            ColorUtilities.RGBtoHSL(dst[0], dst[1], dst[2], dstHSL)

                            ColorUtilities.HSLtoRGB(srcHSL[0], srcHSL[1], dstHSL[2], result)
                            result[3] = min(255, src[3] + dst[3] - src[3] * dst[3] / 255)
                        }
                    }
                    BlendComposite.BlendingMode.COLOR_BURN -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            result[0] = if (src[0] == 0)
                                0
                            else
                                max(0, 255 - (255 - dst[0] shl 8) / src[0])
                            result[1] = if (src[1] == 0)
                                0
                            else
                                max(0, 255 - (255 - dst[1] shl 8) / src[1])
                            result[2] = if (src[2] == 0)
                                0
                            else
                                max(0, 255 - (255 - dst[2] shl 8) / src[2])
                            result[3] = min(255, src[3] + dst[3] - src[3] * dst[3] / 255)
                        }
                    }
                    BlendComposite.BlendingMode.COLOR_DODGE -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            result[0] = if (src[0] == 255)
                                255
                            else
                                min((dst[0] shl 8) / (255 - src[0]), 255)
                            result[1] = if (src[1] == 255)
                                255
                            else
                                min((dst[1] shl 8) / (255 - src[1]), 255)
                            result[2] = if (src[2] == 255)
                                255
                            else
                                min((dst[2] shl 8) / (255 - src[2]), 255)
                            result[3] = min(255, src[3] + dst[3] - src[3] * dst[3] / 255)
                        }
                    }
                    BlendComposite.BlendingMode.DARKEN -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            result[0] = min(src[0], dst[0])
                            result[1] = min(src[1], dst[1])
                            result[2] = min(src[2], dst[2])
                            result[3] = min(255, src[3] + dst[3] - src[3] * dst[3] / 255)
                        }
                    }
                    BlendComposite.BlendingMode.DIFFERENCE -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            result[0] = abs(dst[0] - src[0])
                            result[1] = abs(dst[1] - src[1])
                            result[2] = abs(dst[2] - src[2])
                            result[3] = min(255, src[3] + dst[3] - src[3] * dst[3] / 255)
                        }
                    }
                    BlendComposite.BlendingMode.EXCLUSION -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            result[0] = dst[0] + src[0] - (dst[0] * src[0] shr 7)
                            result[1] = dst[1] + src[1] - (dst[1] * src[1] shr 7)
                            result[2] = dst[2] + src[2] - (dst[2] * src[2] shr 7)
                            result[3] = min(255, src[3] + dst[3] - src[3] * dst[3] / 255)
                        }
                    }
                    BlendComposite.BlendingMode.FREEZE -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            result[0] = if (src[0] == 0)
                                0
                            else
                                max(0, 255 - (255 - dst[0]) * (255 - dst[0]) / src[0])
                            result[1] = if (src[1] == 0)
                                0
                            else
                                max(0, 255 - (255 - dst[1]) * (255 - dst[1]) / src[1])
                            result[2] = if (src[2] == 0)
                                0
                            else
                                max(0, 255 - (255 - dst[2]) * (255 - dst[2]) / src[2])
                            result[3] = min(255, src[3] + dst[3] - src[3] * dst[3] / 255)
                        }
                    }
                    BlendComposite.BlendingMode.GLOW -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            result[0] = if (dst[0] == 255)
                                255
                            else
                                min(255, src[0] * src[0] / (255 - dst[0]))
                            result[1] = if (dst[1] == 255)
                                255
                            else
                                min(255, src[1] * src[1] / (255 - dst[1]))
                            result[2] = if (dst[2] == 255)
                                255
                            else
                                min(255, src[2] * src[2] / (255 - dst[2]))
                            result[3] = min(255, src[3] + dst[3] - src[3] * dst[3] / 255)
                        }
                    }
                    BlendComposite.BlendingMode.GREEN -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            result[0] = dst[0]
                            result[1] = dst[1]
                            result[2] = src[2]
                            result[3] = min(255, src[3] + dst[3] - src[3] * dst[3] / 255)
                        }
                    }
                    BlendComposite.BlendingMode.HARD_LIGHT -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            result[0] = if (src[0] < 128)
                                dst[0] * src[0] shr 7
                            else
                                255 - ((255 - src[0]) * (255 - dst[0]) shr 7)
                            result[1] = if (src[1] < 128)
                                dst[1] * src[1] shr 7
                            else
                                255 - ((255 - src[1]) * (255 - dst[1]) shr 7)
                            result[2] = if (src[2] < 128)
                                dst[2] * src[2] shr 7
                            else
                                255 - ((255 - src[2]) * (255 - dst[2]) shr 7)
                            result[3] = min(255, src[3] + dst[3] - src[3] * dst[3] / 255)
                        }
                    }
                    BlendComposite.BlendingMode.HEAT -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            result[0] = if (dst[0] == 0)
                                0
                            else
                                max(0, 255 - (255 - src[0]) * (255 - src[0]) / dst[0])
                            result[1] = if (dst[1] == 0)
                                0
                            else
                                max(0, 255 - (255 - src[1]) * (255 - src[1]) / dst[1])
                            result[2] = if (dst[2] == 0)
                                0
                            else
                                max(0, 255 - (255 - src[2]) * (255 - src[2]) / dst[2])
                            result[3] = min(255, src[3] + dst[3] - src[3] * dst[3] / 255)
                        }
                    }
                    BlendComposite.BlendingMode.HUE -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            val srcHSL = FloatArray(3)
                            ColorUtilities.RGBtoHSL(src[0], src[1], src[2], srcHSL)
                            val dstHSL = FloatArray(3)
                            ColorUtilities.RGBtoHSL(dst[0], dst[1], dst[2], dstHSL)

                            ColorUtilities.HSLtoRGB(srcHSL[0], dstHSL[1], dstHSL[2], result)
                            result[3] = min(255, src[3] + dst[3] - src[3] * dst[3] / 255)
                        }
                    }
                    BlendComposite.BlendingMode.INVERSE_COLOR_BURN -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            result[0] = if (dst[0] == 0)
                                0
                            else
                                max(0, 255 - (255 - src[0] shl 8) / dst[0])
                            result[1] = if (dst[1] == 0)
                                0
                            else
                                max(0, 255 - (255 - src[1] shl 8) / dst[1])
                            result[2] = if (dst[2] == 0)
                                0
                            else
                                max(0, 255 - (255 - src[2] shl 8) / dst[2])
                            result[3] = min(255, src[3] + dst[3] - src[3] * dst[3] / 255)
                        }
                    }
                    BlendComposite.BlendingMode.INVERSE_COLOR_DODGE -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            result[0] = if (dst[0] == 255)
                                255
                            else
                                min((src[0] shl 8) / (255 - dst[0]), 255)
                            result[1] = if (dst[1] == 255)
                                255
                            else
                                min((src[1] shl 8) / (255 - dst[1]), 255)
                            result[2] = if (dst[2] == 255)
                                255
                            else
                                min((src[2] shl 8) / (255 - dst[2]), 255)
                            result[3] = min(255, src[3] + dst[3] - src[3] * dst[3] / 255)
                        }
                    }
                    BlendComposite.BlendingMode.LIGHTEN -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            result[0] = max(src[0], dst[0])
                            result[1] = max(src[1], dst[1])
                            result[2] = max(src[2], dst[2])
                            result[3] = min(255, src[3] + dst[3] - src[3] * dst[3] / 255)
                        }
                    }
                    BlendComposite.BlendingMode.LUMINOSITY -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            val srcHSL = FloatArray(3)
                            ColorUtilities.RGBtoHSL(src[0], src[1], src[2], srcHSL)
                            val dstHSL = FloatArray(3)
                            ColorUtilities.RGBtoHSL(dst[0], dst[1], dst[2], dstHSL)

                            ColorUtilities.HSLtoRGB(dstHSL[0], dstHSL[1], srcHSL[2], result)
                            result[3] = min(255, src[3] + dst[3] - src[3] * dst[3] / 255)
                        }
                    }
                    BlendComposite.BlendingMode.MULTIPLY -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            result[0] = src[0] * dst[0] shr 8
                            result[1] = src[1] * dst[1] shr 8
                            result[2] = src[2] * dst[2] shr 8
                            result[3] = min(255, src[3] + dst[3] - src[3] * dst[3] / 255)
                        }
                    }
                    BlendComposite.BlendingMode.NEGATION -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            result[0] = 255 - abs(255 - dst[0] - src[0])
                            result[1] = 255 - abs(255 - dst[1] - src[1])
                            result[2] = 255 - abs(255 - dst[2] - src[2])
                            result[3] = min(255, src[3] + dst[3] - src[3] * dst[3] / 255)
                        }
                    }
                    BlendComposite.BlendingMode.OVERLAY -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            result[0] = if (dst[0] < 128)
                                dst[0] * src[0] shr 7
                            else
                                255 - ((255 - dst[0]) * (255 - src[0]) shr 7)
                            result[1] = if (dst[1] < 128)
                                dst[1] * src[1] shr 7
                            else
                                255 - ((255 - dst[1]) * (255 - src[1]) shr 7)
                            result[2] = if (dst[2] < 128)
                                dst[2] * src[2] shr 7
                            else
                                255 - ((255 - dst[2]) * (255 - src[2]) shr 7)
                            result[3] = min(255, src[3] + dst[3] - src[3] * dst[3] / 255)
                        }
                    }
                    BlendComposite.BlendingMode.RED -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            result[0] = src[0]
                            result[1] = dst[1]
                            result[2] = dst[2]
                            result[3] = min(255, src[3] + dst[3] - src[3] * dst[3] / 255)
                        }
                    }
                    BlendComposite.BlendingMode.REFLECT -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            result[0] = if (src[0] == 255)
                                255
                            else
                                min(255, dst[0] * dst[0] / (255 - src[0]))
                            result[1] = if (src[1] == 255)
                                255
                            else
                                min(255, dst[1] * dst[1] / (255 - src[1]))
                            result[2] = if (src[2] == 255)
                                255
                            else
                                min(255, dst[2] * dst[2] / (255 - src[2]))
                            result[3] = min(255, src[3] + dst[3] - src[3] * dst[3] / 255)
                        }
                    }
                    BlendComposite.BlendingMode.SATURATION -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            val srcHSL = FloatArray(3)
                            ColorUtilities.RGBtoHSL(src[0], src[1], src[2], srcHSL)
                            val dstHSL = FloatArray(3)
                            ColorUtilities.RGBtoHSL(dst[0], dst[1], dst[2], dstHSL)

                            ColorUtilities.HSLtoRGB(dstHSL[0], srcHSL[1], dstHSL[2], result)
                            result[3] = min(255, src[3] + dst[3] - src[3] * dst[3] / 255)
                        }
                    }
                    BlendComposite.BlendingMode.SCREEN -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            result[0] = 255 - ((255 - src[0]) * (255 - dst[0]) shr 8)
                            result[1] = 255 - ((255 - src[1]) * (255 - dst[1]) shr 8)
                            result[2] = 255 - ((255 - src[2]) * (255 - dst[2]) shr 8)
                            result[3] = min(255, src[3] + dst[3] - src[3] * dst[3] / 255)
                        }
                    }
                    BlendComposite.BlendingMode.SOFT_BURN -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            result[0] = if (dst[0] + src[0] < 256)
                                if (dst[0] == 255)
                                    255
                                else
                                    min(255, (src[0] shl 7) / (255 - dst[0]))
                            else
                                max(0, 255 - (((255 - dst[0]) shl 7) / src[0]))
                            result[1] = if (dst[1] + src[1] < 256)
                                (if (dst[1] == 255)
                                    255
                                else
                                    min(255, (src[1] shl 7) / (255 - dst[1])))
                            else
                                max(0, 255 - (((255 - dst[1]) shl 7) / src[1]))
                            result[2] = if (dst[2] + src[2] < 256)
                                (if (dst[2] == 255)
                                    255
                                else
                                    min(255, (src[2] shl 7) / (255 - dst[2])))
                            else
                                max(0, 255 - (((255 - dst[2]) shl 7) / src[2]))
                            result[3] = min(255, src[3] + dst[3] - (src[3] * dst[3]) / 255)
                        }
                    }
                    BlendComposite.BlendingMode.SOFT_DODGE -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            result[0] = if (dst[0] + src[0] < 256)
                                (if (src[0] == 255)
                                    255
                                else
                                    min(255, (dst[0] shl 7) / (255 - src[0])))
                            else
                                max(0, 255 - (((255 - src[0]) shl 7) / dst[0]))
                            result[1] = if (dst[1] + src[1] < 256)
                                (if (src[1] == 255)
                                    255
                                else
                                    min(255, (dst[1] shl 7) / (255 - src[1])))
                            else
                                max(0, 255 - (((255 - src[1]) shl 7) / dst[1]))
                            result[2] = if (dst[2] + src[2] < 256)
                                (if (src[2] == 255)
                                    255
                                else
                                    min(255, (dst[2] shl 7) / (255 - src[2])))
                            else
                                max(0, 255 - (((255 - src[2]) shl 7) / dst[2]))
                            result[3] = min(255, src[3] + dst[3] - (src[3] * dst[3]) / 255)
                        }
                    }
                    BlendComposite.BlendingMode.SOFT_LIGHT -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            val mRed = src[0] * dst[0] / 255
                            val mGreen = src[1] * dst[1] / 255
                            val mBlue = src[2] * dst[2] / 255
                            result[0] = mRed + src[0] * (255 - ((255 - src[0]) * (255 - dst[0]) / 255) - mRed) / 255
                            result[1] = mGreen + src[1] * (255 - ((255 - src[1]) * (255 - dst[1]) / 255) - mGreen) / 255
                            result[2] = mBlue + src[2] * (255 - ((255 - src[2]) * (255 - dst[2]) / 255) - mBlue) / 255
                            result[3] = min(255, src[3] + dst[3] - (src[3] * dst[3]) / 255)
                        }
                    }
                    BlendComposite.BlendingMode.STAMP -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            result[0] = max(0, min(255, dst[0] + 2 * src[0] - 256))
                            result[1] = max(0, min(255, dst[1] + 2 * src[1] - 256))
                            result[2] = max(0, min(255, dst[2] + 2 * src[2] - 256))
                            result[3] = min(255, src[3] + dst[3] - (src[3] * dst[3]) / 255)
                        }
                    }
                    BlendComposite.BlendingMode.SUBTRACT -> return object : Blender() {
                        override fun blend(src: IntArray, dst: IntArray, result: IntArray) {
                            result[0] = max(0, src[0] + dst[0] - 256)
                            result[1] = max(0, src[1] + dst[1] - 256)
                            result[2] = max(0, src[2] + dst[2] - 256)
                            result[3] = min(255, src[3] + dst[3] - (src[3] * dst[3]) / 255)
                        }
                    }
                }
            }
        }
    }

    companion object {

        val Average = BlendComposite(BlendingMode.AVERAGE)
        val Multiply = BlendComposite(BlendingMode.MULTIPLY)
        val Screen = BlendComposite(BlendingMode.SCREEN)
        val Darken = BlendComposite(BlendingMode.DARKEN)
        val Lighten = BlendComposite(BlendingMode.LIGHTEN)
        val Overlay = BlendComposite(BlendingMode.OVERLAY)
        val HardLight = BlendComposite(BlendingMode.HARD_LIGHT)
        val SoftLight = BlendComposite(BlendingMode.SOFT_LIGHT)
        val Difference = BlendComposite(BlendingMode.DIFFERENCE)
        val Negation = BlendComposite(BlendingMode.NEGATION)
        val Exclusion = BlendComposite(BlendingMode.EXCLUSION)
        val ColorDodge = BlendComposite(BlendingMode.COLOR_DODGE)
        val InverseColorDodge = BlendComposite(BlendingMode.INVERSE_COLOR_DODGE)
        val SoftDodge = BlendComposite(BlendingMode.SOFT_DODGE)
        val ColorBurn = BlendComposite(BlendingMode.COLOR_BURN)
        val InverseColorBurn = BlendComposite(BlendingMode.INVERSE_COLOR_BURN)
        val SoftBurn = BlendComposite(BlendingMode.SOFT_BURN)
        val Reflect = BlendComposite(BlendingMode.REFLECT)
        val Glow = BlendComposite(BlendingMode.GLOW)
        val Freeze = BlendComposite(BlendingMode.FREEZE)
        val Heat = BlendComposite(BlendingMode.HEAT)
        val Add = BlendComposite(BlendingMode.ADD)
        val Subtract = BlendComposite(BlendingMode.SUBTRACT)
        val Stamp = BlendComposite(BlendingMode.STAMP)
        val Red = BlendComposite(BlendingMode.RED)
        val Green = BlendComposite(BlendingMode.GREEN)
        val Blue = BlendComposite(BlendingMode.BLUE)
        val Hue = BlendComposite(BlendingMode.HUE)
        val Saturation = BlendComposite(BlendingMode.SATURATION)
        val Color = BlendComposite(BlendingMode.COLOR)
        val Luminosity = BlendComposite(BlendingMode.LUMINOSITY)

        /**
         *
         * Creates a new composite based on the blending mode passed
         * as a parameter. A default opacity of 1.0 is applied.
         *
         * @param mode the blending mode defining the compositing rule
         * @return a new `BlendComposite` based on the selected blending
         * mode, with an opacity of 1.0
         */
        fun getInstance(mode: BlendingMode): BlendComposite {
            return BlendComposite(mode)
        }

        /**
         *
         * Creates a new composite based on the blending mode and opacity passed
         * as parameters. The opacity must be a value between 0.0 and 1.0.
         *
         * @param mode the blending mode defining the compositing rule
         * @param alpha the constant alpha to be multiplied with the alpha of the
         * source. `alpha` must be a floating point between 0.0 and 1.0.
         * @throws IllegalArgumentException if the opacity is less than 0.0 or
         * greater than 1.0
         * @return a new `BlendComposite` based on the selected blending
         * mode and opacity
         */
        fun getInstance(mode: BlendingMode, alpha: Float): BlendComposite {
            return BlendComposite(mode, alpha)
        }

        private fun checkComponentsOrder(cm: ColorModel): Boolean {
            if ((cm is DirectColorModel && cm.getTransferType() == DataBuffer.TYPE_INT)) {
                val directCM = cm as DirectColorModel

                return (directCM.redMask == 0x00FF0000 &&
                        directCM.greenMask == 0x0000FF00 &&
                        directCM.blueMask == 0x000000FF &&
                        ((directCM.numComponents != 4 || directCM.alphaMask == -0x1000000)))
            }

            return false
        }
    }
}