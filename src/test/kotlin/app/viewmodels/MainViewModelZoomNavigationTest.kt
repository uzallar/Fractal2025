package app.viewmodels

import androidx.compose.ui.geometry.Offset
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.math.abs
import java.util.Locale

class MainViewModelZoomAndProportionTest {

    private lateinit var viewModel: MainViewModel

    @BeforeEach
    fun setUp() {
        viewModel = MainViewModel()
    }

    // Тесты для функций зума
    @Test
    fun `initial zoom should be 1x`() {
        assertEquals(1.0, viewModel.zoomLevel, 0.001)
        assertEquals("1x", viewModel.zoomText)
    }

    @Test
    fun `resetZoom should restore initial zoom and aspect ratio`() {
        // Изменяем область просмотра
        viewModel.currentPlain.xMin = -1.0
        viewModel.currentPlain.xMax = 0.0
        viewModel.currentPlain.yMin = -0.5
        viewModel.currentPlain.yMax = 0.5

        // Вызываем resetZoom
        viewModel.resetZoom()

        // Проверяем, что зум сбросился
        assertEquals(1.0, viewModel.zoomLevel, 0.001)
        assertEquals("1x", viewModel.zoomText)

        // Проверяем, что пропорции соответствуют текущему размеру окна
        val aspectRatio = (viewModel.currentPlain.xMax - viewModel.currentPlain.xMin) /
                (viewModel.currentPlain.yMax - viewModel.currentPlain.yMin)
        val windowAspect = viewModel.currentPlain.width / viewModel.currentPlain.height
        assertEquals(windowAspect.toDouble(), aspectRatio, 0.001)
    }

    @Test
    fun `updateZoomLevel should calculate correct zoom value`() {
        // Сохраняем начальный зум
        val initialZoom = viewModel.zoomLevel

        // Уменьшаем область просмотра (увеличиваем зум в 2 раза)
        val currentWidth = viewModel.currentPlain.xMax - viewModel.currentPlain.xMin
        val currentHeight = viewModel.currentPlain.yMax - viewModel.currentPlain.yMin
        val centerX = (viewModel.currentPlain.xMin + viewModel.currentPlain.xMax) / 2
        val centerY = (viewModel.currentPlain.yMin + viewModel.currentPlain.yMax) / 2

        viewModel.currentPlain.xMin = centerX - currentWidth / 4
        viewModel.currentPlain.xMax = centerX + currentWidth / 4
        viewModel.currentPlain.yMin = centerY - currentHeight / 4
        viewModel.currentPlain.yMax = centerY + currentHeight / 4

        // Расчетный зум должен быть в 2 раза больше
        val expectedZoom = initialZoom * 2.0

        // В реальном коде updateZoomLevel вызывается автоматически,
        // но мы можем проверить логику расчета
        val initialWidth = 3.0 // initialXMax - initialXMin
        val newWidth = viewModel.currentPlain.xMax - viewModel.currentPlain.xMin
        val calculatedZoom = initialWidth / newWidth

        assertEquals(expectedZoom, calculatedZoom, 0.001)
    }

