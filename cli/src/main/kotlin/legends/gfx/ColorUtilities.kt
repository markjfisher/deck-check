package legends.gfx

import java.awt.Color


internal object ColorUtilities {

    /**
     *
     * Returns the HSL (Hue/Saturation/Luminance) equivalent of a given
     * RGB color. All three HSL components are between 0.0 and 1.0.
     *
     * @param color the RGB color to convert
     * @return a new array of 3 floats corresponding to the HSL components
     */
    fun RGBtoHSL(color: Color): FloatArray {
        return RGBtoHSL(color.red, color.green, color.blue, null)
    }

    /**
     *
     * Returns the HSL (Hue/Saturation/Luminance) equivalent of a given
     * RGB color. All three HSL components are between 0.0 and 1.0.
     *
     * @param color the RGB color to convert
     * @param hsl a pre-allocated array of floats; can be null
     * @return `hsl` if non-null, a new array of 3 floats otherwise
     * @throws IllegalArgumentException if `hsl` has a length lower
     * than 3
     */
    fun RGBtoHSL(color: Color, hsl: FloatArray): FloatArray {
        return RGBtoHSL(color.red, color.green, color.blue, hsl)
    }

    /**
     *
     * Returns the HSL (Hue/Saturation/Luminance) equivalent of a given
     * RGB color. All three HSL components are floats between 0.0 and 1.0.
     *
     * @param r the red component, between 0 and 255
     * @param g the green component, between 0 and 255
     * @param b the blue component, between 0 and 255
     * @param hsl a pre-allocated array of floats; can be null
     * @return `hsl` if non-null, a new array of 3 floats otherwise
     * @throws IllegalArgumentException if `hsl` has a length lower
     * than 3
     */
    @JvmOverloads
    fun RGBtoHSL(r: Int, g: Int, b: Int, hsl: FloatArray? = null): FloatArray {
        var r = r
        var g = g
        var b = b
        var hsl = hsl
        if (hsl == null) {
            hsl = FloatArray(3)
        } else require(hsl.size >= 3) { "hsl array must have a length of" + " at least 3" }

        if (r < 0)
            r = 0
        else if (r > 255) r = 255
        if (g < 0)
            g = 0
        else if (g > 255) g = 255
        if (b < 0)
            b = 0
        else if (b > 255) b = 255

        val var_R = r / 255f
        val var_G = g / 255f
        val var_B = b / 255f

        var var_Min: Float
        var var_Max: Float
        val del_Max: Float

        if (var_R > var_G) {
            var_Min = var_G
            var_Max = var_R
        } else {
            var_Min = var_R
            var_Max = var_G
        }
        if (var_B > var_Max) {
            var_Max = var_B
        }
        if (var_B < var_Min) {
            var_Min = var_B
        }

        del_Max = var_Max - var_Min

        var H: Float
        val S: Float
        val L: Float
        L = (var_Max + var_Min) / 2f

        if (del_Max - 0.01f <= 0.0f) {
            H = 0f
            S = 0f
        } else {
            if (L < 0.5f) {
                S = del_Max / (var_Max + var_Min)
            } else {
                S = del_Max / (2f - var_Max - var_Min)
            }

            val del_R = ((var_Max - var_R) / 6f + del_Max / 2f) / del_Max
            val del_G = ((var_Max - var_G) / 6f + del_Max / 2f) / del_Max
            val del_B = ((var_Max - var_B) / 6f + del_Max / 2f) / del_Max

            if (var_R == var_Max) {
                H = del_B - del_G
            } else if (var_G == var_Max) {
                H = 1 / 3f + del_R - del_B
            } else {
                H = 2 / 3f + del_G - del_R
            }
            if (H < 0) {
                H += 1f
            }
            if (H > 1) {
                H -= 1f
            }
        }

        hsl[0] = H
        hsl[1] = S
        hsl[2] = L

        return hsl
    }

    /**
     *
     * Returns the RGB equivalent of a given HSL (Hue/Saturation/Luminance)
     * color.
     *
     * @param h the hue component, between 0.0 and 1.0
     * @param s the saturation component, between 0.0 and 1.0
     * @param l the luminance component, between 0.0 and 1.0
     * @return a new `Color` object equivalent to the HSL components
     */
    fun HSLtoRGB(h: Float, s: Float, l: Float): Color {
        val rgb = HSLtoRGB(h, s, l, null)
        return Color(rgb[0], rgb[1], rgb[2])
    }

    /**
     *
     * Returns the RGB equivalent of a given HSL (Hue/Saturation/Luminance)
     * color. All three RGB components are integers between 0 and 255.
     *
     * @param h the hue component, between 0.0 and 1.0
     * @param s the saturation component, between 0.0 and 1.0
     * @param l the luminance component, between 0.0 and 1.0
     * @param rgb a pre-allocated array of ints; can be null
     * @return `rgb` if non-null, a new array of 3 ints otherwise
     * @throws IllegalArgumentException if `rgb` has a length lower
     * than 3
     */
    fun HSLtoRGB(h: Float, s: Float, l: Float, rgb: IntArray?): IntArray {
        var h = h
        var s = s
        var l = l
        var rgb = rgb
        if (rgb == null) {
            rgb = IntArray(3)
        } else require(rgb.size >= 3) { "rgb array must have a length of" + " at least 3" }

        if (h < 0)
            h = 0.0f
        else if (h > 1.0f) h = 1.0f
        if (s < 0)
            s = 0.0f
        else if (s > 1.0f) s = 1.0f
        if (l < 0)
            l = 0.0f
        else if (l > 1.0f) l = 1.0f

        val R: Int
        val G: Int
        val B: Int

        if (s - 0.01f <= 0.0f) {
            R = (l * 255.0f).toInt()
            G = (l * 255.0f).toInt()
            B = (l * 255.0f).toInt()
        } else {
            val var_1: Float
            val var_2: Float
            if (l < 0.5f) {
                var_2 = l * (1 + s)
            } else {
                var_2 = l + s - s * l
            }
            var_1 = 2 * l - var_2

            R = (255.0f * hue2RGB(var_1, var_2, h + 1.0f / 3.0f)).toInt()
            G = (255.0f * hue2RGB(var_1, var_2, h)).toInt()
            B = (255.0f * hue2RGB(var_1, var_2, h - 1.0f / 3.0f)).toInt()
        }

        rgb[0] = R
        rgb[1] = G
        rgb[2] = B

        return rgb
    }

    private fun hue2RGB(v1: Float, v2: Float, vH: Float): Float {
        var vH = vH
        if (vH < 0.0f) {
            vH += 1.0f
        }
        if (vH > 1.0f) {
            vH -= 1.0f
        }
        if (6.0f * vH < 1.0f) {
            return v1 + (v2 - v1) * 6.0f * vH
        }
        if (2.0f * vH < 1.0f) {
            return v2
        }
        return if (3.0f * vH < 2.0f) {
            v1 + (v2 - v1) * (2.0f / 3.0f - vH) * 6.0f
        } else v1
    }
}
/**
 *
 * Returns the HSL (Hue/Saturation/Luminance) equivalent of a given
 * RGB color. All three HSL components are between 0.0 and 1.0.
 *
 * @param r the red component, between 0 and 255
 * @param g the green component, between 0 and 255
 * @param b the blue component, between 0 and 255
 * @return a new array of 3 floats corresponding to the HSL components
 */