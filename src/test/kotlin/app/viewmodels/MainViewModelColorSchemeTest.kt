package app.viewmodels

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class MainViewModelColorSchemeTest {

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
        // В коде используется ice, но в UI отображается "Ледяная"
        vm.setCosmicColors()
        assertEquals("Ледяная", vm.currentColorSchemeName)
    }

    @Test
    fun `initial color scheme should be standard`() {
        val vm = MainViewModel()
        assertEquals("Стандартная", vm.currentColorSchemeName)
    }
}