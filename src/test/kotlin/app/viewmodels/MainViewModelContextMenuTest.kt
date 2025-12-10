package app.viewmodels


import androidx.compose.ui.geometry.Offset
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class MainViewModelContextMenuTest {

    private lateinit var vm: MainViewModel

    @BeforeEach
    fun setUp() {
        vm = MainViewModel()
    }

    @Test
    fun `showContextMenuAt sets correct position and visibility`() {
        val position = Offset(100f, 150f)

        vm.showContextMenuAt(position)

        assertEquals(position, vm.contextMenuPosition)
        assertTrue(vm.showContextMenu)
        assertTrue(vm.contextMenuCoordinates.isNotEmpty())
    }

    @Test
    fun `hideContextMenu hides menu`() {
        vm.showContextMenuAt(Offset(100f, 100f))
        vm.hideContextMenu()

        assertFalse(vm.showContextMenu)
    }

    @Test
    fun `context menu coordinates contain numbers`() {
        vm.showContextMenuAt(Offset(100f, 100f))

        // Координаты должны содержать числа
        assertTrue(vm.contextMenuCoordinates.contains(Regex("[-\\d.]+")))
    }
}