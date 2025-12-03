package app.fractal

import app.math.Complex
import kotlin.math.abs
//import androidx.compose.ui.graphics.drawscope.DrawScope

typealias FractalFunction = (Complex) -> Float

object FractalFunctions {
    val mandelbrot: FractalFunction = { c ->
        var z = Complex(0.0, 0.0)
        val maxIterations = 200
        var result = 1f
        for (i in 0 until maxIterations) {
            z = z * z + c
            if (z.absoluteValue > 2) {
                result = i.toFloat() / maxIterations.toFloat()
                break
            }
        }
        result
    }


    val julia: FractalFunction = { z ->
        val c = Complex(-0.7, 0.27015)
        val maxIterations = 200
        var current = z
        var result = 1f
        for (i in 0 until maxIterations) {
            current = current * current + c
            if (current.absoluteValue > 2) {
                result = i.toFloat() / maxIterations.toFloat()
                break
            }
        }
        result
    }


    val burningShip: FractalFunction = { c ->
        var z = Complex(0.0, 0.0)
        val maxIterations = 200
        var result = 1f
        for (i in 0 until maxIterations) {
            z = Complex(abs(z.re), abs(z.im))
            z = z * z + c
            if (z.absoluteValue > 2) {
                result = i.toFloat() / maxIterations.toFloat()
                break
            }
        }
        result
    }


    val tricorn: FractalFunction = { c ->
        var z = Complex(0.0, 0.0)
        val maxIterations = 200
        var result = 1f
        for (i in 0 until maxIterations) {
            z = Complex(z.re, -z.im)
            z = z * z + c
            if (z.absoluteValue > 2) {
                result = i.toFloat() / maxIterations.toFloat()
                break
            }
        }
        result
    }


    val cubicMandelbrot: FractalFunction = { c ->
        var z = Complex(0.0, 0.0)
        val maxIterations = 200
        var result = 1f
        for (i in 0 until maxIterations) {
            val z2 = z * z
            z = z2 * z + c
            if (z.absoluteValue > 2) {
                result = i.toFloat() / maxIterations.toFloat()
                break
            }
        }
        result
    }

    fun getFractalByName(name: String): FractalFunction {
        return when (name.lowercase()) {
            "mandelbrot", "мандельброт" -> mandelbrot
            "julia", "жюлиа" -> julia
            "burningship", "корабль", "горящий корабль" -> burningShip
            "tricorn", "трикорн" -> tricorn
            "cubic", "кубический" -> cubicMandelbrot
            else -> mandelbrot
        }
    }
}

