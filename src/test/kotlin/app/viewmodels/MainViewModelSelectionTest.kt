package app.viewmodels

import androidx.compose.ui.geometry.Offset
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class MainViewModelSelectionTest {

    private lateinit var vm: MainViewModel

    @BeforeEach
    fun setUp() {
        vm = MainViewModel()
    }

    @Test
    fun `onStartSelecting sets isSelecting to true`() {
        vm.onStartSelecting(Offset(10f, 10f))
        assertTrue(vm.isSelecting)
    }

    @Test
    fun `selectionRect returns zero when not selecting`() {
        val (offset, size) = vm.selectionRect
        assertEquals(Offset.Zero, offset)
        assertEquals(androidx.compose.ui.geometry.Size.Zero, size)
    }

    @Test
    fun `selectionRect returns correct rectangle when selecting`() {
        vm.onStartSelecting(Offset(10f, 10f))
        vm.onSelecting(Offset(50f, 50f))

        val (offset, size) = vm.selectionRect
        assertEquals(Offset(10f, 10f), offset)
        assertEquals(40f, size.width)
        assertEquals(40f, size.height)
    }

    @Test
    fun `onStopSelecting with small selection does nothing`() {
        val initialPlain = vm.currentPlain.copy()

        vm.onStartSelecting(Offset(10f, 10f))
        vm.onSelecting(Offset(15f, 15f)) // Маленькая область (<10px)
        vm.onStopSelecting()

        assertEquals(initialPlain.xMin, vm.currentPlain.xMin, 0.001)
        assertEquals(initialPlain.xMax, vm.currentPlain.xMax, 0.001)
    }
}