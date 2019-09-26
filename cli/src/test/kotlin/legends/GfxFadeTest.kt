package legends

import legends.gfx.GfxFade
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel


class GfxFadeTest {
    @Test
    @Disabled
    fun `colour fade with 2 colours`() {
        val c1 = Color(0xff, 0x00, 0x00, 0xff)
        val c2 = Color(0x00, 0xff, 0x00, 0xff)
        val c3 = Color(0x00, 0x00, 0xff, 0xff)

        val width = 320
        val height = 80
        val bufferedImage: BufferedImage = GfxFade.createColourFade(c1, c2, c3, width, height, 0.1f)

        val frame = MainFrame(bufferedImage)
        frame.isVisible = true

        Thread.sleep(10000)
    }

    @Test
    @Disabled
    fun `merge two images`() {
        val i1 = ImageIO.read(this::class.java.getResource("/test-image1.png"))
        val i2 = ImageIO.read(this::class.java.getResource("/test-image2.png"))

        val merged = GfxFade.mergeImages(i1, i2, 500, 100, 0.5f, 0.2f)

        val frame = MainFrame(merged)
        frame.isVisible = true

        Thread.sleep(10000)
    }

    @Test
    @Disabled
    fun `merge two images with one shifted`() {
        val i1 = ImageIO.read(this::class.java.getResource("/test-image1.png"))
        val i2 = ImageIO.read(this::class.java.getResource("/test-image2.png")).getSubimage(250, 0, 250, 100)

        val merged = GfxFade.mergeImages(i1, i2, 500, 100, 0.75f, 0.2f)

        val frame = MainFrame(merged)
        frame.isVisible = true

        Thread.sleep(10000)
    }

}

class MainFrame(private val image: BufferedImage): JFrame() {
    init {
        createUI()
    }

    private fun createUI() {
        title = "jframe"
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(image.width + 2, image.height + 2)

        val panel = JPanel()
        val label = JLabel(ImageIcon(image))
        panel.add(label)

        setDefaultLookAndFeelDecorated(true)
        add(panel)

        setLocationRelativeTo(null)

        pack()
    }

}