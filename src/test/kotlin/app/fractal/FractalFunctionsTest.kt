package app.fractal

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class FractalFunctionsTest {

    private val maxIterations = 100

    @Nested
    @DisplayName("Mandelbrot")
    inner class MandelbrotTests {

        @Test
        fun `точка внутри множества Мандельброта должна иметь большое число итераций`() {
            val result = FractalFunctions.mandelbrot(0.0, 0.0, maxIterations)
            assertEquals(1.0f, result, 0.0001f) // полностью внутри — n == maxIterations
        }

        @Test
        fun `точка далеко снаружи должна быстро выйти`() {
            val result = FractalFunctions.mandelbrot(3.0, 0.0, maxIterations)
            assertTrue(result < 0.1f) // быстрое значение
        }

        @ParameterizedTest(name = "c={0}+{1}i → iterations {2}")
        @CsvSource(
            "0.0, 0.0, 1.0",
            "-1.0, 0.0, 1.0",
            "-0.5, 0.0, 1.0",
            "0.25, 0.0, 0.99",   // точка на границе, но внутри
            "2.0, 0.0, 0.01"     // снаружи
        )
        fun `проверка известных точек Мандельброта`(xc: Double, yc: Double, expectedRatio: Double) {
            val result = FractalFunctions.mandelbrot(xc, yc, maxIterations)
            assertEquals(expectedRatio, result.toDouble(), 0.02) // небольшая погрешность допустима
        }
    }

    @Nested
    @DisplayName("Julia")
    inner class JuliaTests {

        // Используется фиксированный параметр c = -0.7 + 0.27015i

        @Test
        fun `центр Julia-фрактала должен быть внутри`() {
            val result = FractalFunctions.julia(0.0, 0.0, maxIterations)
            assertTrue(result > 0.8f) // точка 0+0i обычно глубоко внутри
        }

        @Test
        fun `точка далеко от центра должна быстро выйти`() {
            val result = FractalFunctions.julia(2.0, 2.0, maxIterations)
            assertTrue(result < 0.2f)
        }

        @ParameterizedTest(name = "z={0}+{1}i → iterations {2}")
        @CsvSource(
            "0.0, 0.0, 1.0",
            "0.3, 0.0, 0.95",
            "-0.3, 0.0, 0.9",
            "1.0, 1.0, 0.05"
        )
        fun `проверка известных точек Julia`(x: Double, y: Double, expectedRatio: Double) {
            val result = FractalFunctions.julia(x, y, maxIterations)
            assertEquals(expectedRatio, result.toDouble(), 0.05)
        }
    }

    @Nested
    @DisplayName("Tricorn")
    inner class TricornTests {

        @Test
        fun `точка (0,0) должна быть внутри трикорна`() {
            val result = FractalFunctions.tricorn(0.0, 0.0, maxIterations)
            assertEquals(1.0f, result, 0.0001f)
        }

        @Test
        fun `точка далеко снаружи должна быстро выйти`() {
            val result = FractalFunctions.tricorn(3.0, 0.0, maxIterations)
            assertTrue(result < 0.1f)
        }

        @ParameterizedTest(name = "c={0}+{1}i → iterations {2}")
        @CsvSource(
            "0.0, 0.0, 1.0",
            "-1.0, 0.0, 1.0",
            "-0.5, 0.0, 1.0"
        )
        fun `проверка известных точек Tricorn`(xc: Double, yc: Double, expectedRatio: Double) {
            val result = FractalFunctions.tricorn(xc, yc, maxIterations)
            assertEquals(expectedRatio, result.toDouble(), 0.05)
        }
    }

    @Nested
    @DisplayName("Cubic Mandelbrot")
    inner class CubicMandelbrotTests {

        @Test
        fun `точка (0,0) должна быть внутри кубического Мандельброта`() {
            val result = FractalFunctions.cubicMandelbrot(0.0, 0.0, maxIterations)
            assertEquals(1.0f, result, 0.0001f)
        }

        @Test
        fun `точка далеко снаружи должна быстро выйти`() {
            val result = FractalFunctions.cubicMandelbrot(2.0, 2.0, maxIterations)
            assertTrue(result < 0.2f)
        }
    }

    @Nested
    @DisplayName("getFractalByName")
    inner class GetFractalByNameTests {

        @ParameterizedTest(name = "имя '{0}' → Mandelbrot")
        @ValueSource(strings = ["mandelbrot", "Мандельброт", "MANDelbrot", "mandel"])
        fun `должен вернуть Mandelbrot для правильных имен`(name: String) {
            val func = FractalFunctions.getFractalByName(name)
            assertSame(FractalFunctions.mandelbrot, func)
        }

        @ParameterizedTest(name = "имя '{0}' → Julia")
        @ValueSource(strings = ["julia", "жюлиа", "Julia", "JULIA"])
        fun `должен вернуть Julia`(name: String) {
            val func = FractalFunctions.getFractalByName(name)
            assertSame(FractalFunctions.julia, func)
        }

        @ParameterizedTest(name = "имя '{0}' → Tricorn")
        @ValueSource(strings = ["tricorn", "трикорн", "Tricorn", "TRICORN"])
        fun `должен вернуть Tricorn`(name: String) {
            val func = FractalFunctions.getFractalByName(name)
            assertSame(FractalFunctions.tricorn, func)
        }

        @Test
        fun `неизвестное имя должно вернуть Mandelbrot`() {
            val func = FractalFunctions.getFractalByName("unknown")
            assertSame(FractalFunctions.mandelbrot, func)
        }

        @Test
        fun `пустое имя должно вернуть Mandelbrot`() {
            val func = FractalFunctions.getFractalByName("")
            assertSame(FractalFunctions.mandelbrot, func)
        }
    }
}