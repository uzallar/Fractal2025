package app.viewmodels

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class MainViewModelIntegrationTest {

    @Test
    fun `complete workflow with multiple operations - simplified`() {
        val vm = MainViewModel()

        // 1. Проверяем начальное состояние
        assertEquals("Мандельброт", vm.currentFractalName)
        assertEquals("Стандартная", vm.currentColorSchemeName)

        // 2. Меняем фрактал и проверяем
        vm.setJulia()
        assertEquals("Жюлиа", vm.currentFractalName)
        assertEquals("Стандартная", vm.currentColorSchemeName)

        // 3. Меняем цветовую схему и проверяем
        vm.setFireColors()
        assertEquals("Жюлиа", vm.currentFractalName)
        assertEquals("Огненная", vm.currentColorSchemeName)

        // 4. Отменяем последнее действие (цвет)
        vm.undo()
        assertEquals("Жюлиа", vm.currentFractalName)
        assertEquals("Стандартная", vm.currentColorSchemeName)

        // 5. Отменяем еще раз (фрактал)
        vm.undo()
        assertEquals("Мандельброт", vm.currentFractalName)
        assertEquals("Стандартная", vm.currentColorSchemeName)

        // 6. Повторяем отмененное (фрактал)
        vm.redo()
        assertEquals("Жюлиа", vm.currentFractalName)
        assertEquals("Стандартная", vm.currentColorSchemeName)

        // 7. Повторяем еще раз (цвет)
        vm.redo()
        assertEquals("Жюлиа", vm.currentFractalName)
        assertEquals("Огненная", vm.currentColorSchemeName)

        // 8. Сбрасываем
        vm.resetToInitial()
        assertEquals("Мандельброт", vm.currentFractalName)
        assertEquals("Стандартная", vm.currentColorSchemeName)
    }

    @Test
    fun `history info updates correctly`() {
        val vm = MainViewModel()

        // Начальное состояние - после создания ViewModel уже есть одно состояние в истории
        val initialHistoryInfo = vm.historyInfo
        println("Начальная historyInfo: '$initialHistoryInfo'")

        // Проверяем, что информация о истории не пустая
        assertTrue(initialHistoryInfo.isNotEmpty(), "History info не должен быть пустым после инициализации")

        // После изменений
        vm.setJulia()
        vm.setFireColors()

        val historyInfo = vm.historyInfo
        println("Конечная historyInfo: '$historyInfo'")

        // Проверяем, что информация о истории изменилась
        assertTrue(historyInfo.isNotEmpty(), "History info не должен быть пустым после изменений")

        // Можно проверить, что historyInfo содержит информацию о шагах
        // или просто убедиться, что он отличается от начального
        assertNotEquals(initialHistoryInfo, historyInfo,
            "History info должен измениться после операций")
    }
}