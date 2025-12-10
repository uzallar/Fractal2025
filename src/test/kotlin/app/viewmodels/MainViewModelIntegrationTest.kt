package app.viewmodels

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class MainViewModelIntegrationTest {



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