package app.painting

import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.abs


typealias ColorScheme = (Float) -> Color


object ColorSchemes {
    val standard: ColorScheme = { probability ->
        if (probability == 1f) {
            Color.Black
        } else {
            Color(
                red = abs(cos(7 * probability)),
                green = abs(sin(12 * (1f - probability))),
                blue = abs(sin(4 * probability) * cos(4 * (1 - probability)))
            )
        }
    }


    val monochrome: ColorScheme = { probability ->
        if (probability == 1f) {
            Color.Black
        } else {
            val gray = 1f - probability
            Color(gray, gray, gray)
        }
    }


    val fire: ColorScheme = { probability ->
        if (probability == 1f) {
            Color.Black
        } else {
            Color(
                red = probability,
                green = probability * 0.5f,
                blue = 0f
            )
        }
    }


    val ice: ColorScheme = { probability ->
        if (probability == 1f) {
            Color.Black
        } else {
            Color(
                red = 0f,
                green = probability,
                blue = probability * 1.5f.coerceAtMost(1f)
            )
        }
    }


    val rainbow: ColorScheme = { probability ->
        if (probability == 1f) {
            Color.Black
        } else {
            val hue = probability * 360f
            // Конвертация HSV в RGB
            val c = (1 - abs(2 * 0.5f - 1)) * 0.8f
            val x = c * (1 - abs((hue / 60) % 2 - 1))
            val m = 0.5f - c / 2

            val (r1, g1, b1) = when {
                hue < 60 -> Triple(c, x, 0f)
                hue < 120 -> Triple(x, c, 0f)
                hue < 180 -> Triple(0f, c, x)
                hue < 240 -> Triple(0f, x, c)
                hue < 300 -> Triple(x, 0f, c)
                else -> Triple(c, 0f, x)
            }

            Color(r1 + m, g1 + m, b1 + m)
        }
    }


    val cosmic: ColorScheme = { probability ->
        if (probability == 1f) {
            Color(0x0F, 0x08, 0x25) // Темно-фиолетовый
        } else {
            Color(
                red = probability * 0.3f,
                green = probability * 0.1f,
                blue = probability * 0.8f + 0.2f
            )
        }
    }


    fun getColorSchemeByName(name: String): ColorScheme {
        return when (name.lowercase()) {
            "standard", "стандартная" -> standard
            "fire", "огненная" -> fire
            "rainbow", "радужная" -> rainbow
            "cosmic", "космическая" -> cosmic
            else -> standard // по умолчанию
        }
    }
}