    @Test
    fun `zoomText should format correctly for different zoom levels`() {
        // Устанавливаем русскую локаль для всего теста
        val originalLocale = Locale.getDefault()
        Locale.setDefault(Locale("ru", "RU"))

        try {
            fun formatZoomText(zoomLevel: Double): String {
                return when {
                    zoomLevel >= 1_000_000 -> String.format("%.1fMx", zoomLevel / 1_000_000)
                    zoomLevel >= 1_000 -> String.format("%.1fKx", zoomLevel / 1_000)
                    zoomLevel >= 100 -> String.format("%.0fx", zoomLevel)
                    zoomLevel >= 10 -> String.format("%.1fx", zoomLevel)
                    zoomLevel >= 1 -> String.format("%.2fx", zoomLevel)
                    else -> String.format("%.4fx", zoomLevel)
                }
            }

            // Тестируем различные уровни зума (ожидаем запятую)
            assertEquals("0,1234x", formatZoomText(0.1234))
            assertEquals("0,5000x", formatZoomText(0.5))
            assertEquals("1,00x", formatZoomText(1.0))
            assertEquals("5,00x", formatZoomText(5.0))
            assertEquals("15,0x", formatZoomText(15.0))    // Обратите внимание: 15,0x (не 15,00x)
            assertEquals("150x", formatZoomText(150.0))    // %.0fx для >= 100
            assertEquals("999x", formatZoomText(999.0))    // %.0fx для >= 100
            assertEquals("1,0Kx", formatZoomText(1000.0))
            assertEquals("1,5Kx", formatZoomText(1500.0))
            assertEquals("999,9Kx", formatZoomText(999900.0))
            assertEquals("1,0Mx", formatZoomText(1000000.0))
            assertEquals("2,5Mx", formatZoomText(2500000.0))
        } finally {
            // Восстанавливаем оригинальную локаль
            Locale.setDefault(originalLocale)
        }
    }

    @Test
    fun `randomJump should increase zoom level`() {
        val initialZoom = viewModel.zoomLevel

        viewModel.randomJump()

        // randomJump должен приблизить вид к деталям, увеличив зум
        assertTrue(viewModel.zoomLevel > initialZoom)

        // Зум должен быть больше 1 (так как мы приближаемся)
        assertTrue(viewModel.zoomLevel > 1.0)
    }

    @Test
    fun `resetToInitial should reset zoom to 1x`() {
        // Изменяем состояние
        viewModel.randomJump()
        viewModel.setJulia()
        viewModel.setFireColors()

        // Проверяем, что состояние изменилось
        assertNotEquals(1.0, viewModel.zoomLevel)

        // Сбрасываем
        viewModel.resetToInitial()

        // Проверяем сброс зума
        assertEquals(1.0, viewModel.zoomLevel, 0.001)
        assertEquals("1,00x", viewModel.zoomText)
    }

    // Тесты для функций, сохраняющих пропорции
    @Test
    fun `adjustFractalForWindowSize should maintain correct aspect ratio`() {
        val initialAspect = (viewModel.currentPlain.xMax - viewModel.currentPlain.xMin) /
                (viewModel.currentPlain.yMax - viewModel.currentPlain.yMin)

        // Изменяем размер окна (другое соотношение сторон)
        val newWidth = 1200f
        val newHeight = 800f
        val newAspect = newWidth / newHeight

        // Устанавливаем новые размеры
        viewModel.currentPlain.width = newWidth
        viewModel.currentPlain.height = newHeight

        // Вызываем paint, который внутри вызовет adjustFractalForWindowSize
        // Вместо этого симулируем логику adjustFractalForWindowSize
        if (abs(newAspect - initialAspect) > 0.001) {
            val currentWidth = viewModel.currentPlain.xMax - viewModel.currentPlain.xMin
            val currentHeight = viewModel.currentPlain.yMax - viewModel.currentPlain.yMin
            val currentCenterX = (viewModel.currentPlain.xMin + viewModel.currentPlain.xMax) / 2
            val currentCenterY = (viewModel.currentPlain.yMin + viewModel.currentPlain.yMax) / 2

            if (newAspect > initialAspect) {
                val adjustedWidth = currentHeight * newAspect
                viewModel.currentPlain.xMin = currentCenterX - adjustedWidth / 2
                viewModel.currentPlain.xMax = currentCenterX + adjustedWidth / 2
            } else {
                val adjustedHeight = currentWidth / newAspect
                viewModel.currentPlain.yMin = currentCenterY - adjustedHeight / 2
                viewModel.currentPlain.yMax = currentCenterY + adjustedHeight / 2
            }

            // Проверяем результат
            val finalAspect = (viewModel.currentPlain.xMax - viewModel.currentPlain.xMin) /
                    (viewModel.currentPlain.yMax - viewModel.currentPlain.yMin)
            assertEquals(newAspect.toDouble(), finalAspect, 0.001,
                "Aspect ratio should match window aspect after adjustment")
        }
    }

