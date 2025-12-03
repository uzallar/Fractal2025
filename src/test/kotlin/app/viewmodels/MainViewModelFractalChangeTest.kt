package app.viewmodels

import app.fractal.FractalFunctions
import app.painting.ColorSchemes
import app.viewmodels.MainViewModel
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MainViewModelFractalChangeTestTest {

    @Test
    fun `setMandelbrot updates fractal name`() {
        val vm = MainViewModel()
        vm.setMandelbrot()
        assertEquals("Мандельброт", vm.currentFractalName)
    }

    @Test
    fun `setJulia updates fractal name`() {
        val vm = MainViewModel()
        vm.setJulia()
        assertEquals("Жюлиа", vm.currentFractalName)
    }

    @Test
    fun `setBurningShip updates fractal name`() {
        val vm = MainViewModel()
        vm.setBurningShip()
        assertEquals("Горящий корабль", vm.currentFractalName)
    }

    @Test
    fun `setTricorn updates fractal name`() {
        val vm = MainViewModel()
        vm.setTricorn()
        assertEquals("Трикорн", vm.currentFractalName)
    }
}
