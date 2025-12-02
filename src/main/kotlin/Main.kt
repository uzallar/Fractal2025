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
import kotlin.math.pow
import app.fractal.IterationsCalculator // для использованяи функции вычисления кол-ва итераций при увеличении
//библиотеки для работы с Text()
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.foundation.layout.padding

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
            Text(
                text = "Итераций: ${IterationsCalculator.getMaxIterations(viewModel.currentPlain)}",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(20.dp)
                    .background(Color.Black.copy(0.7f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
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
}