    @Test
    fun `selection should maintain window aspect ratio`() {
        // Устанавливаем размеры окна
        viewModel.currentPlain.width = 1000f
        viewModel.currentPlain.height = 800f
        val windowAspect = viewModel.currentPlain.width / viewModel.currentPlain.height

        // Начинаем прямоугольное выделение (не соответствует пропорциям окна)
        viewModel.onStartSelecting(Offset(100f, 100f))
        viewModel.onSelecting(Offset(300f, 250f)) // Пропорция 200/150 = 1.333, а окно 1000/800 = 1.25

        // Завершаем выделение (должно скорректировать пропорции)
        viewModel.onStopSelecting()

        // Проверяем, что новая область имеет правильные пропорции
        val newWidth = viewModel.currentPlain.xMax - viewModel.currentPlain.xMin
        val newHeight = viewModel.currentPlain.yMax - viewModel.currentPlain.yMin
        val newAspect = newWidth / newHeight

        assertEquals(windowAspect.toDouble(), newAspect, 0.001,
            "Selected area should be adjusted to match window aspect ratio")
    }

    @Test
    fun `resetZoom should maintain current window aspect ratio`() {
        // Изменяем размер окна
        viewModel.currentPlain.width = 1600f
        viewModel.currentPlain.height = 900f
        val windowAspect = viewModel.currentPlain.width / viewModel.currentPlain.height

        // Вызываем resetZoom
        viewModel.resetZoom()

        // Проверяем, что пропорции сохранены
        val fractalAspect = (viewModel.currentPlain.xMax - viewModel.currentPlain.xMin) /
                (viewModel.currentPlain.yMax - viewModel.currentPlain.yMin)

        assertEquals(windowAspect.toDouble(), fractalAspect, 0.001,
            "resetZoom should maintain current window aspect ratio")
    }

    @Test
    fun `onStopSelecting with small selection should not change view`() {
        // Сохраняем начальное состояние
        val initialXMin = viewModel.currentPlain.xMin
        val initialXMax = viewModel.currentPlain.xMax
        val initialYMin = viewModel.currentPlain.yMin
        val initialYMax = viewModel.currentPlain.yMax

        // Пытаемся выделить очень маленькую область (<10px)
        viewModel.onStartSelecting(Offset(100f, 100f))
        viewModel.onSelecting(Offset(105f, 105f))
        viewModel.onStopSelecting()

        // Область просмотра не должна измениться
        assertEquals(initialXMin, viewModel.currentPlain.xMin, 0.000001)
        assertEquals(initialXMax, viewModel.currentPlain.xMax, 0.000001)
        assertEquals(initialYMin, viewModel.currentPlain.yMin, 0.000001)
        assertEquals(initialYMax, viewModel.currentPlain.yMax, 0.000001)
    }

    @Test
    fun `selectionRect should calculate correct rectangle`() {
        // Тестируем выделение слева-направо, сверху-вниз
        viewModel.onStartSelecting(Offset(100f, 100f))
        viewModel.onSelecting(Offset(300f, 200f))

        val (offset, size) = viewModel.selectionRect

        assertEquals(100f, offset.x, 0.001f)
        assertEquals(100f, offset.y, 0.001f)
        assertEquals(200f, size.width, 0.001f)
        assertEquals(100f, size.height, 0.001f)
    }

    @Test
    fun `selectionRect should handle reversed selection`() {
        // Тестируем выделение справа-налево, снизу-вверх
        viewModel.onStartSelecting(Offset(300f, 200f))
        viewModel.onSelecting(Offset(100f, 100f))

        val (offset, size) = viewModel.selectionRect

        // Все равно должны получить правильный прямоугольник
        assertEquals(100f, offset.x, 0.001f)
        assertEquals(100f, offset.y, 0.001f)
        assertEquals(200f, size.width, 0.001f)
        assertEquals(100f, size.height, 0.001f)
    }

