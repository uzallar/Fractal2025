package app.mouse

import androidx.compose.ui.geometry.Offset
import app.painting.convertation.Plain
import app.viewmodels.MainViewModel
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import kotlin.math.abs

class RightClickPanningTests {

    private lateinit var viewModel: MainViewModel

    @BeforeEach
    fun setup() {
        viewModel = MainViewModel()
        // Устанавливаем размеры для корректных расчетов
        viewModel.currentPlain.width = 800.0f
        viewModel.currentPlain.height = 600.0f
    }

    @Test
    fun `pan right moves view left`() { // панорамирование вправо смещает вид влево
        val initialXMin = viewModel.currentPlain.xMin
        val initialXMax = viewModel.currentPlain.xMax

        // Панорамируем вправо на 100 пикселей
        viewModel.handlePan(Offset(100f, 0f))

        // Проверяем, что оба края сместились влево
        assertTrue(viewModel.currentPlain.xMin < initialXMin, "xMin должен уменьшиться при панорамировании вправо")
        assertTrue(viewModel.currentPlain.xMax < initialXMax, "xMax должен уменьшиться при панорамировании вправо")

        // Проверяем, что смещение пропорционально
        val expectedChange = 100.0 / 800.0 * (initialXMax - initialXMin)
        val actualChange = abs(initialXMin - viewModel.currentPlain.xMin)
        // Используем дельту с типом Double
        assertEquals(expectedChange, actualChange, 0.0001, "Смещение должно быть пропорционально движению мыши")
    }

    @Test
    fun `pan down moves view up`() { // панорамирование вниз смещает вид вверх
        val initialYMin = viewModel.currentPlain.yMin
        val initialYMax = viewModel.currentPlain.yMax

        // Панорамируем вниз на 50 пикселей
        viewModel.handlePan(Offset(0f, 50f))

        // ВАЖНО: В математике фракталов ось Y направлена ВВЕРХ
        // При движении мыши ВНИЗ (положительное delta.y), вид должен сместиться ВНИЗ
        // то есть yMin и yMax должны УМЕНЬШИТЬСЯ

        // Правильная проверка:
        assertTrue(viewModel.currentPlain.yMin < initialYMin,
            "yMin должен УМЕНЬШИТЬСЯ при панорамировании вниз")
        assertTrue(viewModel.currentPlain.yMax < initialYMax,
            "yMax должен УМЕНЬШИТЬСЯ при панорамировании вниз")

        // Если вы хотите проверить логику преобразования:
        println("До панорамирования: yMin=$initialYMin, yMax=$initialYMax")
        println("После панорамирования: yMin=${viewModel.currentPlain.yMin}, yMax=${viewModel.currentPlain.yMax}")
        println("Изменение: dy=${viewModel.currentPlain.yMin - initialYMin}")
    }
    @Test
    fun `small movement triggers repaint`() { // небольшое движение вызывает перерисовку
        // Сохраняем состояние до панорамирования
        val initialXMin = viewModel.currentPlain.xMin

        // Маленькое движение (всего 1 пиксель)
        viewModel.handlePan(Offset(1f, 0f))

        // Проверяем, что координаты изменились
        assertNotEquals(initialXMin, viewModel.currentPlain.xMin,
            "Даже движение на 1 пиксель должно изменить координаты")
    }

    @Test
    fun `zero movement does nothing`() { // нулевое движение ничего не делает
        val initialXMin = viewModel.currentPlain.xMin
        val initialXMax = viewModel.currentPlain.xMax
        val initialYMin = viewModel.currentPlain.yMin
        val initialYMax = viewModel.currentPlain.yMax

        // Нулевое движение
        viewModel.handlePan(Offset.Zero)

        // Проверяем, что ничего не изменилось
        assertEquals(initialXMin, viewModel.currentPlain.xMin, "xMin не должен меняться при нулевом движении")
        assertEquals(initialXMax, viewModel.currentPlain.xMax, "xMax не должен меняться при нулевом движении")
        assertEquals(initialYMin, viewModel.currentPlain.yMin, "yMin не должен меняться при нулевом движении")
        assertEquals(initialYMax, viewModel.currentPlain.yMax, "yMax не должен меняться при нулевом движении")
    }

    @Test
    fun `panning preserves aspect ratio`() { // панорамирование сохраняет соотношение сторон
        val initialWidth = viewModel.currentPlain.xMax - viewModel.currentPlain.xMin
        val initialHeight = viewModel.currentPlain.yMax - viewModel.currentPlain.yMin
        val initialAspectRatio = initialWidth / initialHeight

        // Произвольное панорамирование
        viewModel.handlePan(Offset(100f, 75f))

        val newWidth = viewModel.currentPlain.xMax - viewModel.currentPlain.xMin
        val newHeight = viewModel.currentPlain.yMax - viewModel.currentPlain.yMin
        val newAspectRatio = newWidth / newHeight

        // Дельта должна быть Double
        assertEquals(initialAspectRatio, newAspectRatio, 0.0001,
            "Соотношение сторон должно сохраняться при панорамировании")
    }

    @Test
    fun `panning works after zoom`() { // панорамирование работает после зума
        // Сначала зумируем (увеличиваем масштаб в 10 раз)
        viewModel.currentPlain.xMin = -0.2
        viewModel.currentPlain.xMax = 0.0
        viewModel.currentPlain.yMin = -0.1
        viewModel.currentPlain.yMax = 0.1

        val initialXMin = viewModel.currentPlain.xMin
        val zoomedRange = viewModel.currentPlain.xMax - viewModel.currentPlain.xMin

        // Панорамируем при увеличении
        viewModel.handlePan(Offset(100f, 0f))

        // При зуме то же движение мыши дает большее смещение
        val expectedChange = 100.0 / 800.0 * zoomedRange
        val actualChange = abs(initialXMin - viewModel.currentPlain.xMin)

        // Дельта должна быть Double
        assertEquals(expectedChange, actualChange, 0.0001,
            "Панорамирование должно учитывать текущий масштаб")
    }

    @Test
    fun `context menu shows on right click`() { // контекстное меню появляется по правому клику
        val clickPosition = Offset(300f, 200f)

        viewModel.showContextMenuAt(clickPosition)

        assertTrue(viewModel.showContextMenu, "Контекстное меню должно отображаться после правого клика")
        assertTrue(viewModel.contextMenuCoordinates.isNotEmpty(),
            "Координаты должны вычисляться для контекстного меню")
        assertTrue(viewModel.contextMenuCoordinates.contains("x:"),
            "Строка координат должна содержать x")
        assertTrue(viewModel.contextMenuCoordinates.contains("y:"),
            "Строка координат должна содержать y")
    }

}
