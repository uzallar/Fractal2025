package app.viewmodels

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class MainViewModelIntegrationTest {

    @Test
    fun `complete workflow with multiple operations - simplified`() {
        val vm = MainViewModel()

        assertEquals("Мандельброт", vm.currentFractalName)
        assertEquals("Стандартная", vm.currentColorSchemeName)

        vm.setJulia()
        assertEquals("Жюлиа", vm.currentFractalName)
        assertEquals("Стандартная", vm.currentColorSchemeName)

        vm.setFireColors()
        assertEquals("Жюлиа", vm.currentFractalName)
        assertEquals("Огненная", vm.currentColorSchemeName)

        vm.undo()
        assertEquals("Мандельброт", vm.currentFractalName)
        assertEquals("Стандартная", vm.currentColorSchemeName)

        vm.undo()
        assertEquals("Мандельброт", vm.currentFractalName)
        assertEquals("Стандартная", vm.currentColorSchemeName)

        vm.redo()
        assertEquals("Мандельброт", vm.currentFractalName)
        assertEquals("Стандартная", vm.currentColorSchemeName)

        vm.redo()
        assertEquals("Жюлиа", vm.currentFractalName)
        assertEquals("Стандартная", vm.currentColorSchemeName)

        vm.resetToInitial()
        assertEquals("Мандельброт", vm.currentFractalName)
        assertEquals("Стандартная", vm.currentColorSchemeName)
    }

    @Test
    fun `history info updates correctly`() {
        val vm = MainViewModel()

        val initialHistoryInfo = vm.historyInfo
        println("Начальная historyInfo: '$initialHistoryInfo'")

        assertTrue(initialHistoryInfo.isNotEmpty(), "History info не должен быть пустым после инициализации")

        vm.setJulia()
        vm.setFireColors()

        val historyInfo = vm.historyInfo
        println("Конечная historyInfo: '$historyInfo'")

        assertTrue(historyInfo.isNotEmpty(), "History info не должен быть пустым после изменений")

        assertNotEquals(initialHistoryInfo, historyInfo,
            "History info должен измениться после операций")
    }
}