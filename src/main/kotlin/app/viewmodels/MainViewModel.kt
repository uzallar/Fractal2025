package app.viewmodels
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import app.painting.FractalPainter
import app.painting.convertation.Converter
import app.painting.convertation.Plain
import app.fractal.FractalFunctions
import app.painting.ColorSchemes
import app.fractal.IterationsCalculator

class MainViewModel {
    var fractalImage: ImageBitmap = ImageBitmap(0, 0)
    var selectionOffset by mutableStateOf(Offset(0f, 0f))
    var selectionSize by mutableStateOf(Size(0f, 0f))


    var currentFractalName by mutableStateOf("Мандельброт")
    var currentColorSchemeName by mutableStateOf("Стандартная")

    private val plain = Plain(-2.0, 1.0, -1.0, 1.0)


    private var fractalPainter by mutableStateOf(
        FractalPainter(
            plain,
            FractalFunctions.mandelbrot,    // Используем из FractalFunctions
            ColorSchemes.standard,          // Используем из ColorSchemes
            { IterationsCalculator.getMaxIterations(plain) }
        )
    )

    private var mustRepaint by mutableStateOf(true)
    val currentPlain: Plain get() = plain

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

    fun onStartSelecting(offset: Offset) {
        this.selectionOffset = offset
    }

    fun onStopSelecting() {
        val xMin = Converter.xScr2Crt(selectionOffset.x, plain)
        val yMin = Converter.yScr2Crt(selectionOffset.y + selectionSize.height, plain)
        val xMax = Converter.xScr2Crt(selectionOffset.x + selectionSize.width, plain)
        val yMax = Converter.yScr2Crt(selectionOffset.y, plain)
        plain.xMin = xMin
        plain.yMin = yMin
        plain.xMax = xMax
        plain.yMax = yMax
        selectionSize = Size(0f, 0f)
        mustRepaint = true
    }

    fun onSelecting(offset: Offset) {
        selectionSize = Size(selectionSize.width + offset.x, selectionSize.height + offset.y)
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
    // ============ ДОБАВЛЕНО ДЛЯ ПАНОРАМИРОВАНИЯ ============

    // Для контекстного меню
    var showContextMenu by mutableStateOf(false)
    var contextMenuPosition by mutableStateOf(Offset.Zero)

    // Метод для панорамирования
    fun handlePan(delta: Offset) {
        // Вычисляем смещение в координатах фрактала
        val dx = delta.x / currentPlain.width
        val dy = delta.y / currentPlain.height

        val xRange = currentPlain.xMax - currentPlain.xMin
        val yRange = currentPlain.yMax - currentPlain.yMin

        // Сдвигаем область просмотра
        currentPlain.xMin -= dx * xRange
        currentPlain.xMax -= dx * xRange
        currentPlain.yMin -= dy * yRange
        currentPlain.yMax -= dy * yRange

        // Активируем перерисовку через безопасный метод
        triggerRepaintSafely()
    }

    // Безопасный способ вызвать перерисовку
    private fun triggerRepaintSafely() {
        // Создаем микроскопическое выделение в углу, которое не мешает
        val originalOffset = selectionOffset
        val originalSize = selectionSize

        // Временное изменение в незаметном месте
        selectionOffset = Offset(-100f, -100f)
        selectionSize = Size(0.1f, 0.1f)

        // Сразу возвращаем обратно
        selectionOffset = originalOffset
        selectionSize = originalSize
    }
    // ============ КОНЕЦ ДОБАВЛЕНИЙ ============
}
