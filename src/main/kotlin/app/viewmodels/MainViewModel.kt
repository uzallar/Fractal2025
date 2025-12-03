package app.viewmodels

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import app.painting.FractalPainter
import app.painting.convertation.Converter
import app.painting.convertation.Plain
import app.fractal.FractalFunctions
import app.painting.ColorSchemes
import app.fractal.IterationsCalculator
import app.mouse.ClipboardService

class MainViewModel {
    var fractalImage: ImageBitmap = ImageBitmap(0, 0)

    // Для выделения области
    var selectionStart by mutableStateOf(Offset.Zero)
    var selectionEnd by mutableStateOf(Offset.Zero)
    var isSelecting by mutableStateOf(false)

    var currentFractalName by mutableStateOf("Мандельброт")
    var currentColorSchemeName by mutableStateOf("Стандартная")

    private val plain = Plain(-2.0, 1.0, -1.0, 1.0)

    private var fractalPainter by mutableStateOf(
        FractalPainter(
            plain,
            FractalFunctions.mandelbrot,
            ColorSchemes.standard,
            { IterationsCalculator.getMaxIterations(plain) }
        )
    )

    private var mustRepaint by mutableStateOf(true)
    val currentPlain: Plain get() = plain

    // Вычисляемое свойство для прямоугольника выделения
    val selectionRect: Pair<Offset, Size>
        get() {
            if (!isSelecting) return Pair(Offset.Zero, Size.Zero)

            val x = minOf(selectionStart.x, selectionEnd.x)
            val y = minOf(selectionStart.y, selectionEnd.y)
            val width = kotlin.math.abs(selectionEnd.x - selectionStart.x)
            val height = kotlin.math.abs(selectionEnd.y - selectionStart.y)

            return Pair(Offset(x, y), Size(width, height))
        }

    fun paint(scope: DrawScope) = runBlocking {
        plain.width = scope.size.width
        plain.height = scope.size.height
        if (mustRepaint
            || fractalImage.width != plain.width.toInt()
            || fractalImage.height != plain.height.toInt()
        ) {
            launch(Dispatchers.Default) {
                fractalPainter.paint(scope)
            }
        } else {
            scope.drawImage(fractalImage)
        }
        mustRepaint = false
    }

    fun onImageUpdate(image: ImageBitmap) {
        fractalImage = image
    }

    // Методы для выделения областей
    fun onStartSelecting(offset: Offset) {
        println("DEBUG: Start selecting at $offset")
        selectionStart = offset
        selectionEnd = offset
        isSelecting = true
    }

    fun onStopSelecting() {
        println("DEBUG: Stop selecting")

        if (isSelecting) {
            val (selectionOffset, selectionSize) = selectionRect

            if (selectionSize.width > 10f && selectionSize.height > 10f) {
                // Преобразуем координаты выделения в координаты фрактала
                val x1 = Converter.xScr2Crt(selectionOffset.x, plain)
                val y1 = Converter.yScr2Crt(selectionOffset.y + selectionSize.height, plain)
                val x2 = Converter.xScr2Crt(selectionOffset.x + selectionSize.width, plain)
                val y2 = Converter.yScr2Crt(selectionOffset.y, plain)

                // Убедимся, что координаты корректны
                val xMin = minOf(x1, x2)
                val xMax = maxOf(x1, x2)
                val yMin = minOf(y1, y2)
                val yMax = maxOf(y1, y2)

                println("DEBUG: Zooming to [$xMin, $yMin] - [$xMax, $yMax]")

                // Применяем зум к выделенной области
                plain.xMin = xMin
                plain.xMax = xMax
                plain.yMin = yMin
                plain.yMax = yMax

                mustRepaint = true
            } else {
                println("DEBUG: Selection too small, ignoring")
            }
        }

        // Сбрасываем выделение
        isSelecting = false
    }

    fun onSelecting(offset: Offset) {
        if (isSelecting) {
            selectionEnd = offset
            println("DEBUG: Selecting, end = $offset")
        }
    }

    fun setMandelbrot() {
        fractalPainter = fractalPainter.withFractal(FractalFunctions.mandelbrot)
        currentFractalName = "Мандельброт"
        mustRepaint = true
    }

    fun setJulia() {
        fractalPainter = fractalPainter.withFractal(FractalFunctions.julia)
        currentFractalName = "Жюлиа"
        mustRepaint = true
    }

    fun setBurningShip() {
        fractalPainter = fractalPainter.withFractal(FractalFunctions.burningShip)
        currentFractalName = "Горящий корабль"
        mustRepaint = true
    }

    fun setTricorn() {
        fractalPainter = fractalPainter.withFractal(FractalFunctions.tricorn)
        currentFractalName = "Трикорн"
        mustRepaint = true
    }

    fun setStandardColors() {
        fractalPainter = fractalPainter.withColorScheme(ColorSchemes.standard)
        currentColorSchemeName = "Стандартная"
        mustRepaint = true
    }

    fun setFireColors() {
        fractalPainter = fractalPainter.withColorScheme(ColorSchemes.fire)
        currentColorSchemeName = "Огненная"
        mustRepaint = true
    }

    fun setRainbowColors() {
        fractalPainter = fractalPainter.withColorScheme(ColorSchemes.rainbow)
        currentColorSchemeName = "Радужная"
        mustRepaint = true
    }

    fun setCosmicColors() {
        fractalPainter = fractalPainter.withColorScheme(ColorSchemes.cosmic)
        currentColorSchemeName = "Космическая"
        mustRepaint = true
    }

    // Для контекстного меню
    var showContextMenu by mutableStateOf(false)
    var contextMenuPosition by mutableStateOf(Offset.Zero)
    var contextMenuCoordinates by mutableStateOf("")

    // Метод для панорамирования
    fun handlePan(delta: Offset) {
        // Вычисляем смещение в координатах фрактала
        val dx = delta.x / plain.width
        val dy = delta.y / plain.height

        val xRange = plain.xMax - plain.xMin
        val yRange = plain.yMax - plain.yMin

        // Сдвигаем область просмотра
        plain.xMin -= dx * xRange
        plain.xMax -= dx * xRange
        plain.yMin -= dy * yRange
        plain.yMax -= dy * yRange

        // Активируем перерисовку
        mustRepaint = true
    }

    // Для контекстного меню
    fun showContextMenuAt(position: Offset) {
        contextMenuPosition = position
        contextMenuCoordinates = ClipboardService.getCoordinatesString(position, plain)
        showContextMenu = true
    }

    fun hideContextMenu() {
        showContextMenu = false
    }

    fun copyCoordinatesToClipboard() {
        ClipboardService.copyFractalCoordinates(contextMenuPosition, plain)
    }

    // Метод для сброса зума (может понадобиться для кнопки "Сброс")
    fun resetZoom() {
        plain.xMin = -2.0
        plain.xMax = 1.0
        plain.yMin = -1.0
        plain.yMax = 1.0
        mustRepaint = true
    }
}