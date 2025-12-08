package app.viewmodels

import app.painting.ColorSchemes
import app.viewmodels.MainViewModel
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MainViewModelColorChangeTestTest {

    @Test
    fun `setStandardColors updates color scheme name`() {
        val vm = MainViewModel()
        vm.setStandardColors()
        assertEquals("Стандартная", vm.currentColorSchemeName)
    }

    @Test
    fun `setFireColors updates color scheme name`() {
        val vm = MainViewModel()
        vm.setFireColors()
        assertEquals("Огненная", vm.currentColorSchemeName)
    }

    @Test
    fun `setRainbowColors updates color scheme name`() {
        val vm = MainViewModel()
        vm.setRainbowColors()
        assertEquals("Радужная", vm.currentColorSchemeName)
    }

    @Test
    fun `setCosmicColors updates color scheme name`() {
        val vm = MainViewModel()
        vm.setCosmicColors()
        assertEquals("Космическая", vm.currentColorSchemeName)
    }
}
