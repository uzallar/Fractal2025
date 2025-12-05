package app.painting

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import app.fractal.FractalFunction
import kotlinx.coroutines.coroutineScope
import app.math.Complex
import app.painting.convertation.Converter
import app.painting.convertation.Plain
import org.jetbrains.skia.*
//import kotlin.concurrent.thread
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Color
import java.awt.image.BufferedImage


class FractalPainter(private val plain: Plain,
                     val fractalFunction: FractalFunction,
                     val colorScheme: ColorScheme,
                     private val maxIterationsProvider: () -> Int = { 200 }
): Painter {
    override suspend fun paint(scope: DrawScope) {
        plain.width = scope.size.width
        plain.height = scope.size.height

        val width = plain.width.toInt()
        val height = plain.height.toInt()

        // TODO: ДОПИЛИТЬ НОРМАЛЬНО
        //val xCenter = (plain.xMax + plain.xMin + 1) / 2
        //val yCenter = (plain.yMax + plain.yMin) / 2

        val bytePixels = ByteArray(width * height * 4)
        for (x in 0 until width) {
            for (y in 0 until height) {
                val complex = Complex(
                    Converter.xScr2Crt(x.toFloat(), plain),
                    Converter.yScr2Crt(y.toFloat(), plain)
                )
                val probability = fractalFunction(
                    Complex(0.0, 0.0), complex, 2.0, maxIterationsProvider())
                val color = colorScheme(probability)

                val r = (color.red * 255 + 0.5).toInt()
                val g = (color.green * 255 + 0.5).toInt()
                val b = (color.blue * 255 + 0.5).toInt()
                val a = 255

                val pos = (y * width + x) * 4

                bytePixels[pos] = b.toByte()
                bytePixels[pos + 1] = g.toByte()
                bytePixels[pos + 2] = r.toByte()
                bytePixels[pos + 3] = a.toByte()

           }
        }
        val skiaBitmap = Bitmap()
        skiaBitmap.allocN32Pixels(width, height)

        skiaBitmap.installPixels(
            ImageInfo.makeN32(width, height, ColorAlphaType.OPAQUE),
            bytePixels,
            width * 4
        )

        val imageBitmap = skiaBitmap.asComposeImageBitmap()
        scope.drawImage(imageBitmap)
    }

    fun withFractal(newFractal: FractalFunction): FractalPainter {
        return FractalPainter(plain, newFractal, colorScheme, maxIterationsProvider)
    }

    fun withColorScheme(newColorScheme: ColorScheme): FractalPainter {
        return FractalPainter(plain, fractalFunction, newColorScheme, maxIterationsProvider)
    }

    fun withMaxIterationsProvider(newProvider: () -> Int): FractalPainter {
        return FractalPainter(plain, fractalFunction, colorScheme, newProvider)
    }
}