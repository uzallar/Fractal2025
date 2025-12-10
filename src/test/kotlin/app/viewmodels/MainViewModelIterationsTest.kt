package app.viewmodels

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class MainViewModelIterationsTest {

    @Test
    fun `maxIterations returns valid value`() {
        val vm = MainViewModel()

        val iterations = vm.maxIterations

        // Итерации должны быть в разумных пределах
        assertTrue(iterations >= 100)
        assertTrue(iterations <= 20000)
    }

    @Test
    fun `detailed history can be refreshed`() {
        val vm = MainViewModel()

        // Проверяем, что метод не падает
        assertDoesNotThrow {
            vm.refreshDetailedHistory()
        }

        // Детальная история может быть пустой или содержать информацию
        assertNotNull(vm.detailedHistory)
    }
}