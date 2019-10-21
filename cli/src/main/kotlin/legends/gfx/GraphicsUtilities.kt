package legends.gfx

import java.awt.GraphicsEnvironment
import java.awt.RenderingHints
import java.awt.Transparency
import java.awt.image.BufferedImage
import java.io.IOException
import java.net.URL
import javax.imageio.ImageIO


internal object GraphicsUtilities {
    private val CONFIGURATION =
        GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration

    /**
     *
     * Returns a new `BufferedImage` using the same color model
     * as the image passed as a parameter. The returned image is only compatible
     * with the image passed as a parameter. This does not mean the returned
     * image is compatible with the hardware.
     *
     * @param image the reference image from which the color model of the new
     * image is obtained
     * @return a new `BufferedImage`, compatible with the color model
     * of `image`
     */
    fun createColorModelCompatibleImage(image: BufferedImage): BufferedImage {
        val cm = image.colorModel
        return BufferedImage(
            cm,
            cm.createCompatibleWritableRaster(
                image.width,
                image.height
            ),
            cm.isAlphaPremultiplied, null
        )
    }

    /**
     *
     * Returns a new compatible image of the specified width and height, and
     * the same transparency setting as the image specified as a parameter.
     *
     * @see java.awt.Transparency
     *
     * @see .createCompatibleImage
     * @see .createCompatibleImage
     * @see .createTranslucentCompatibleImage
     * @see .loadCompatibleImage
     * @see .toCompatibleImage
     * @param width the width of the new image
     * @param height the height of the new image
     * @param image the reference image from which the transparency of the new
     * image is obtained
     * @return a new compatible `BufferedImage` with the same
     * transparency as `image` and the specified dimension
     */
    @JvmOverloads
    fun createCompatibleImage(
        image: BufferedImage,
        width: Int = image.width, height: Int = image.height
    ): BufferedImage {
        return CONFIGURATION.createCompatibleImage(
            width, height,
            image.transparency
        )
    }

    /**
     *
     * Returns a new opaque compatible image of the specified width and
     * height.
     *
     * @see .createCompatibleImage
     * @see .createCompatibleImage
     * @see .createTranslucentCompatibleImage
     * @see .loadCompatibleImage
     * @see .toCompatibleImage
     * @param width the width of the new image
     * @param height the height of the new image
     * @return a new opaque compatible `BufferedImage` of the
     * specified width and height
     */
    fun createCompatibleImage(width: Int, height: Int): BufferedImage {
        return CONFIGURATION.createCompatibleImage(width, height)
    }

    /**
     *
     * Returns a new translucent compatible image of the specified width
     * and height.
     *
     * @see .createCompatibleImage
     * @see .createCompatibleImage
     * @see .createCompatibleImage
     * @see .loadCompatibleImage
     * @see .toCompatibleImage
     * @param width the width of the new image
     * @param height the height of the new image
     * @return a new translucent compatible `BufferedImage` of the
     * specified width and height
     */
    fun createTranslucentCompatibleImage(
        width: Int,
        height: Int
    ): BufferedImage {
        return CONFIGURATION.createCompatibleImage(
            width, height,
            Transparency.TRANSLUCENT
        )
    }

    /**
     *
     * Returns a new compatible image from a URL. The image is loaded from the
     * specified location and then turned, if necessary into a compatible
     * image.
     *
     * @see .createCompatibleImage
     * @see .createCompatibleImage
     * @see .createCompatibleImage
     * @see .createTranslucentCompatibleImage
     * @see .toCompatibleImage
     * @param resource the URL of the picture to load as a compatible image
     * @return a new translucent compatible `BufferedImage` of the
     * specified width and height
     * @throws java.io.IOException if the image cannot be read or loaded
     */
    @Throws(IOException::class)
    fun loadCompatibleImage(resource: URL): BufferedImage {
        val image = ImageIO.read(resource)
        return toCompatibleImage(image)
    }

    /**
     *
     * Return a new compatible image that contains a copy of the specified
     * image. This method ensures an image is compatible with the hardware,
     * and therefore optimized for fast blitting operations.
     *
     * @see .createCompatibleImage
     * @see .createCompatibleImage
     * @see .createCompatibleImage
     * @see .createTranslucentCompatibleImage
     * @see .loadCompatibleImage
     * @param image the image to copy into a new compatible image
     * @return a new compatible copy, with the
     * same width and height and transparency and content, of `image`
     */
    fun toCompatibleImage(image: BufferedImage): BufferedImage {
        if (image.colorModel == CONFIGURATION.colorModel) {
            return image
        }

        val compatibleImage = CONFIGURATION.createCompatibleImage(
            image.width, image.height, image.transparency
        )
        val g = compatibleImage.graphics
        g.drawImage(image, 0, 0, null)
        g.dispose()

        return compatibleImage
    }