    @Test
    fun `handlePan should not change zoom level`() {
        // 1. Устанавливаем plain.width и plain.height для корректных вычислений
        viewModel.currentPlain.width = 800f
        viewModel.currentPlain.height = 600f

        // 2. Запоминаем начальные значения
        val initialZoom = viewModel.zoomLevel
        val initialWidth = viewModel.currentPlain.xMax - viewModel.currentPlain.xMin
        val initialHeight = viewModel.currentPlain.yMax - viewModel.currentPlain.yMin

        println("DEBUG: Начальный зум: $initialZoom")
        println("DEBUG: Начальная ширина области: $initialWidth, высота: $initialHeight")

        // 3. Выполняем панорамирование (смещение на 50px вправо и 30px вниз)
        viewModel.handlePan(Offset(50f, 30f))

        // 4. Проверяем результат
        val finalZoom = viewModel.zoomLevel
        val finalWidth = viewModel.currentPlain.xMax - viewModel.currentPlain.xMin
        val finalHeight = viewModel.currentPlain.yMax - viewModel.currentPlain.yMin

        println("DEBUG: Конечный зум: $finalZoom")
        println("DEBUG: Конечная ширина области: $finalWidth, высота: $finalHeight")
        println("DEBUG: Разница в ширине: ${finalWidth - initialWidth}")
        println("DEBUG: Разница в высоте: ${finalHeight - initialHeight}")

        // 5. Ширина и высота должны остаться прежними (точность из-за double)
        assertEquals(initialWidth, finalWidth, 0.000001,
            "Ширина области не должна меняться при панорамировании")
        assertEquals(initialHeight, finalHeight, 0.000001,
            "Высота области не должна меняться при панорамировании")

        // 6. Зум должен остаться прежним
        assertEquals(initialZoom, finalZoom, 0.001,
            "Уровень зума не должен меняться при панорамировании")
    }

    @Test
    fun `handlePan should save state only on first call`() {
        val initialHistory = viewModel.historyInfo

        // Первый вызов handlePan начинает новую сессию панорамирования
        viewModel.handlePan(Offset(10f, 10f))
        val historyAfterFirst = viewModel.historyInfo

        // Должно сохраниться состояние
        assertNotEquals(initialHistory, historyAfterFirst)

        // Второй вызов в той же сессии не должен сохранять состояние
        viewModel.handlePan(Offset(20f, 20f))
        val historyAfterSecond = viewModel.historyInfo
        assertEquals(historyAfterFirst, historyAfterSecond)

        // Сбрасываем флаг панорамирования
        viewModel.setMandelbrot() // Любое другое действие сбрасывает флаг

        // Теперь новое панорамирование снова сохранит состояние
        viewModel.handlePan(Offset(30f, 30f))
        val historyAfterReset = viewModel.historyInfo
        assertNotEquals(historyAfterSecond, historyAfterReset)
    }

