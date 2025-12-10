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
import app.history.UndoManager
import app.tour.FractalTour
import app.tour.TourFrame
import app.utils.ExporterJPG
import app.utils.FractalSaving
import app.utils.SoundPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import java.awt.FileDialog
import java.awt.Frame
import javax.swing.JOptionPane


class MainViewModel {
    var fractalImage by mutableStateOf(ImageBitmap(0, 0))
    var selectionStart by mutableStateOf(Offset.Zero)
    var selectionEnd by mutableStateOf(Offset.Zero)
    var isSelecting by mutableStateOf(false)
    var currentFractalName by mutableStateOf("Мандельброт")
    var currentColorSchemeName by mutableStateOf("Стандартная")

    private val undoManager = UndoManager(maxSteps = 100)
    var canUndo by mutableStateOf(false)
    var canRedo by mutableStateOf(false)
    var historyInfo by mutableStateOf("")

    var detailedHistory by mutableStateOf("")
    private var isPanning = false
    var showContextMenu by mutableStateOf(false)
    var contextMenuPosition by mutableStateOf(Offset.Zero)
    var contextMenuCoordinates by mutableStateOf("")

    private var iterationsOffset by mutableStateOf(0)

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


    var fractalPainter by mutableStateOf(
        FractalPainter(
            plain,
            FractalFunctions.mandelbrot,
            ColorSchemes.standard,
            { getAdjustedMaxIterations() }
        )
    )

    private var mustRepaint by mutableStateOf(true)


    val maxIterations: Int
        get() = getAdjustedMaxIterations()

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

    var currentTour: FractalTour? = null
    private var tourJob: Job? = null
    var isTourRunning by mutableStateOf(false)
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    var isRecordingTour by mutableStateOf(false)
    var currentTourFrames by mutableStateOf<List<TourFrame>>(emptyList())
    var tourName by mutableStateOf("Моя экскурсия")


    fun startTour(tour: FractalTour) {
        if (isTourRunning) return
        isTourRunning = true
        tourJob = viewModelScope.launch {
            try {
                while (true) {
                    for (i in 0 until tour.frames.size - 1) {
                        animateBetween(tour.frames[i], tour.frames[i + 1])
                        if (!isTourRunning) break
                    }
                    if (!tour.loop) break
                }
            } finally {
                isTourRunning = false
            }
        }
    }

    fun startTourRecording() {
        isRecordingTour = true
        currentTourFrames = emptyList()
    }

    fun stopTourRecording() {
        isRecordingTour = false
    }

    fun saveAndStartTour() {
        if (currentTourFrames.size < 2) return // минимум 2 кадра
        currentTour = FractalTour(
            name = tourName.ifBlank { "Экскурсия ${System.currentTimeMillis()}" },
            frames = currentTourFrames,
            loop = false
        )
        isRecordingTour = false
        startTour(currentTour!!)
    }

    fun addTourFrame() {
        val frame = TourFrame(
            plain = Plain(
                xMin = plain.xMin,
                xMax = plain.xMax,
                yMin = plain.yMin,
                yMax = plain.yMax,
                width = plain.width,
                height = plain.height
            ),
            fractalName = currentFractalName,
            colorSchemeName = currentColorSchemeName,
            durationMs = 3000
        )
        currentTourFrames += frame
    }

    private data class FractalView(
        val centerX: Double,
        val centerY: Double,
        val scale: Double
    ) {
        companion object {
            fun fromPlain(plain: Plain): FractalView {
                val cx = (plain.xMin + plain.xMax) / 2.0
                val cy = (plain.yMin + plain.yMax) / 2.0
                val width = plain.xMax - plain.xMin
                val scale = 3.0 / width  // 3.0 = initial width (-2 → +1)
                return FractalView(cx, cy, scale)
            }

        }
    }


    private fun setFractalByName(name: String) {
        when (name) {
            "Мандельброт" -> setMandelbrot()
            "Жюлиа" -> setJulia()
            "Трикорн" -> setTricorn()
            "Кубический" -> setCubicMandelbrot() // add if supported
        }
    }

    private fun setColorSchemeByName(name: String) {
        when (name) {
            "Стандартная" -> setStandardColors()
            "Огненная" -> setFireColors()
            "Радужная" -> setRainbowColors()
            "Ледяная" -> setIceColors()
        }
    }

    private fun setCubicMandelbrot() {
        resetPanFlag()
        saveCurrentState()
        fractalPainter = fractalPainter.withFractal(FractalFunctions.cubicMandelbrot)
        currentFractalName = "Кубический"
        mustRepaint = true
    }

    fun stopTour() {
        isTourRunning = false
        tourJob?.cancel()
        tourJob = null
    }