    /**
     *
     * Returns a thumbnail of a source image. `newSize` defines
     * the length of the longest dimension of the thumbnail. The other
     * dimension is then computed according to the dimensions ratio of the
     * original picture.
     *
     * This method favors speed over quality. When the new size is less than
     * half the longest dimension of the source image,
     * [.createThumbnail] or
     * [.createThumbnail] should be used instead
     * to ensure the quality of the result without sacrificing too much
     * performance.
     *
     * @see .createThumbnailFast
     * @see .createThumbnail
     * @see .createThumbnail
     * @param image the source image
     * @param newSize the length of the largest dimension of the thumbnail
     * @return a new compatible `BufferedImage` containing a
     * thumbnail of `image`
     * @throws IllegalArgumentException if `newSize` is larger than
     * the largest dimension of `image` or &lt;= 0
     */
    fun createThumbnailFast(
        image: BufferedImage,
        newSize: Int
    ): BufferedImage {
        val ratio: Float
        var width = image.width
        var height = image.height

        if (width > height) {
            require(newSize < width) { "newSize must be lower than" + " the image width" }
            require(newSize > 0) { "newSize must" + " be greater than 0" }

            ratio = width.toFloat() / height.toFloat()
            width = newSize
            height = (newSize / ratio).toInt()
        } else {
            require(newSize < height) { "newSize must be lower than" + " the image height" }
            require(newSize > 0) { "newSize must" + " be greater than 0" }

            ratio = height.toFloat() / width.toFloat()
            height = newSize
            width = (newSize / ratio).toInt()
        }

        val temp = createCompatibleImage(image, width, height)
        val g2 = temp.createGraphics()
        g2.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR
        )
        g2.drawImage(image, 0, 0, temp.width, temp.height, null)
        g2.dispose()

