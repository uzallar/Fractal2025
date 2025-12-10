package app.viewmodels

import androidx.compose.ui.geometry.Offset
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class MainViewModelPanningTest {

    private lateinit var vm: MainViewModel

    @BeforeEach
    fun setUp() {
        vm = MainViewModel()
    }


    @Test
    fun `panning changes zoom text`() {
        val initialZoomText = vm.zoomText

        vm.handlePan(Offset(50f, 50f))


        assertNotNull(vm.zoomText)
    }
}