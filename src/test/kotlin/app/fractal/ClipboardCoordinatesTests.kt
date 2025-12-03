package app.mouse

import androidx.compose.ui.geometry.Offset
import app.painting.convertation.Converter
import app.painting.convertation.Plain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.util.Locale

class ClipboardCoordinatesTests {

    @Test
    fun `getCoordinatesString returns correct format`() {
        val plain = createTestPlain()

        val testPoints = listOf(
            Offset(0f, 0f) to "левый верхний угол",
            Offset(400f, 300f) to "центр",
            Offset(800f, 600f) to "правый нижний угол"
        )

        for ((position, description) in testPoints) {
            val coordinates = ClipboardService.getCoordinatesString(position, plain)

            // Проверяем базовый формат
            assertTrue(coordinates.contains("x:"), "$description: должна быть координата x")
            assertTrue(coordinates.contains("y:"), "$description: должна быть координата y")

            // Проверяем формат (две строки)
            val lines = coordinates.trim().split("\n")
            assertEquals(2, lines.size, "$description: должно быть две строки")

            // Проверяем начало строк
            assertTrue(lines[0].startsWith("x:"), "$description: первая строка должна начинаться с x:")
            assertTrue(lines[1].startsWith("y:"), "$description: вторая строка должна начинаться с y:")

            // Проверяем научную нотацию
            assertTrue(lines[0].contains("e") || lines[0].contains("E"),
                "$description: координата x должна быть в научной нотации")
            assertTrue(lines[1].contains("e") || lines[1].contains("E"),
                "$description: координата y должна быть в научной нотации")
        }
    }

    @Test
    fun `getCoordinatesString shows correct corner values`() {
        val plain = createTestPlain()

        // Левый верхний угол должен дать (-2.0, 1.0)
        val topLeftCoords = ClipboardService.getCoordinatesString(Offset(0f, 0f), plain)
        val topLeftLines = topLeftCoords.trim().split("\n")

        val topLeftX = parseLocalizedDouble(topLeftLines[0].substringAfter(":").trim())
        val topLeftY = parseLocalizedDouble(topLeftLines[1].substringAfter(":").trim())

        assertEquals(-2.0, topLeftX, 0.001, "Левый верхний угол: x должен быть -2.0")
        assertEquals(1.0, topLeftY, 0.001, "Левый верхний угол: y должен быть 1.0")

        // Правый нижний угол должен дать (1.0, -1.0)
        val bottomRightCoords = ClipboardService.getCoordinatesString(Offset(800f, 600f), plain)
        val bottomRightLines = bottomRightCoords.trim().split("\n")

        val bottomRightX = parseLocalizedDouble(bottomRightLines[0].substringAfter(":").trim())
        val bottomRightY = parseLocalizedDouble(bottomRightLines[1].substringAfter(":").trim())

        assertEquals(1.0, bottomRightX, 0.001, "Правый нижний угол: x должен быть 1.0")
        assertEquals(-1.0, bottomRightY, 0.001, "Правый нижний угол: y должен быть -1.0")
    }

    @Test
    fun `getCoordinatesString for center point`() {
        val plain = createTestPlain()

        // Центр экрана должен дать (-0.5, 0.0)
        val centerCoords = ClipboardService.getCoordinatesString(Offset(400f, 300f), plain)
        val lines = centerCoords.trim().split("\n")

        val centerX = parseLocalizedDouble(lines[0].substringAfter(":").trim())
        val centerY = parseLocalizedDouble(lines[1].substringAfter(":").trim())

        assertEquals(-0.5, centerX, 0.001, "Центр: x должен быть -0.5")
        assertEquals(0.0, centerY, 0.001, "Центр: y должен быть 0.0")
    }

    @Test
    fun `coordinates match converter functions`() {
        val plain = createTestPlain()

        val testPoints = listOf(
            Offset(200f, 150f),
            Offset(400f, 300f),
            Offset(600f, 450f)
        )

        for (position in testPoints) {
            // Вычисляем через Converter
            val expectedX = Converter.xScr2Crt(position.x, plain)
            val expectedY = Converter.yScr2Crt(position.y, plain)

            // Получаем строку из ClipboardService
            val coordinates = ClipboardService.getCoordinatesString(position, plain)
            val lines = coordinates.trim().split("\n")

            // Используем функцию для парсинга с учетом локальных настроек
            val actualX = parseLocalizedDouble(lines[0].substringAfter(":").trim())
            val actualY = parseLocalizedDouble(lines[1].substringAfter(":").trim())

            // Проверяем совпадение
            assertEquals(expectedX, actualX, 0.000001,
                "Координата x должна совпадать с Converter.xScr2Crt для точки $position")
            assertEquals(expectedY, actualY, 0.000001,
                "Координата y должна совпадать с Converter.yScr2Crt для точки $position")
        }
    }