        return temp
    }

    /**
     *
     * Returns a thumbnail of a source image.
     *
     * This method favors speed over quality. When the new size is less than
     * half the longest dimension of the source image,
     * [.createThumbnail] or
     * [.createThumbnail] should be used instead
     * to ensure the quality of the result without sacrificing too much
     * performance.
     *
     * @see .createThumbnailFast
     * @see .createThumbnail
     * @see .createThumbnail
     * @param image the source image
     * @param newWidth the width of the thumbnail
     * @param newHeight the height of the thumbnail
     * @return a new compatible `BufferedImage` containing a
     * thumbnail of `image`
     * @throws IllegalArgumentException if `newWidth` is larger than
     * the width of `image` or if code>newHeight is larger
     * than the height of `image` or if one of the dimensions
     * is &lt;= 0
     */
    fun createThumbnailFast(
        image: BufferedImage,
        newWidth: Int, newHeight: Int
    ): BufferedImage {
        require(!(newWidth >= image.width || newHeight >= image.height)) { "newWidth and newHeight cannot" +
            " be greater than the image" +
            " dimensions" }
        require(!(newWidth <= 0 || newHeight <= 0)) { ("newWidth and newHeight must" + " be greater than 0") }

        val temp = createCompatibleImage(image, newWidth, newHeight)
        val g2 = temp.createGraphics()
        g2.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR
        )
        g2.drawImage(image, 0, 0, temp.width, temp.height, null)
        g2.dispose()

        return temp
    }

    /**
     *
     * Returns a thumbnail of a source image. `newSize` defines
     * the length of the longest dimension of the thumbnail. The other
     * dimension is then computed according to the dimensions ratio of the
     * original picture.
     *
     * This method offers a good trade-off between speed and quality.
     * The result looks better than
     * [.createThumbnailFast] when
     * the new size is less than half the longest dimension of the source
     * image, yet the rendering speed is almost similar.
     *
     * @see .createThumbnailFast
     * @see .createThumbnailFast
     * @see .createThumbnail
     * @param image the source image
     * @param newSize the length of the largest dimension of the thumbnail
     * @return a new compatible `BufferedImage` containing a
     * thumbnail of `image`
     * @throws IllegalArgumentException if `newSize` is larger than
     * the largest dimension of `image` or &lt;= 0
     */
    fun createThumbnail(
        image: BufferedImage,
        newSize: Int
    ): BufferedImage {
        var width = image.width
        var height = image.height

        val isWidthGreater = width > height

        if (isWidthGreater) {
            require(newSize < width) { ("newSize must be lower than" + " the image width") }
        } else require(newSize < height) { ("newSize must be lower than" + " the image height") }

        require(newSize > 0) { ("newSize must" + " be greater than 0") }

        val ratioWH = width.toFloat() / height.toFloat()
        val ratioHW = height.toFloat() / width.toFloat()

        var thumb = image

        do {
            if (isWidthGreater) {
                width /= 2
                if (width < newSize) {
                    width = newSize
                }
                height = (width / ratioWH).toInt()
            } else {
                height /= 2
                if (height < newSize) {
                    height = newSize
                }
                width = (height / ratioHW).toInt()
            }


            val temp = createCompatibleImage(image, width, height)
            val g2 = temp.createGraphics()
            g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
            )
            g2.drawImage(thumb, 0, 0, temp.width, temp.height, null)
            g2.dispose()

            thumb = temp
        } while (newSize != (if (isWidthGreater) width else height))

        return thumb
    }

    /**
     *
     * Returns a thumbnail of a source image.
     *
     * This method offers a good trade-off between speed and quality.
     * The result looks better than
     * [.createThumbnailFast] when
     * the new size is less than half the longest dimension of the source
     * image, yet the rendering speed is almost similar.
     *
     * @see .createThumbnailFast
     * @see .createThumbnailFast
     * @see .createThumbnail
     * @param image the source image
     * @param newWidth the width of the thumbnail
     * @param newHeight the height of the thumbnail
     * @return a new compatible `BufferedImage` containing a
     * thumbnail of `image`
     * @throws IllegalArgumentException if `newWidth` is larger than
     * the width of `image` or if code>newHeight is larger
     * than the height of `image or if one the dimensions is not > 0`
     */
    fun createThumbnail(
        image: BufferedImage,
        newWidth: Int, newHeight: Int
    ): BufferedImage {
        var width = image.width
        var height = image.height

        require(!(newWidth >= width || newHeight >= height)) { ("newWidth and newHeight cannot" +
            " be greater than the image" +
            " dimensions") }
        require(!(newWidth <= 0 || newHeight <= 0)) { ("newWidth and newHeight must" + " be greater than 0") }

        var thumb = image

        do {
            if (width > newWidth) {
                width /= 2
                if (width < newWidth) {
                    width = newWidth
                }
            }

            if (height > newHeight) {
                height /= 2
                if (height < newHeight) {
                    height = newHeight
                }
            }

            val temp = createCompatibleImage(image, width, height)
            val g2 = temp.createGraphics()
            g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
            )
            g2.drawImage(thumb, 0, 0, temp.width, temp.height, null)
            g2.dispose()

            thumb = temp
        } while (width != newWidth || height != newHeight)

        return thumb
    }

    /**
     *
     * Returns an array of pixels, stored as integers, from a
     * `BufferedImage`. The pixels are grabbed from a rectangular
     * area defined by a location and two dimensions. Calling this method on
     * an image of type different from `BufferedImage.TYPE_INT_ARGB`
     * and `BufferedImage.TYPE_INT_RGB` will unmanage the image.
     *
     * @param img the source image
     * @param x the x location at which to start grabbing pixels
     * @param y the y location at which to start grabbing pixels
     * @param w the width of the rectangle of pixels to grab
     * @param h the height of the rectangle of pixels to grab
     * @param pixels a pre-allocated array of pixels of size w*h; can be null
     * @return `pixels` if non-null, a new array of integers
     * otherwise
     * @throws IllegalArgumentException is `pixels` is non-null and
     * of length &lt; w*h
     */
    fun getPixels(
        img: BufferedImage,
        x: Int, y: Int, w: Int, h: Int, pixels: IntArray?
    ): IntArray {
        var pixels = pixels
        if (w == 0 || h == 0) {
            return IntArray(0)
        }

        if (pixels == null) {
            pixels = IntArray(w * h)
        } else require(pixels.size >= w * h) { ("pixels array must have a length" + " >= w*h") }

        val imageType = img.type
        if ((imageType == BufferedImage.TYPE_INT_ARGB || imageType == BufferedImage.TYPE_INT_RGB)) {
            val raster = img.raster
            return raster.getDataElements(x, y, w, h, pixels) as IntArray
        }

        // Unmanages the image
        return img.getRGB(x, y, w, h, pixels, 0, w)
    }

    /**
     *
     * Writes a rectangular area of pixels in the destination
     * `BufferedImage`. Calling this method on
     * an image of type different from `BufferedImage.TYPE_INT_ARGB`
     * and `BufferedImage.TYPE_INT_RGB` will unmanage the image.
     *
     * @param img the destination image
     * @param x the x location at which to start storing pixels
     * @param y the y location at which to start storing pixels
     * @param w the width of the rectangle of pixels to store
     * @param h the height of the rectangle of pixels to store
     * @param pixels an array of pixels, stored as integers
     * @throws IllegalArgumentException is `pixels` is non-null and
     * of length &lt; w*h
     */
    fun setPixels(
        img: BufferedImage,
        x: Int, y: Int, w: Int, h: Int, pixels: IntArray?
    ) {
        if (pixels == null || w == 0 || h == 0) {
            return
        } else require(pixels.size >= w * h) { ("pixels array must have a length" + " >= w*h") }

        val imageType = img.type
        if ((imageType == BufferedImage.TYPE_INT_ARGB || imageType == BufferedImage.TYPE_INT_RGB)) {
            val raster = img.raster
            raster.setDataElements(x, y, w, h, pixels)
        } else {
            // Unmanages the image
            img.setRGB(x, y, w, h, pixels, 0, w)
        }
    }
}
/**
 *
 * Returns a new compatible image with the same width, height and
 * transparency as the image specified as a parameter.
 *
 * @see java.awt.Transparency
 *
 * @see .createCompatibleImage
 * @see .createCompatibleImage
 * @see .createTranslucentCompatibleImage
 * @see .loadCompatibleImage
 * @see .toCompatibleImage
 * @param image the reference image from which the dimension and the
 * transparency of the new image are obtained
 * @return a new compatible `BufferedImage` with the same
 * dimension and transparency as `image`
 */