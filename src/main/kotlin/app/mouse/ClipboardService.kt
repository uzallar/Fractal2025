package app.mouse

import androidx.compose.ui.geometry.Offset
import app.painting.convertation.Converter
import app.painting.convertation.Plain
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

object ClipboardService {
    fun copyFractalCoordinates(screenPosition: Offset, plain: Plain) {
        val fractalX = Converter.xScr2Crt(screenPosition.x, plain)
        val fractalY = Converter.yScr2Crt(screenPosition.y, plain)

        val coordinates = "x: ${"%.12e".format(fractalX)}, y: ${"%.12e".format(fractalY)}"

        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val stringSelection = StringSelection(coordinates)
        clipboard.setContents(stringSelection, null)
    }

    fun getCoordinatesString(screenPosition: Offset, plain: Plain): String {
        val fractalX = Converter.xScr2Crt(screenPosition.x, plain)
        val fractalY = Converter.yScr2Crt(screenPosition.y, plain)
        return "x: ${"%.6e".format(fractalX)}\ny: ${"%.6e".format(fractalY)}"
    }
}