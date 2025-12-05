package app.painting

import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.abs
import kotlin.math.PI

typealias ColorScheme = (Float) -> Color

//TODO: оптмизировать по памяти

object ColorSchemes {
    // ИЗМЕНЕНА НА РОЗОВУЮ СХЕМУ
    val standard: ColorScheme = { probability ->
        if (probability == 1f) {
            Color.Black
        } else {
            // Розовая цветовая схема
            // Основной розовый цвет с вариациями
            val pinkHue = 330f // Розовый оттенок (330 градусов в HSV)
            val hue = pinkHue / 360f

            // Создаём красивые розовые оттенки
            val t = probability * 2f * PI.toFloat()

            // Вариации розового: от нежно-розового к насыщенному
            val baseR = 0.95f // Базовый красный для розового
            val baseG = 0.45f // Базовый зелёный для розового
            val baseB = 0.75f // Базовый синий для розового

            // Добавляем волнообразные вариации для красоты
            val variationR = 0.1f * sin(t * 3f)
            val variationG = 0.1f * sin(t * 5f + 1f)
            val variationB = 0.1f * sin(t * 7f + 2f)

            Color(
                red = (baseR + variationR).coerceIn(0f, 1f),
                green = (baseG + variationG).coerceIn(0f, 1f),
                blue = (baseB + variationB).coerceIn(0f, 1f)
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

    // ДОБАВИМ ЕЩЁ ОДНУ РОЗОВУЮ СХЕМУ ДЛЯ РАЗНООБРАЗИЯ
    val softPink: ColorScheme = { probability ->
        if (probability == 1f) {
            Color.Black
        } else {
            // Мягкие пастельные розовые оттенки
            val t = probability * PI.toFloat()

            Color(
                red = 0.95f - 0.2f * sin(t * 2f), // 0.75-0.95
                green = 0.6f - 0.3f * sin(t * 3f + 0.5f), // 0.3-0.6
                blue = 0.8f - 0.2f * sin(t * 4f + 1f) // 0.6-0.8
            )
        }
    }


    fun getColorSchemeByName(name: String): ColorScheme {
        return when (name.lowercase()) {
            "standard", "стандартная" -> standard
            "fire", "огненная" -> fire
            "rainbow", "радужная" -> rainbow
            "cosmic", "космическая" -> cosmic
            else -> standard
        }
    }
}