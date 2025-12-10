package app.history

import app.painting.convertation.Plain
import kotlin.math.abs

data class FractalState(
    val plain: Plain,
    val fractalName: String,
    val colorSchemeName: String,
    val iterationsOffset: Int
)

class UndoManager(private val maxSteps: Int = 100) {
    private val undoStack = mutableListOf<FractalState>()
    private val redoStack = mutableListOf<FractalState>()

    fun canUndo(): Boolean = undoStack.size > 1
    fun canRedo(): Boolean = redoStack.isNotEmpty()

    fun saveState(
        plain: Plain,
        fractalName: String,
        colorSchemeName: String,
        iterationsOffset: Int
    ) {
        println("=== UNDO MANAGER: saveState called ===")
        println("Fractal: '$fractalName' -> '${undoStack.lastOrNull()?.fractalName ?: "none"}'")
        println("Color: '$colorSchemeName' -> '${undoStack.lastOrNull()?.colorSchemeName ?: "none"}'")
        val state = FractalState(
            plain = createCopy(plain),
            fractalName = fractalName,
            colorSchemeName = colorSchemeName,
            iterationsOffset = iterationsOffset
        )
        if (undoStack.isNotEmpty()) {
            val lastState = undoStack.last()
            println("Last state in stack: ${lastState.fractalName} - ${lastState.colorSchemeName}")
            if (undoStack.isNotEmpty()) {
                val lastState = undoStack.last()


                val hasFractalOrColorChange =
                    lastState.fractalName != state.fractalName ||
                            lastState.colorSchemeName != state.colorSchemeName ||
                            lastState.iterationsOffset != state.iterationsOffset


                val hasCoordinateChange =
                    abs(lastState.plain.xMin - state.plain.xMin) > 1e-4 ||
                            abs(lastState.plain.xMax - state.plain.xMax) > 1e-4 ||
                            abs(lastState.plain.yMin - state.plain.yMin) > 1e-4 ||
                            abs(lastState.plain.yMax - state.plain.yMax) > 1e-4

                if (!hasFractalOrColorChange && !hasCoordinateChange) {
                    return
                }
            }


        }



        undoStack.add(state)


        if (undoStack.size > maxSteps) {
            clear()
        }


    }

    fun undo(): FractalState? {
        if (!canUndo()) {
            return null
        }

        val currentState = undoStack.removeLast()
        redoStack.add(currentState)

        return undoStack.last()
    }

    fun redo(): FractalState? {
        if (!canRedo()) {
            return null
        }

        val nextState = redoStack.removeLast()
        undoStack.add(nextState)

        return nextState
    }

    private fun createCopy(plain: Plain): Plain {
        return Plain(
            xMin = plain.xMin,
            xMax = plain.xMax,
            yMin = plain.yMin,
            yMax = plain.yMax,
            width = plain.width,
            height = plain.height
        )
    }

    fun clear(): FractalState? {
        val initialState = undoStack.firstOrNull()
        undoStack.clear()
        redoStack.clear()

        initialState?.let {
            undoStack.add(it)
        }

        return initialState
    }

    fun getHistoryInfo(): String {
        val undoSteps = if (undoStack.size > 0) undoStack.size - 1 else 0
        val redoSteps = redoStack.size
        return "← $undoSteps | → $redoSteps"
    }

    fun getDetailedHistoryInfo(): String {
        val info = StringBuilder()
        info.appendLine("=== ИСТОРИЯ ДЕЙСТВИЙ ===")
        info.appendLine("Назад (${undoStack.size - 1} доступно):")
        undoStack.forEachIndexed { index, state ->
            val prefix = if (index == 0) "Начальное: " else "Шаг ${index}: "
            info.appendLine("  $prefix${state.fractalName} - ${state.colorSchemeName}")
        }
        info.appendLine("Вперёд (${redoStack.size} доступно):")
        redoStack.reversed().forEachIndexed { index, state ->
            info.appendLine("  Шаг ${index + 1}: ${state.fractalName} - ${state.colorSchemeName}")
        }
        info.appendLine("=== КОНЕЦ ИСТОРИИ ===")
        return info.toString()
    }
}