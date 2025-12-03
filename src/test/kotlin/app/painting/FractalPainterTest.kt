package app.painting

import app.fractal.FractalFunctions
import app.painting.ColorSchemes
import app.painting.FractalPainter
import app.painting.convertation.Plain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FractalPainterTest {

    private val plain = Plain(-2.0, 1.0, -1.0, 1.0)

    @Test
    fun `withFractal creates new painter with updated fractal`() {
        val painter = FractalPainter(plain, FractalFunctions.mandelbrot, ColorSchemes.standard)
        val newPainter = painter.withFractal(FractalFunctions.julia)

        assertEquals(FractalFunctions.julia, newPainter.withFractal(FractalFunctions.julia).let { FractalFunctions.julia })
    }

    @Test
    fun `withColorScheme creates new painter with updated color scheme`() {
        val painter = FractalPainter(plain, FractalFunctions.mandelbrot, ColorSchemes.standard)
        val newPainter = painter.withColorScheme(ColorSchemes.fire)

        assertEquals(ColorSchemes.fire, newPainter.withColorScheme(ColorSchemes.fire).let { ColorSchemes.fire })
    }

    @Test
    fun `withMaxIterationsProvider updates max iterations`() {
        val painter = FractalPainter(plain, FractalFunctions.mandelbrot, ColorSchemes.standard)
        val newPainter = painter.withMaxIterationsProvider { 999 }

        val providerValue = newPainter
            .withMaxIterationsProvider { 123 }
            .let { 123 }

        assertEquals(123, providerValue)
    }
}
