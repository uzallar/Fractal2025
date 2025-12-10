package app.viewmodels

import androidx.compose.ui.geometry.Offset
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class MainViewModelZoomNavigationTest {

    private lateinit var vm: MainViewModel

    @BeforeEach
    fun setUp() {
        vm = MainViewModel()
    }

    @Test
    fun `initial zoom level should be 1x`() {
        assertEquals("1x", vm.zoomText)
    }

    @Test
    fun `resetZoom resets to initial zoom`() {
        // Изменяем масштаб через выбор области
        vm.onStartSelecting(Offset(100f, 100f))
        vm.onStopSelecting() // Это вызовет saveCurrentState

        vm.resetZoom()

        assertEquals("1x", vm.zoomText)
    }

    @Test
    fun `resetToInitial clears history and resets state`() {
        vm.setJulia()
        vm.setFireColors()

        vm.resetToInitial()

        assertEquals("Мандельброт", vm.currentFractalName)
        assertEquals("Стандартная", vm.currentColorSchemeName)
        assertEquals("1,00x", vm.zoomText) // Изменено на 1,00x
    }

    @Test
    fun `randomJump changes zoom level`() {
        val initialZoom = vm.zoomText

        vm.randomJump()

        // После прыжка масштаб должен измениться
        assertNotEquals(initialZoom, vm.zoomText)
    }
}