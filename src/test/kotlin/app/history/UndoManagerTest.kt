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
    fun `test undo with no states`() {
        val result = undoManager.undo()
        assertNull(result)
        assertFalse(undoManager.canUndo())
    }

    @Test
    fun `test getHistoryInfo with empty manager`() {
        assertEquals("← 0 | → 0", undoManager.getHistoryInfo())
    }



    @Test
    fun `test clear with empty manager`() {
        val result = undoManager.clear()
        assertNull(result)
        assertFalse(undoManager.canUndo())
        assertFalse(undoManager.canRedo())
    }




}