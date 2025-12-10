package app.history

import app.painting.convertation.Plain
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UndoManagerTest {

    private lateinit var undoManager: UndoManager
    private lateinit var testPlain: Plain

    @BeforeEach
    fun setUp() {
        undoManager = UndoManager(maxSteps = 3)
        testPlain = Plain(
            xMin = -2.0,
            xMax = 2.0,
            yMin = -2.0,
            yMax = 2.0,
            width = 800F,
            height = 600F
        )
    }

    @Test
    fun `test initial state`() {
        assertFalse(undoManager.canUndo())
        assertFalse(undoManager.canRedo())
        assertEquals("← 0 | → 0", undoManager.getHistoryInfo())
    }

    @Test
    fun `test save first state`() {
        undoManager.saveState(
            plain = testPlain,
            fractalName = "Mandelbrot",
            colorSchemeName = "Rainbow",
            iterationsOffset = 0
        )

        assertFalse(undoManager.canUndo())
        assertFalse(undoManager.canRedo())
        assertEquals("← 0 | → 0", undoManager.getHistoryInfo())
    }

    @Test
    fun `test undo and redo with multiple states`() {
        undoManager.saveState(testPlain.copy(xMin = -2.0), "Mandelbrot", "Rainbow", 0)
        undoManager.saveState(testPlain.copy(xMin = -1.0), "Julia", "Fire", 10)
        undoManager.saveState(testPlain.copy(xMin = 0.0), "Burning Ship", "Ocean", 20)

        assertTrue(undoManager.canUndo())
        assertFalse(undoManager.canRedo())
        assertEquals("← 2 | → 0", undoManager.getHistoryInfo())

        val undoneState = undoManager.undo()
        assertNotNull(undoneState)
        assertEquals("Julia", undoneState!!.fractalName)
        assertEquals(-1.0, undoneState.plain.xMin, 0.001)
        assertTrue(undoManager.canUndo())
        assertTrue(undoManager.canRedo())
        assertEquals("← 1 | → 1", undoManager.getHistoryInfo())

        val undoneState2 = undoManager.undo()
        assertNotNull(undoneState2)
        assertEquals("Mandelbrot", undoneState2!!.fractalName)
        assertEquals(-2.0, undoneState2.plain.xMin, 0.001)
        assertFalse(undoManager.canUndo())
        assertTrue(undoManager.canRedo())
        assertEquals("← 0 | → 2", undoManager.getHistoryInfo())

        val redoneState = undoManager.redo()
        assertNotNull(redoneState)
        assertEquals("Julia", redoneState!!.fractalName)
        assertEquals(-1.0, redoneState.plain.xMin, 0.001)
        assertTrue(undoManager.canUndo())
        assertTrue(undoManager.canRedo())
        assertEquals("← 1 | → 1", undoManager.getHistoryInfo())

        val redoneState2 = undoManager.redo()
        assertNotNull(redoneState2)
        assertEquals("Burning Ship", redoneState2!!.fractalName)
        assertEquals(0.0, redoneState2.plain.xMin, 0.001)
        assertTrue(undoManager.canUndo())
        assertFalse(undoManager.canRedo())
        assertEquals("← 2 | → 0", undoManager.getHistoryInfo())
    }

    @Test
    fun `test max steps limit`() {
        undoManager.saveState(testPlain.copy(xMin = 0.0), "Fractal0", "Scheme0", 0)
        undoManager.saveState(testPlain.copy(xMin = 1.0), "Fractal1", "Scheme1", 1)
        undoManager.saveState(testPlain.copy(xMin = 2.0), "Fractal2", "Scheme2", 2)
        undoManager.saveState(testPlain.copy(xMin = 3.0), "Fractal3", "Scheme3", 3)
        undoManager.saveState(testPlain.copy(xMin = 4.0), "Fractal4", "Scheme4", 4)

        assertTrue(undoManager.canUndo())
        assertEquals("← 3 | → 0", undoManager.getHistoryInfo())

        undoManager.undo()
        undoManager.undo()
        undoManager.undo()

        assertFalse(undoManager.canUndo())

        val currentState = undoManager.undo()
        assertNull(currentState)
    }

    @Test
    fun `test redo stack cleared on new action`() {
        undoManager.saveState(testPlain.copy(xMin = 0.0), "Fractal1", "Scheme1", 0)
        undoManager.saveState(testPlain.copy(xMin = 1.0), "Fractal2", "Scheme2", 1)

        undoManager.undo()
        assertTrue(undoManager.canRedo())
        assertEquals("← 0 | → 1", undoManager.getHistoryInfo())

        undoManager.saveState(testPlain.copy(xMin = 2.0), "Fractal3", "Scheme3", 2)

        assertFalse(undoManager.canRedo())
        assertEquals("← 1 | → 0", undoManager.getHistoryInfo())
    }

//    @Test
//    fun `test clear history`() {
//        undoManager.saveState(testPlain.copy(xMin = 0.0), "Fractal1", "Scheme1", 0)
//        undoManager.saveState(testPlain.copy(xMin = 1.0), "Fractal2", "Scheme2", 1)
//        undoManager.saveState(testPlain.copy(xMin = 2.0), "Fractal3", "Scheme3", 2)
//
//        undoManager.undo()
//        undoManager.undo()
//
//        assertTrue(undoManager.canUndo())
//        assertTrue(undoManager.canRedo())
//
//        val initialState = undoManager.clear()
//        assertNotNull(initialState)
//
//        assertEquals("Fractal1", initialState!!.fractalName)
//        assertEquals(0.0, initialState.plain.xMin, 0.001)
//
//        assertFalse(undoManager.canUndo())
//        assertFalse(undoManager.canRedo())
//        assertEquals("← 0 | → 0", undoManager.getHistoryInfo())
//    }

//    @Test
//    fun `test getDetailedHistoryInfo`() {
//        undoManager.saveState(testPlain.copy(xMin = 0.0), "Mandelbrot", "Rainbow", 0)
//        undoManager.saveState(testPlain.copy(xMin = 1.0), "Julia", "Fire", 10)
//        undoManager.saveState(testPlain.copy(xMin = 2.0), "Burning Ship", "Ocean", 20)
//
//        undoManager.undo()
//
//        val detailedInfo = undoManager.getDetailedHistoryInfo()
//
//        assertTrue(detailedInfo.contains("=== ИСТОРИЯ ДЕЙСТВИЙ ==="))
//        assertTrue(detailedInfo.contains("Назад (2 доступно):"))
//        assertTrue(detailedInfo.contains("Вперёд (1 доступно):"))
//        assertTrue(detailedInfo.contains("Начальное: Mandelbrot - Rainbow"))
//        assertTrue(detailedInfo.contains("Шаг 1: Julia - Fire"))
//        assertTrue(detailedInfo.contains("Шаг 1: Burning Ship - Ocean"))
//    }

    @Test
    fun `test undo when only one state exists`() {
        undoManager.saveState(testPlain, "Mandelbrot", "Rainbow", 0)

        val result = undoManager.undo()
        assertNull(result)
        assertFalse(undoManager.canUndo())
    }

    @Test
    fun `test redo when empty`() {
        undoManager.saveState(testPlain, "Mandelbrot", "Rainbow", 0)

        val result = undoManager.redo()
        assertNull(result)
        assertFalse(undoManager.canRedo())
    }

    @Test
    fun `test state copying`() {
        val originalPlain = testPlain.copy(xMin = 5.0, xMax = 10.0)

        undoManager.saveState(originalPlain, "TestFractal", "TestScheme", 100)

        originalPlain.xMin = 999.0
        originalPlain.xMax = 999.0

        undoManager.saveState(testPlain, "AnotherFractal", "AnotherScheme", 200)
        val undoneState = undoManager.undo()

        assertNotNull(undoneState)
        assertEquals(5.0, undoneState!!.plain.xMin, 0.001)
        assertEquals(10.0, undoneState.plain.xMax, 0.001)
        assertNotEquals(999.0, undoneState.plain.xMin, 0.001)
    }

    @Test
    fun `test multiple undo-redo cycles`() {
        val states = listOf(
            Triple("Fractal1", "Scheme1", 0.0),
            Triple("Fractal2", "Scheme2", 1.0),
            Triple("Fractal3", "Scheme3", 2.0),
            Triple("Fractal4", "Scheme4", 3.0)
        )

        states.forEach { (name, scheme, xMin) ->
            undoManager.saveState(testPlain.copy(xMin = xMin), name, scheme, xMin.toInt())
        }

        repeat(3) {
            undoManager.undo()
        }

        assertEquals("← 0 | → 3", undoManager.getHistoryInfo())

        repeat(2) {
            undoManager.redo()
        }

        assertEquals("← 2 | → 1", undoManager.getHistoryInfo())

        undoManager.saveState(testPlain.copy(xMin = 10.0), "Fractal5", "Scheme5", 10)

        assertEquals("← 3 | → 0", undoManager.getHistoryInfo())
        assertFalse(undoManager.canRedo())
    }

    @Test
    fun `test undo with no states`() {
        val result = undoManager.undo()
        assertNull(result)
        assertFalse(undoManager.canUndo())
    }

    @Test
    fun `test getHistoryInfo with empty manager`() {
        assertEquals("← 0 | → 0", undoManager.getHistoryInfo())
    }

//    @Test
//    fun `test getDetailedHistoryInfo with empty manager`() {
//        val info = undoManager.getDetailedHistoryInfo()
//        assertTrue(info.contains("Назад (0 доступно):"))
//        assertTrue(info.contains("Вперёд (0 доступно):"))
//    }

    @Test
    fun `test clear with empty manager`() {
        val result = undoManager.clear()
        assertNull(result)
        assertFalse(undoManager.canUndo())
        assertFalse(undoManager.canRedo())
    }

    @Test
    fun `test undo after clearing redo stack`() {
        undoManager.saveState(testPlain.copy(xMin = 0.0), "Fractal1", "Scheme1", 0)
        undoManager.saveState(testPlain.copy(xMin = 1.0), "Fractal2", "Scheme2", 1)
        undoManager.undo()

        assertTrue(undoManager.canRedo())

        undoManager.saveState(testPlain.copy(xMin = 2.0), "Fractal3", "Scheme3", 2)

        assertFalse(undoManager.canRedo())
        assertTrue(undoManager.canUndo())

        val undoneState = undoManager.undo()
        assertNotNull(undoneState)
        assertEquals("Fractal1", undoneState!!.fractalName)
    }

    @Test
    fun `test max steps with exactly max steps states`() {
        undoManager = UndoManager(maxSteps = 2)

        undoManager.saveState(testPlain.copy(xMin = 0.0), "Fractal0", "Scheme0", 0)
        undoManager.saveState(testPlain.copy(xMin = 1.0), "Fractal1", "Scheme1", 1)
        undoManager.saveState(testPlain.copy(xMin = 2.0), "Fractal2", "Scheme2", 2)

        assertEquals("← 2 | → 0", undoManager.getHistoryInfo())

        undoManager.undo()
        undoManager.undo()

        assertFalse(undoManager.canUndo())
        assertEquals("← 0 | → 2", undoManager.getHistoryInfo())
    }
}