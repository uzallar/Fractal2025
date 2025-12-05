package app.fractal

import app.math.Complex
import kotlin.math.abs
//import androidx.compose.ui.graphics.drawscope.DrawScope

typealias FractalFunction = (Complex, Complex, Double, Int) -> Float

object FractalFunctions {
    val mandelbrot: FractalFunction = { start, c, escapeRadius, maxIterations ->
        val escapeRR = escapeRadius * escapeRadius
        val (xc, yc) = c
        var (x, y) = start

        var n = 0
        var xx = x * x
        var yy = y * y

        while (n < maxIterations && xx + yy < escapeRR) {
            val xy = x * y
            y = xy + xy + yc
            x = xx - yy + xc

            xx = x * x
            yy = y * y

            n++
        }

        n.toFloat() / maxIterations
    }


    val julia: FractalFunction =  { start, _, escapeRadius, maxIterations ->
        val c = Complex(-0.7, 0.27015)
        var result = 1f

//        for (i in 0 until maxIterations) {
//            current = current * current + c
//            if (current.absoluteValue > 2) {
//                result = i.toFloat() / maxIterations.toFloat()
//                break
//            }
//        }
        result
    }


    val burningShip: FractalFunction = { start, c, escapeRadius, maxIterations ->
        var z = Complex(0.0, 0.0)
        var result = 1f
//        for (i in 0 until maxIterations) {
//            z = Complex(abs(z.re), abs(z.im))
//            z = z * z + c
//            if (z.absoluteValue > 2) {
//                result = i.toFloat() / maxIterations.toFloat()
//                break
//            }
//        }
        result
    }


    val tricorn: FractalFunction = { start, c, escapeRadius, maxIterations ->
        var z = Complex(0.0, 0.0)
        var result = 1f
//        for (i in 0 until maxIterations) {
//            z = Complex(z.re, -z.im)
//            z = z * z + c
//            if (z.absoluteValue > 2) {
//                result = i.toFloat() / maxIterations.toFloat()
//                break
//            }
//        }
        result
    }


    val cubicMandelbrot: FractalFunction = { start, c, escapeRadius, maxIterations ->
        var z = Complex(0.0, 0.0)
        var result = 1f
//        for (i in 0 until maxIterations) {
//            val z2 = z * z
//            z = z2 * z + c
//            if (z.absoluteValue > 2) {
//                result = i.toFloat() / maxIterations.toFloat()
//                break
//            }
//        }
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

