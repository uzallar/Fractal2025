package app.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import app.painting.FractalPainter
import app.painting.convertation.Plain
import kotlinx.coroutines.runBlocking
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.math.roundToInt

object ExporterJPG {

    fun exportToJpg(
        image: ImageBitmap,
        plain: Plain,
        zoomText: String,
        maxIterations: Int
    ) {
        val chooser = JFileChooser().apply {
            dialogTitle = "Сохранить фрактал как JPG"
            fileFilter = FileNameExtensionFilter("JPEG изображение (*.jpg)", "jpg", "jpeg")
            selectedFile = File("fractal_${System.currentTimeMillis()}.jpg")
        }

        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return

        var file = chooser.selectedFile!!
        if (!file.name.lowercase().endsWith(".jpg") && !file.name.lowercase().endsWith(".jpeg")) {
            file = File(file.absolutePath + ".jpg")
        }

        val h = image.height
        val w = image.width

        val original = image.toAwtImage()
        val padding = 56
        val result = BufferedImage(w, h + padding, BufferedImage.TYPE_INT_RGB)
        val g = result.createGraphics().apply {
            drawImage(original, 0, 0, null)


            color = java.awt.Color(15, 15, 15)
            fillRect(0, h, w, padding)


            color = java.awt.Color.WHITE
            font = Font("Segoe UI", Font.BOLD, 19)

            val text = "x: ${"%.12f".format(plain.xMin)} … ${"%.12f".format(plain.xMax)}    " +
                    "y: ${"%.12f".format(plain.yMin)} … ${"%.12f".format(plain.yMax)}    " +
                    "Зум: $zoomText    Количество итераций: $maxIterations"

            val fm = fontMetrics
            val x = (w - fm.stringWidth(text)) / 2f
            drawString(text, x, h + padding - 16f)
        }
        g.dispose()

        ImageIO.write(result, "jpg", file)
    }
}