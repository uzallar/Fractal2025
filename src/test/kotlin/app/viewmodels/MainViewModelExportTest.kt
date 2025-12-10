package app.viewmodels


import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class MainViewModelExportTest {

    @Test
    fun `saveAsJpg does not throw exception`() {
        val vm = MainViewModel()

        // Этот тест просто проверяет, что метод не падает с исключением
        // Фактический экспорт файла будет происходить в UI-потоке
        assertDoesNotThrow {
            vm.saveAsJpg()
        }
    }
}