    private suspend fun animateBetween(
        start: TourFrame,
        end: TourFrame
    ) {
        if (currentFractalName != start.fractalName) {
            setFractalByName(start.fractalName)
        }
        if (currentColorSchemeName != start.colorSchemeName) {
            setColorSchemeByName(start.colorSchemeName)
        }

        val startPlain = start.plain
        val endPlain = end.plain

        val screenCenter = Offset(startPlain.width / 2f, startPlain.height / 2f)

        val startWorldX = Converter.xScr2Crt(screenCenter.x, startPlain)
        val startWorldY = Converter.yScr2Crt(screenCenter.y, startPlain)
        val endWorldX = Converter.xScr2Crt(screenCenter.x, endPlain)
        val endWorldY = Converter.yScr2Crt(screenCenter.y, endPlain)

        val startView = FractalView.fromPlain(startPlain)
        val endView = FractalView.fromPlain(endPlain)

        val invS0 = 1.0 / startView.scale
        val invS1 = 1.0 / endView.scale

        val duration = start.durationMs.coerceAtLeast(500)
        val steps = (duration / 16).coerceIn(2, 200).toInt()

        for (i in 0..steps) {
            if (!isTourRunning) break
            val t = i.toDouble() / steps

            val invS = lerp(invS0, invS1, t)
            val scale = 1.0 / invS

            val interpWorldX = lerp(startWorldX, endWorldX, t)
            val interpWorldY = lerp(startWorldY, endWorldY, t)

            val aspect = plain.width / plain.height
            val visibleWidth = 3.0 / scale
            val visibleHeight = visibleWidth / aspect

            plain.xMin = interpWorldX - visibleWidth / 2
            plain.xMax = interpWorldX + visibleWidth / 2
            plain.yMin = interpWorldY - visibleHeight / 2
            plain.yMax = interpWorldY + visibleHeight / 2

            updateZoomLevel()
            mustRepaint = true
            delay(16)
        }

        if (end.fractalName != currentFractalName) setFractalByName(end.fractalName)
        if (end.colorSchemeName != currentColorSchemeName) setColorSchemeByName(end.colorSchemeName)
    }

    private fun lerp(a: Double, b: Double, t: Double) = a + (b - a) * t

    init {
        saveCurrentState()
        updateHistoryInfo()
    }

    private fun saveCurrentState() {
        undoManager.saveState(
            plain = plain,
            fractalName = currentFractalName,
            colorSchemeName = currentColorSchemeName,
            iterationsOffset = iterationsOffset,
        )
        updateHistoryInfo()
    }

    private fun updateHistoryInfo() {
        canUndo = undoManager.canUndo()
        canRedo = undoManager.canRedo()
        historyInfo = undoManager.getHistoryInfo()
    }

    fun undo() {
        val state = undoManager.undo()
        state?.let { restoreState(it) }
        updateHistoryInfo()
    }

    fun redo() {
        val state = undoManager.redo()
        state?.let { restoreState(it) }
        updateHistoryInfo()
    }

    private fun restoreState(state: app.history.FractalState) {
        plain.xMin = state.plain.xMin
        plain.xMax = state.plain.xMax
        plain.yMin = state.plain.yMin
        plain.yMax = state.plain.yMax
        plain.width = state.plain.width
        plain.height = state.plain.height

        iterationsOffset = state.iterationsOffset
        currentFractalName = state.fractalName
        currentColorSchemeName = state.colorSchemeName

        updateFractalPainterWithState(state)
        updateZoomTextFromLevel()
        mustRepaint = true
    }
    private fun updateZoomTextFromLevel() {
        zoomText = when {
            zoomLevel >= 1_000_000 -> String.format("%.1fMx", zoomLevel / 1_000_000)
            zoomLevel >= 1_000 -> String.format("%.1fKx", zoomLevel / 1_000)
            zoomLevel >= 100 -> String.format("%.0fx", zoomLevel)
            zoomLevel >= 10 -> String.format("%.1fx", zoomLevel)
            zoomLevel >= 1 -> String.format("%.2fx", zoomLevel)
            else -> String.format("%.4fx", zoomLevel)
        }
    }

    private fun updateFractalPainterWithState(state: app.history.FractalState) {
        val fractalFunction = when (state.fractalName) {
            "Мандельброт" -> FractalFunctions.mandelbrot
            "Жюлиа" -> FractalFunctions.julia
            "Трикорн" -> FractalFunctions.tricorn
            else -> FractalFunctions.mandelbrot
        }

        val colorScheme = when (state.colorSchemeName) {
            "Стандартная" -> ColorSchemes.standard
            "Огненная" -> ColorSchemes.fire
            "Радужная" -> ColorSchemes.rainbow
            "Ледяная" -> ColorSchemes.ice
            else -> ColorSchemes.standard
        }

        fractalPainter = FractalPainter(
            plain,
            fractalFunction,
            colorScheme,
            { getAdjustedMaxIterations() }
        )
        mustRepaint = true
    }

