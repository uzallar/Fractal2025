package app.history

import app.painting.convertation.Plain

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
        val state = FractalState(
            plain = createCopy(plain),
            fractalName = fractalName,
            colorSchemeName = colorSchemeName,
            iterationsOffset = iterationsOffset
        )

        undoStack.add(state)
        redoStack.clear()

        if (undoStack.size > maxSteps + 1) {
            undoStack.removeAt(1)
        }

        println("DEBUG: State saved. Undo stack: ${undoStack.size}, Redo stack: ${redoStack.size}")
    }

    fun undo(): FractalState? {
        if (!canUndo()) {
            println("DEBUG: Cannot undo. Stack size: ${undoStack.size}")
            return null
        }

        val currentState = undoStack.removeLast()
        redoStack.add(currentState)

        val previousState = undoStack.last()

        println("DEBUG: Undo performed.")
        println("  Undo: ${undoStack.size}, Redo: ${redoStack.size}")

        return previousState
    }

    fun redo(): FractalState? {
        if (!canRedo()) {
            println("DEBUG: Cannot redo. Redo stack: ${redoStack.size}")
            return null
        }

        val nextState = redoStack.removeLast()
        undoStack.add(nextState)

        println("DEBUG: Redo performed.")
        println("  Undo: ${undoStack.size}, Redo: ${redoStack.size}")

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

        println("DEBUG: History cleared. Initial state restored.")
        return initialState
    }

    fun getHistoryInfo(): String {
        val undoSteps = if (undoStack.size > 0) undoStack.size - 1 else 0
        val redoSteps = redoStack.size
        return "← $undoSteps | → $redoSteps"
    }

    fun getDetailedHistoryInfo(): String {
        val info = StringBuilder()
        info.appendLine("Назад (${undoStack.size - 1} доступно):")
        undoStack.forEachIndexed { index, state ->
            val prefix = if (index == 0) "Начальное: " else "Шаг ${index}: "
            info.appendLine("  $prefix${state.fractalName} - ${state.colorSchemeName}")
        }
        info.appendLine("Вперёд (${redoStack.size} доступно):")
        redoStack.reversed().forEachIndexed { index, state ->
            info.appendLine("  Шаг ${index + 1}: ${state.fractalName} - ${state.colorSchemeName}")
        }
        return info.toString()
    }
}