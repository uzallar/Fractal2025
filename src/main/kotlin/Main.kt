import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.ui.PaintPanel
import app.ui.SelectionPanel
import app.viewmodels.MainViewModel

@Composable
@Preview
fun App(viewModel: MainViewModel= MainViewModel()) {
    MaterialTheme {
        Box {
            PaintPanel(
                Modifier.fillMaxSize(),
                onImageUpdate = {
                    viewModel.onImageUpdate(it)
                }
            ) {
                viewModel.paint(it)
            }
            SelectionPanel(
                viewModel.selectionOffset,
                viewModel.selectionSize,
                Modifier.fillMaxSize(),
                viewModel::onStartSelecting,
                viewModel::onStopSelecting,
                viewModel::onSelecting,
            )
        }
    }
}

fun main(): Unit = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Фрактал - 2025 (гр. 05-307)"
    ) {
        App()
    }
    student2Print()
}

// пробный коммит
// что-то еще
// еще что то пропро


private fun student2Print() {
    val x = 7
    val y = 8
    val product = x * y
    println("Student 2: $x * $y = $product")
}

