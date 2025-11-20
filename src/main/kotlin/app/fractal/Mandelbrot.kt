package app.fractal

import app.math.Complex

class Mandelbrot(
    val nMax: Int = 200,
    val r: Double = 2.0,
) {
    fun isInSet(c: Complex): Float{
        val z = Complex()
        repeat(nMax) { n ->
            z *= z
            z += c
            if (z.absoluteValue2 >= r * r) return n.toFloat() / nMax
        }
        return 1f
    }
}