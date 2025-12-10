package app.painting


import androidx.compose.ui.graphics.drawscope.DrawScope
import app.fractal.FractalFunction
import app.painting.convertation.Converter
import app.painting.convertation.Plain
import org.jetbrains.skia.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Color
import kotlinx.coroutines.*

// ЖЕСТКИЙ ХАРДКОД (ЯВНО НЕ ПОМЕЧАЮ)
val bytePixels = ByteArray(2600 * 2600 * 4)

class FractalPainter(private val plain: Plain,
                     val fractalFunction: FractalFunction,
                     val colorScheme: ColorScheme,
                     private val maxIterationsProvider: () -> Int = { 200 }
): Painter {


    override suspend fun paint(scope: DrawScope, image: ImageBitmap){
        scope.drawImage(image)
    }

    suspend fun generateImage(scope: DrawScope): ImageBitmap {
        plain.width = scope.size.width
        plain.height = scope.size.height

        val iterations = maxIterationsProvider()
        val width = plain.width.toInt()
        val height = plain.height.toInt()
        //val bytePixels = ByteArray(width * height * 4)

        val n = 8
        val jobs = mutableListOf<Job>()
        val blockSizeX = (width + n - 1) / n
        val blockSizeY = (height + n - 1) / n

        runBlocking {
            for (blockX in 0 until n) {
                for (blockY in 0 until n) {
                    val job = launch(Dispatchers.Default) {
                        val startX = blockX * blockSizeX
                        val endX = minOf((blockX + 1) * blockSizeX, width)
                        val startY = blockY * blockSizeY
                        val endY = minOf((blockY + 1) * blockSizeY, height)

                        for (x in startX until endX) {
                            for (y in startY until endY) {
                                val zx = Converter.xScr2Crt(x.toFloat(), plain)
                                val zy = Converter.yScr2Crt(y.toFloat(), plain)

                                val probability = fractalFunction(zx, zy, iterations)
                                val color = colorScheme(probability)

                                val r = Color.getR(color)
                                val g = Color.getG(color)
                                val b = Color.getB(color)
                                val a = 255

                                val pos = (y * width + x) * 4
                                bytePixels[pos] = b.toByte()
                                bytePixels[pos + 1] = g.toByte()
                                bytePixels[pos + 2] = r.toByte()
                                bytePixels[pos + 3] = a.toByte()
                            }
                        }
                    }
                    jobs.add(job)
                }
            }
            jobs.joinAll()
        }
        System.gc()

        val skiaBitmap = Bitmap()
        skiaBitmap.allocN32Pixels(width, height)

        skiaBitmap.installPixels(
            ImageInfo.makeN32(width, height, ColorAlphaType.OPAQUE),
            bytePixels,
            width * 4
        )

        val imageBitmap = skiaBitmap.asComposeImageBitmap()
        return imageBitmap
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