package app.fractal

import app.fractal.FractalFunctions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FractalFunctionsTest {

    @Test
    fun `getFractalByName returns Mandelbrot`() {
        val f = FractalFunctions.getFractalByName("mandelbrot")
        assertEquals(FractalFunctions.mandelbrot, f)
    }

    @Test
    fun `getFractalByName returns Julia`() {
        val f = FractalFunctions.getFractalByName("julia")
        assertEquals(FractalFunctions.julia, f)
    }

    @Test
    fun `getFractalByName returns BurningShip`() {
        val f = FractalFunctions.getFractalByName("burningship")
        assertEquals(FractalFunctions.burningShip, f)
    }

    @Test
    fun `getFractalByName returns Tricorn`() {
        val f = FractalFunctions.getFractalByName("tricorn")
        assertEquals(FractalFunctions.tricorn, f)
    }

    @Test
    fun `getFractalByName returns Mandelbrot on unknown`() {
        val f = FractalFunctions.getFractalByName("unknownFractal")
        assertEquals(FractalFunctions.mandelbrot, f)
    }
}
