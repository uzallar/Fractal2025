package app.painting

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.coroutineScope
import app.fractal.FractalFunction
import app.math.Complex
import app.painting.convertation.Converter
import app.painting.convertation.Plain

class FractalPainter(
    private val plain: Plain,
    private val fractalFunction: FractalFunction, // Лямбда для фрактала
    private val colorScheme: ColorScheme,         // Лямбда для цветов
    private val maxIterationsProvider: () -> Int = { 200 }
): Painter {

    override suspend fun paint(scope: DrawScope) {
        plain.width = scope.size.width
        plain.height = scope.size.height

        for (iX in 0..<plain.width.toInt()) {
            coroutineScope {
                val x = iX.toFloat()
                repeat(plain.height.toInt()) { iY ->
                    val y = iY.toFloat()

                    val complex = Complex(
                        Converter.xScr2Crt(x, plain),
                        Converter.yScr2Crt(y, plain),
                    )

                    val probability = fractalFunction(complex)

                    val color = colorScheme(probability)

                    scope.drawRect(
                        color,
                        Offset(x, y),
                        Size(1f, 1f),
                    )
                }
            }
        }
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