    @Test
    fun `different positions give different coordinates`() {
        val plain = createTestPlain()

        // Три разные точки
        val point1 = Offset(100f, 100f)
        val point2 = Offset(400f, 300f)
        val point3 = Offset(700f, 500f)

        val coords1 = ClipboardService.getCoordinatesString(point1, plain)
        val coords2 = ClipboardService.getCoordinatesString(point2, plain)
        val coords3 = ClipboardService.getCoordinatesString(point3, plain)

        // Все должны быть разными
        assertNotEquals(coords1, coords2, "Координаты разных точек должны отличаться")
        assertNotEquals(coords2, coords3, "Координаты разных точек должны отличаться")
        assertNotEquals(coords1, coords3, "Координаты разных точек должны отличаться")

        // Извлекаем числовые значения для проверки порядка (с учетом локали)
        fun extractValue(coords: String, lineIndex: Int): Double {
            val lines = coords.trim().split("\n")
            return parseLocalizedDouble(lines[lineIndex].substringAfter(":").trim())
        }

        val x1 = extractValue(coords1, 0)
        val x2 = extractValue(coords2, 0)
        val x3 = extractValue(coords3, 0)

        // Проверяем порядок по X (слева направо)
        assertTrue(x1 < x2, "X слева должен быть меньше X в центре")
        assertTrue(x2 < x3, "X в центре должен быть меньше X справа")

        // Проверяем порядок по Y (в математических координатах Y направлен вверх)
        val y1 = extractValue(coords1, 1)
        val y2 = extractValue(coords2, 1)
        val y3 = extractValue(coords3, 1)

        // В математических координатах: верх экрана = 1.0, низ = -1.0
        // Поэтому y1 > y2 > y3
        assertTrue(y1 > y2, "Y сверху должен быть больше Y в центре")
        assertTrue(y2 > y3, "Y в центре должен быть больше Y снизу")
    }

    @Test
    fun `coordinates change with zoom level`() {
        // Широкий вид
        val wideView = createTestPlain()

        // Увеличенный вид (10x zoom)
        val zoomedView = Plain(-0.2, 0.0, -0.1, 0.1)
        zoomedView.width = 800f  // Используем Float
        zoomedView.height = 600f // Используем Float

        val screenPosition = Offset(400f, 300f)

        val wideCoords = ClipboardService.getCoordinatesString(screenPosition, wideView)
        val zoomedCoords = ClipboardService.getCoordinatesString(screenPosition, zoomedView)

        // Координаты должны быть разными
        assertNotEquals(wideCoords, zoomedCoords,
            "Координаты должны различаться при разном зуме")

        // Извлекаем X координаты
        fun extractX(coords: String): Double {
            val lines = coords.trim().split("\n")
            return parseLocalizedDouble(lines[0].substringAfter(":").trim())
        }

        val wideX = extractX(wideCoords)
        val zoomedX = extractX(zoomedCoords)

        // При зуме координаты должны быть ближе к центру
        assertTrue(Math.abs(zoomedX) < Math.abs(wideX),
            "При увеличении координаты должны быть ближе к центру области. " +
                    "wideX=$wideX, zoomedX=$zoomedX")
    }

    @Test
    fun `getCoordinatesString uses scientific notation`() {
        val plain = createTestPlain()

        val position = Offset(123.456f, 789.012f)
        val coordinates = ClipboardService.getCoordinatesString(position, plain)

        val lines = coordinates.trim().split("\n")
        val xStr = lines[0].substringAfter(":").trim()
        val yStr = lines[1].substringAfter(":").trim()

        // Проверяем, что используется научная нотация (есть 'e')
        assertTrue(xStr.contains("e") || xStr.contains("E"),
            "Должна использоваться научная нотация для X")
        assertTrue(yStr.contains("e") || yStr.contains("E"),
            "Должна использоваться научная нотация для Y")

        // Проверяем наличие десятичного разделителя (',' или '.')
        assertTrue(xStr.contains(",") || xStr.contains("."),
            "Должен быть десятичный разделитель (',' или '.') для X")
        assertTrue(yStr.contains(",") || yStr.contains("."),
            "Должен быть десятичный разделитель (',' или '.') для Y")
    }

    @Test
    fun `coordinates have correct order`() {
        val plain = createTestPlain()

        val left = Offset(200f, 300f)
        val right = Offset(600f, 300f)
        val top = Offset(400f, 100f)
        val bottom = Offset(400f, 500f)

        // Извлекаем координаты с учетом локали
        fun getX(position: Offset): Double {
            val coords = ClipboardService.getCoordinatesString(position, plain)
            val line = coords.trim().split("\n")[0]
            return parseLocalizedDouble(line.substringAfter(":").trim())
        }

        fun getY(position: Offset): Double {
            val coords = ClipboardService.getCoordinatesString(position, plain)
            val line = coords.trim().split("\n")[1]
            return parseLocalizedDouble(line.substringAfter(":").trim())
        }

        // Проверяем порядок по X
        val xLeft = getX(left)
        val xRight = getX(right)
        assertTrue(xLeft < xRight, "Точка слева должна иметь меньший X")

        // Проверяем порядок по Y (в математических координатах Y направлен вверх)
        val yTop = getY(top)
        val yBottom = getY(bottom)
        assertTrue(yTop > yBottom, "Точка сверху должна иметь больший Y")
    }

    // Вспомогательная функция для создания тестового Plain
    private fun createTestPlain(): Plain {
        val plain = Plain(-2.0, 1.0, -1.0, 1.0)
        plain.width = 800f  // Float
        plain.height = 600f // Float
        return plain
    }

    // Вспомогательная функция для парсинга double с учетом локали
    private fun parseLocalizedDouble(str: String): Double {
        return try {
            // Сначала пробуем стандартный парсинг
            str.toDouble()
        } catch (e: NumberFormatException) {
            // Если не получилось, возможно используется запятая
            val normalized = str.replace(',', '.')
            normalized.toDouble()
        }
    }
}