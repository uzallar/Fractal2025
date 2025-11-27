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
    student1Print()
    student3Print()
    student5Print()
    student6Print()
    student8Print()
    student9Print()
}

// пробный коммит


private fun student2Print() {
    val x = 7
    val y = 8
    val product = x * y
    println("Student 2: $x * $y = $product")
}


private fun student9Print() {
    val randomNum = (1..100).random()
    println("Student 9: Random number between 1 and 100: $randomNum")
}



private fun student1Print() {
    val a = 5
    val b = 3
    val sum = a + b
    println("Student 1: $a + $b = $sum")
}

private fun student3Print() {
    val a = 15
    val b = 4
    val difference = a - b
    println("Student 3: $a - $b = $difference")
}

private fun student5Print() {
    val base = 2
    val exponent = 5
    val power = base.toDouble().pow(exponent.toDouble()).toInt()
    println("Student 5: $base^$exponent = $power")
//привет привет :)
}

private fun student6Print() {
    val dividend = 17
    val divisor = 5
    val remainder = dividend % divisor
    println("Student 6: $dividend % $divisor = $remainder")
}

private fun student8Print() {
    val pi = Math.PI
    println("Student 8: PI = ${"%.4f".format(pi)}")
}
