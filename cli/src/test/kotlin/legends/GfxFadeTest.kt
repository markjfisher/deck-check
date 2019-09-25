package legends

import legends.gfx.GfxFade
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel


class GfxFadeTest {
    @Test
    @Disabled
    fun `colour fade with 2 colours`() {
        val c1 = Color(0xff, 0, 0, 0x7f)
        val c2 = Color( 0, 0, 0xff, 0xff)

        val width = 320
        val height = 46
        val bufferedImage: BufferedImage = GfxFade.createColourFade(c1, c2, width, height)

        val frame = MainFrame(bufferedImage)
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