    private fun updateFractalPainter() {
        fractalPainter = FractalPainter(
            plain,
            fractalPainter.fractalFunction,
            fractalPainter.colorScheme,
            { getAdjustedMaxIterations() }
        )
        mustRepaint = true
    }

    private fun getAdjustedMaxIterations(): Int {
        val baseIterations = IterationsCalculator.getMaxIterations(plain)
        return (baseIterations + iterationsOffset).coerceIn(100, 20000)
    }

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
        val oldZoom = zoomLevel

        zoomLevel = initialWidth / currentWidth

        zoomText = when {
            zoomLevel >= 1_000_000 -> String.format("%.1fMx", zoomLevel / 1_000_000)
            zoomLevel >= 1_000 -> String.format("%.1fKx", zoomLevel / 1_000)
            zoomLevel >= 100 -> String.format("%.0fx", zoomLevel)
            zoomLevel >= 10 -> String.format("%.1fx", zoomLevel)
            zoomLevel >= 1 -> String.format("%.2fx", zoomLevel)
            else -> String.format("%.4fx", zoomLevel)
        }
        mustRepaint = true

        if ((zoomLevel / oldZoom) > 1.05 || (oldZoom / zoomLevel) > 1.05) {
            SoundPlayer.zoom()
        }
    }

    fun paint(scope: DrawScope) = runBlocking {
        plain.width = scope.size.width
        plain.height = scope.size.height

        adjustFractalForWindowSize(scope.size.width, scope.size.height)

        if (mustRepaint
            || fractalImage.width != plain.width.toInt()
            || fractalImage.height != plain.height.toInt()
        ) {
            launch(Dispatchers.Default) {
                fractalImage = fractalPainter.generateImage(scope)
            }
        }
        // ??? Фигня, допилить
        if (fractalImage.height != 0 && fractalImage.width != 0 ) {
            launch(Dispatchers.Default) {
                fractalPainter.paint(scope, fractalImage)
            }
        }
        System.gc()
        mustRepaint = false
    }

    fun onStartSelecting(offset: Offset) {
        selectionStart = offset
        selectionEnd = offset
        isSelecting = true
    }

    fun onStopSelecting() {
        resetPanFlag()
        if (isSelecting) {
            val (selectionOffset, selectionSize) = selectionRect

            if (selectionSize.width > 10f && selectionSize.height > 10f) {
                saveCurrentState()

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

                plain.xMin = xMin
                plain.xMax = xMax
                plain.yMin = yMin
                plain.yMax = yMax

                updateZoomLevel()
                mustRepaint = true
            }
        }
        isSelecting = false
    }

    fun onSelecting(offset: Offset) {
        if (isSelecting) {
            selectionEnd = offset
        }
    }

    fun setMandelbrot() {
        resetPanFlag()
        saveCurrentState()
        fractalPainter = fractalPainter.withFractal(FractalFunctions.mandelbrot)
        currentFractalName = "Мандельброт"
        mustRepaint = true
    }

    fun setJulia() {
        resetPanFlag()
        saveCurrentState()
        fractalPainter = fractalPainter.withFractal(FractalFunctions.julia)
        currentFractalName = "Жюлиа"
        mustRepaint = true
    }

    fun setTricorn() {
        resetPanFlag()
        saveCurrentState()
        fractalPainter = fractalPainter.withFractal(FractalFunctions.tricorn)
        currentFractalName = "Трикорн"
        mustRepaint = true
    }

    fun setStandardColors() {
        resetPanFlag()
        saveCurrentState()
        fractalPainter = fractalPainter.withColorScheme(ColorSchemes.standard)
        currentColorSchemeName = "Стандартная"
        mustRepaint = true
    }

    fun setFireColors() {
        resetPanFlag()
        saveCurrentState()
        fractalPainter = fractalPainter.withColorScheme(ColorSchemes.fire)
        currentColorSchemeName = "Огненная"
        mustRepaint = true
    }

    fun setRainbowColors() {
        resetPanFlag()
        saveCurrentState()
        fractalPainter = fractalPainter.withColorScheme(ColorSchemes.rainbow)
        currentColorSchemeName = "Радужная"
        mustRepaint = true
    }

    fun setIceColors() {
        resetPanFlag()
        saveCurrentState()
        fractalPainter = fractalPainter.withColorScheme(ColorSchemes.ice)
        currentColorSchemeName = "Ледяная"
        mustRepaint = true
    }

    fun handlePan(delta: Offset) {

        if (!isPanning) {
            saveCurrentState()
            isPanning = true
        }

        val dx = delta.x / plain.width
        val dy = delta.y / plain.height
        val xRange = plain.xMax - plain.xMin
        val yRange = plain.yMax - plain.yMin
        plain.xMin -= dx * xRange
        plain.xMax -= dx * xRange
        plain.yMin -= dy * yRange
        plain.yMax -= dy * yRange


        SoundPlayer.pan()

        updateZoomLevel()
        mustRepaint = true
    }

    fun finishPanning() {
        if (isPanning) {
            isPanning = false
            saveCurrentState()
            updateHistoryInfo()
        }
    }
    private fun resetPanFlag() {
        if (isPanning) {
            finishPanning()
        } else {
            isPanning = false
        }
    }


    fun showContextMenuAt(position: Offset) {
        resetPanFlag()
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
        resetPanFlag()
        saveCurrentState()

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
    }


    fun refreshDetailedHistory() {
        detailedHistory = undoManager.getDetailedHistoryInfo()
    }
    fun resetToInitial() {
        resetPanFlag()
        // Очищаем историю
        undoManager.clear()

        // Сбрасываем параметры к начальным значениям
        plain.xMin = initialXMin
        plain.xMax = initialXMax
        plain.yMin = initialYMin
        plain.yMax = initialYMax

        iterationsOffset = 0
        currentFractalName = "Мандельброт"
        currentColorSchemeName = "Стандартная"

        // Обновляем FractalPainter
        fractalPainter = FractalPainter(
            plain,
            FractalFunctions.mandelbrot,
            ColorSchemes.standard,
            { getAdjustedMaxIterations() }
        )

        // Сохраняем это как новое начальное состояние
        saveCurrentState()

        // Обновляем UI
        updateZoomLevel()
        mustRepaint = true

        println("DEBUG: Reset to initial state with cleared history")
        println("  History info: ${undoManager.getHistoryInfo()}")
    }


    fun onAppClosing() {
        // Можно сохранить историю в файл или просто очистить
        // undoManager.clear()
    }


    fun randomJump() {
        resetPanFlag()
        saveCurrentState()

        // Список интересных точек фрактала
        val interestingPoints = listOf(
            Pair(-0.75, 0.1),    // Знаменитая "головастик"
            Pair(-0.1, 0.65),    // Верхняя часть
            Pair(0.28, 0.0),     // Правая область
            Pair(-1.25, 0.0),    // Левая часть
            Pair(-0.8, 0.15),    // Детальная область
            Pair(0.0, 0.8),      // Верхняя ось
            Pair(-0.5, 0.5),     // Диагональная область
            Pair(-1.0, 0.0),     // Еще левее
            Pair(0.2, 0.4),      // Правый верх
            Pair(-0.2, 0.6)      // Верхний центр
        )

        // Выбираем случайную точку из списка
        val (targetX, targetY) = interestingPoints.random()

        // Размер области для отображения
        val size = 1.0

        // Учитываем пропорции окна
        val aspect = plain.width / plain.height
        val width = size
        val height = width / aspect

        // Устанавливаем новые границы
        plain.xMin = targetX - width / 2
        plain.xMax = targetX + width / 2
        plain.yMin = targetY - height / 2
        plain.yMax = targetY + height / 2

        // Обновляем экран
        updateZoomLevel()
        mustRepaint = true
    }


    fun saveAsJpg() {
        ExporterJPG.exportToJpg(
            image = fractalImage,
            plain = plain,
            zoomText = zoomText,
            maxIterations = maxIterations
        )
    }

    fun updateTypeColorZoom(colorName:String,fractalName: String,plain: Plain,zoomLevel:Double) {
        resetPanFlag()
        saveCurrentState()
        this.plain.xMin = plain.xMin
        this.plain.xMax = plain.xMax
        this.plain.yMin = plain.yMin
        this.plain.yMax = plain.yMax
        fractalPainter = FractalPainter(
            this.plain,
            FractalFunctions.getFractalByName(fractalName),
            ColorSchemes.getColorSchemeByName(colorName)
        )
        currentFractalName = fractalName
        currentColorSchemeName = colorName
        this.zoomLevel = zoomLevel
        zoomText = when {
            zoomLevel >= 1_000_000 -> String.format("%.1fMx", zoomLevel / 1_000_000)
            zoomLevel >= 1_000 -> String.format("%.1fKx", zoomLevel / 1_000)
            zoomLevel >= 100 -> String.format("%.0fx", zoomLevel)
            zoomLevel >= 10 -> String.format("%.1fx", zoomLevel)
            zoomLevel >= 1 -> String.format("%.2fx", zoomLevel)
            else -> String.format("%.4fx", zoomLevel)
        }
        mustRepaint = true
    }
}