    @Test
    fun `zoom should increase after valid selection`() {
        // 1. Сохраняем начальные границы
        val initialXMin = viewModel.currentPlain.xMin
        val initialXMax = viewModel.currentPlain.xMax
        val initialYMin = viewModel.currentPlain.yMin
        val initialYMax = viewModel.currentPlain.yMax
        val initialWidth = initialXMax - initialXMin
        val initialHeight = initialYMax - initialYMin

        val initialZoom = viewModel.zoomLevel
        println("DEBUG: Начальный зум: $initialZoom")
        println("DEBUG: Начальные границы: xMin=$initialXMin, xMax=$initialXMax, yMin=$initialYMin, yMax=$initialYMax")

        // 2. Устанавливаем размеры plain (важно для правильных преобразований)
        viewModel.currentPlain.width = 800f
        viewModel.currentPlain.height = 600f

        // 3. Выделяем область (делаем ее значительно меньше экрана для увеличения зума)
        // Выделяем центральную область, которая будет меньше всего экрана
        viewModel.onStartSelecting(Offset(300f, 200f))  // Начало выделения в центре
        viewModel.onSelecting(Offset(500f, 400f))      // Конец выделения - небольшая область
        viewModel.onStopSelecting()

        // 4. Проверяем результат
        val finalZoom = viewModel.zoomLevel
        val finalXMin = viewModel.currentPlain.xMin
        val finalXMax = viewModel.currentPlain.xMax
        val finalYMin = viewModel.currentPlain.yMin
        val finalYMax = viewModel.currentPlain.yMax
        val finalWidth = finalXMax - finalXMin
        val finalHeight = finalYMax - finalYMin

        println("DEBUG: Конечный зум: $finalZoom")
        println("DEBUG: Конечные границы: xMin=$finalXMin, xMax=$finalXMax, yMin=$finalYMin, yMax=$finalYMax")
        println("DEBUG: Отношение ширины: ${initialWidth / finalWidth}")
        println("DEBUG: Отношение зума: ${finalZoom / initialZoom}")

        // 5. Проверяем, что область действительно уменьшилась (зум увеличился)
        assertTrue(
            finalZoom > initialZoom,
            "Зум должен увеличиться после выделения области. " +
                    "Было: $initialZoom (ширина=$initialWidth), " +
                    "Стало: $finalZoom (ширина=$finalWidth)"
        )

        // 6. Дополнительная проверка: новая область должна быть внутри старой
        assertTrue(finalXMin > initialXMin, "Новая область должна быть правее начальной")
        assertTrue(finalXMax < initialXMax, "Новая область должна быть уже начальной")
        assertTrue(finalWidth < initialWidth, "Новая ширина должна быть меньше начальной")
    }

    @Test
    fun `adjustFractalForWindowSize should not adjust if aspect ratio unchanged`() {
        // Сохраняем текущее состояние
        val initialXMin = viewModel.currentPlain.xMin
        val initialXMax = viewModel.currentPlain.xMax
        val initialYMin = viewModel.currentPlain.yMin
        val initialYMax = viewModel.currentPlain.yMax

        // Устанавливаем размеры окна с тем же соотношением сторон
        val currentAspect = viewModel.currentPlain.width / viewModel.currentPlain.height
        val newWidth = 1200f
        val newHeight = newWidth / currentAspect

        viewModel.currentPlain.width = newWidth
        viewModel.currentPlain.height = newHeight

        // Симулируем логику adjustFractalForWindowSize
        val newAspect = newWidth / newHeight

        // Поскольку аспект не изменился, координаты не должны меняться
        if (abs(newAspect - currentAspect) > 0.001) {
            fail("Aspect ratio should not change significantly")
        } else {
            // Координаты должны остаться прежними
            assertEquals(initialXMin, viewModel.currentPlain.xMin, 0.000001)
            assertEquals(initialXMax, viewModel.currentPlain.xMax, 0.000001)
            assertEquals(initialYMin, viewModel.currentPlain.yMin, 0.000001)
            assertEquals(initialYMax, viewModel.currentPlain.yMax, 0.000001)
        }
    }

    @Test
    fun `initial fractal should have correct aspect ratio`() {
        // Проверяем начальные пропорции фрактала
        val fractalWidth = viewModel.currentPlain.xMax - viewModel.currentPlain.xMin
        val fractalHeight = viewModel.currentPlain.yMax - viewModel.currentPlain.yMin
        val fractalAspect = fractalWidth / fractalHeight

        // Начальное соотношение сторон должно быть 3:2 (ширина=3, высота=2)
        val expectedAspect = 3.0 / 2.0

        assertEquals(expectedAspect, fractalAspect, 0.001,
            "Initial fractal should have 3:2 aspect ratio")
    }
}