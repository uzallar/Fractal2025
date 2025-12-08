package app.fractal

import app.painting.convertation.Plain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class IterationsCalculatorTest {

    @Test
    fun `start view gives about 100 iterations`() {  //на стартовом виде около 100 итераций
        val plain = Plain(-2.0, 1.0, -1.0, 1.0)
        val n = IterationsCalculator.getMaxIterations(plain)
        assertTrue(n in 80..160, "Ожидали 80–160, получили $n")
    }

    @Test
    fun `zoom x10 gives more than 500 iterations`() { //при зуме ×10 — больше 500 итераций
        val plain = Plain(-0.8, -0.5, -0.15, 0.15) // ширина 0.3 → зум ≈10×
        val n = IterationsCalculator.getMaxIterations(plain)
        assertTrue(n > 500, "При зуме ×10 ожидали >500, получили $n")
    }


    @Test
    fun `iterations grow logarithmically`() { //итерации растут логарифмически(плавно)
        val p1 = Plain(-2.0, 1.0, -1.0, 1.0)      // зум 1
        val p2 = Plain(-0.5, 0.5, -0.5, 0.5)      // зум ~6
        val p3 = Plain(-0.05, 0.05, -0.05, 0.05)  // зум ~60

        val n1 = IterationsCalculator.getMaxIterations(p1)
        val n2 = IterationsCalculator.getMaxIterations(p2)
        val n3 = IterationsCalculator.getMaxIterations(p3)

        assertTrue((n1 < n2) && (n2 < n3), "Должно расти с зумом")
        assertTrue(n3 < 5000, "Даже при зуме ×60 не должно быть больше 5000")
    }
}