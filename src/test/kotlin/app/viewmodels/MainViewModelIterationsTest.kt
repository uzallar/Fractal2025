package app.viewmodels

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class MainViewModelIterationsTest {

    @Test
    fun `maxIterations returns valid value`() {
        val vm = MainViewModel()

        val iterations = vm.maxIterations

        assertTrue(iterations >= 100)
        assertTrue(iterations <= 20000)
    }

    @Test
    fun `detailed history can be refreshed`() {
        val vm = MainViewModel()

        assertDoesNotThrow {
            vm.refreshDetailedHistory()
        }

        assertNotNull(vm.detailedHistory)
    }
}