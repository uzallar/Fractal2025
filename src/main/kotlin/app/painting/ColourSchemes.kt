package app.painting

//import androidx.compose.ui.graphics.Color
import org.jetbrains.skia.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.abs
import kotlin.math.PI

typealias ColorScheme = (Float) -> Int


object ColorSchemes {
    val standard: ColorScheme = { probability ->
        if (probability == 1f) {
            Color.BLACK
        } else {
            val t = probability * 2f * PI.toFloat()

            val baseR = 0.95f
            val baseG = 0.45f
            val baseB = 0.75f

            val variationR = 0.1f * sin(t * 3f)
            val variationG = 0.1f * sin(t * 5f + 1f)
            val variationB = 0.1f * sin(t * 7f + 2f)


            Color.makeRGB(
                r = ((baseR + variationR).coerceIn(0f, 1f) * 255f).toInt(),
                g = ((baseG + variationG).coerceIn(0f, 1f) * 255f).toInt(),
                b = ((baseB + variationB).coerceIn(0f, 1f) * 255f).toInt(),
            )
        }
    }


    val monochrome: ColorScheme = { probability ->
        if (probability == 1f) {
            Color.BLACK
        } else {
            val gray = ((1f - probability) * 255f).toInt()
            Color.makeRGB(gray, gray, gray)
        }
    }


    val fire: ColorScheme = { probability ->
        if (probability == 1f) {
            Color.BLACK
        } else {
            Color.makeRGB(
                r = (probability * 255f).toInt(),
                g = (probability * 0.5f * 255f).toInt(),
                b = 0
            )
        }
    }


    val ice: ColorScheme = { probability ->
        if (probability == 1f) {
            Color.BLACK
        } else {
            Color.makeRGB(
                r = 0,
                g = (probability * 255f).toInt(),
                b = (probability * 1.5f.coerceAtMost(1f)).toInt()
            )
        }
    }


    val rainbow: ColorScheme = { probability ->
        if (probability == 1f) {
            Color.BLACK
        } else {
            val hue = probability * 360f
            // Конвертация HSV в RGB
            val c = (1 - abs(2 * 0.5f - 1)) * 0.8f
            val x = c * (1 - abs((hue / 60) % 2 - 1))
            val m = 0.5f - c / 2

            var r1 = 0f
            var g1 = 0f
            var b1 = 0f

            when {
                hue < 60 -> {
                    r1 = c
                    g1 = x
                    b1 = 0f
                }
                hue < 120 -> {
                    r1 = x
                    g1 = c
                    b1 = 0f
                }
                hue < 180 -> {
                    r1 = 0f
                    g1 = c
                    b1 = x}
                hue < 240 -> {
                    r1 = 0f
                    g1 = x
                    b1 = c}
                hue < 300 -> {
                    r1 = x
                    g1 = 0f
                    b1 = c
                }
                else -> {
                    r1 = c
                    g1 = 0f
                    b1 = x}
            }
            Color.makeRGB(
                r = ((r1 + m) * 255f).toInt(),
                g = ((g1 + m) * 255f).toInt(),
                b = ((b1 + m) * 255f).toInt()
            )
        }
    }


//    val cosmic: ColorScheme = { probability ->
//        if (probability == 1f) {
//            Color(0x0F, 0x08, 0x25) // Темно-фиолетовый
//        } else {
//            Color(
//                red = probability * 0.3f,
//                green = probability * 0.1f,
//                blue = probability * 0.8f + 0.2f
//            )
//        }
//    }

//    val softPink: ColorScheme = { probability ->
//        if (probability == 1f) {
//            Color.Black
//        } else {
//            // Мягкие пастельные розовые оттенки
//            val t = probability * PI.toFloat()
//
//            Color(
//                red = 0.95f - 0.2f * sin(t * 2f), // 0.75-0.95
//                green = 0.6f - 0.3f * sin(t * 3f + 0.5f), // 0.3-0.6
//                blue = 0.8f - 0.2f * sin(t * 4f + 1f) // 0.6-0.8
//            )
//        }
//    }


    fun getColorSchemeByName(name: String): ColorScheme {
        return when (name.lowercase()) {
            "standard", "стандартная" -> standard
            "fire", "огненная" -> fire
            "rainbow", "радужная" -> rainbow
            "ice", "ледяная" -> ice
            else -> standard
        }
    }
}