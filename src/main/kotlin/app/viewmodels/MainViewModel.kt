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
    var selectionStart by mutableStateOf(Offset.Zero)
    var selectionEnd by mutableStateOf(Offset.Zero)
    var isSelecting by mutableStateOf(false)
    var currentFractalName by mutableStateOf("Мандельброт")
    var currentColorSchemeName by mutableStateOf("Стандартная")


    private val initialXMin = -2.0
    private val initialXMax = 1.0
    private val initialYMin = -1.0
    private val initialYMax = 1.0


    private val initialFractalAspect = (initialXMax - initialXMin) / (initialYMax - initialYMin)


    private var lastWindowWidth: Float = 0f
    private var lastWindowHeight: Float = 0f

    private val plain = Plain(initialXMin, initialXMax, initialYMin, initialYMax)

    var zoomLevel by mutableStateOf(1.0)
    var zoomText by mutableStateOf("1x")

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
    val selectionRect: Pair<Offset, Size>
        get() {
            if (!isSelecting) return Pair(Offset.Zero, Size.Zero)

            val x = minOf(selectionStart.x, selectionEnd.x)
            val y = minOf(selectionStart.y, selectionEnd.y)
            val width = kotlin.math.abs(selectionEnd.x - selectionStart.x)
            val height = kotlin.math.abs(selectionEnd.y - selectionStart.y)

            return Pair(Offset(x, y), Size(width, height))
        }

    // Функция для коррекции пропорций при изменении размера окна
    private fun adjustFractalForWindowSize(newWidth: Float, newHeight: Float) {
        val newAspect = newWidth / newHeight


        if (newAspect != (lastWindowWidth / lastWindowHeight)) {
            val currentWidth = plain.xMax - plain.xMin
            val currentHeight = plain.yMax - plain.yMin
            val currentCenterX = (plain.xMin + plain.xMax) / 2
            val currentCenterY = (plain.yMin + plain.yMax) / 2

            if (newAspect > initialFractalAspect) {

                val newWidth = currentHeight * newAspect
                plain.xMin = currentCenterX - newWidth / 2
                plain.xMax = currentCenterX + newWidth / 2
            } else {

                val newHeight = currentWidth / newAspect
                plain.yMin = currentCenterY - newHeight / 2
                plain.yMax = currentCenterY + newHeight / 2
            }


            updateZoomLevel()
            mustRepaint = true
        }


        lastWindowWidth = newWidth
        lastWindowHeight = newHeight
    }

    private fun updateZoomLevel() {
        val initialWidth = initialXMax - initialXMin
        val currentWidth = plain.xMax - plain.xMin

        zoomLevel = initialWidth / currentWidth

        zoomText = when {
            zoomLevel >= 1_000_000 -> String.format("%.1fMx", zoomLevel / 1_000_000)
            zoomLevel >= 1_000 -> String.format("%.1fKx", zoomLevel / 1_000)
            zoomLevel >= 100 -> String.format("%.0fx", zoomLevel)
            zoomLevel >= 10 -> String.format("%.1fx", zoomLevel)
            zoomLevel >= 1 -> String.format("%.2fx", zoomLevel)
            else -> String.format("%.4fx", zoomLevel)
        }

        println("DEBUG: Zoom level updated: $zoomLevel ($zoomText)")
    }


    fun paint(scope: DrawScope) = runBlocking {
        plain.width = scope.size.width
        plain.height = scope.size.height

        // Корректируем пропорции фрактала при изменении размера окна
        adjustFractalForWindowSize(scope.size.width, scope.size.height)

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
                val x1 = Converter.xScr2Crt(selectionOffset.x, plain)
                val y1 = Converter.yScr2Crt(selectionOffset.y + selectionSize.height, plain)
                val x2 = Converter.xScr2Crt(selectionOffset.x + selectionSize.width, plain)
                val y2 = Converter.yScr2Crt(selectionOffset.y, plain)

                var xMin = minOf(x1, x2)
                var xMax = maxOf(x1, x2)
                var yMin = minOf(y1, y2)
                var yMax = maxOf(y1, y2)

                val selectionWidth = xMax - xMin
                val selectionHeight = yMax - yMin
                val selectionAspect = selectionWidth / selectionHeight

                val screenAspect = plain.width / plain.height
                val targetAspect = screenAspect.toDouble()

                if (selectionAspect > targetAspect) {
                    val centerY = (yMin + yMax) / 2
                    val newHeight = selectionWidth / targetAspect
                    yMin = centerY - newHeight / 2
                    yMax = centerY + newHeight / 2
                } else {
                    val centerX = (xMin + xMax) / 2
                    val newWidth = selectionHeight * targetAspect
                    xMin = centerX - newWidth / 2
                    xMax = centerX + newWidth / 2
                }

                println("DEBUG: Zooming to [$xMin, $yMin] - [$xMax, $yMax] (aspect corrected)")

                plain.xMin = xMin
                plain.xMax = xMax
                plain.yMin = yMin
                plain.yMax = yMax

                updateZoomLevel()
                mustRepaint = true
            } else {
                println("DEBUG: Selection too small, ignoring")
            }
        }
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

    var showContextMenu by mutableStateOf(false)
    var contextMenuPosition by mutableStateOf(Offset.Zero)
    var contextMenuCoordinates by mutableStateOf("")

    fun handlePan(delta: Offset) {
        val dx = delta.x / plain.width
        val dy = delta.y / plain.height
        val xRange = plain.xMax - plain.xMin
        val yRange = plain.yMax - plain.yMin
        plain.xMin -= dx * xRange
        plain.xMax -= dx * xRange
        plain.yMin -= dy * yRange
        plain.yMax -= dy * yRange

        updateZoomLevel()
        mustRepaint = true
    }

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

    fun resetZoom() {

        val currentAspect = plain.width / plain.height

        if (currentAspect > initialFractalAspect) {

            val width = (initialYMax - initialYMin) * currentAspect
            plain.xMin = -width / 2
            plain.xMax = width / 2
            plain.yMin = initialYMin
            plain.yMax = initialYMax
        } else {

            val height = (initialXMax - initialXMin) / currentAspect
            plain.xMin = initialXMin
            plain.xMax = initialXMax
            plain.yMin = -height / 2
            plain.yMax = height / 2
        }


        lastWindowWidth = plain.width
        lastWindowHeight = plain.height

        zoomLevel = 1.0
        zoomText = "1x"
        mustRepaint = true
        println("DEBUG: Zoom reset to 1x (aspect adjusted)")
    }
}
