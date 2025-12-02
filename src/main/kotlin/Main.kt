package app

import androidx.compose.runtime.Composable
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.ui.PaintPanel
import app.ui.SelectionPanel
import app.viewmodels.MainViewModel
// 1. Добавить эти 3 импорта
import app.mouse.MousePan.rightClickPan
import app.mouse.ClipboardService
import app.mouse.FractalContextMenu

@Composable
@Preview
fun App(viewModel: MainViewModel = MainViewModel()) {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            PaintPanel(
                // 2. Добавить .rightClickPan в эту строку
                Modifier.fillMaxSize().rightClickPan(
                    onPanDelta = { viewModel.handlePan(it) },
                    onRightClick = { viewModel.contextMenuPosition = it; viewModel.showContextMenu = true }
                ),
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

            // 3. Добавить эту одну строку контекстного меню
            FractalContextMenu(
                viewModel.showContextMenu,
                { viewModel.showContextMenu = false },
                { ClipboardService.copyFractalCoordinates(viewModel.contextMenuPosition, viewModel.currentPlain); viewModel.showContextMenu = false },
                if (viewModel.showContextMenu) ClipboardService.getCoordinatesString(viewModel.contextMenuPosition, viewModel.currentPlain) else ""
            )

            // Панель управления
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
            ) {
                ControlPanel(viewModel)
            }

            // Информационная панель
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(20.dp)
            ) {
                InfoPanel(viewModel)
            }
        }
    }
}

// Всё остальное БЕЗ ИЗМЕНЕНИЙ...
@Composable
fun ControlPanel(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .background(Color.Black.copy(0.7f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text("Фрактал:", color = Color.White, fontSize = 14.sp)
        Row {
            Button(
                onClick = { viewModel.setMandelbrot() },
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text("М")
            }
            Button(
                onClick = { viewModel.setJulia() },
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text("Ж")
            }
            Button(
                onClick = { viewModel.setBurningShip() },
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text("К")
            }
            Button(
                onClick = { viewModel.setTricorn() }
            ) {
                Text("Т")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("Цвета:", color = Color.White, fontSize = 14.sp)
        Row {
            Button(
                onClick = { viewModel.setStandardColors() },
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text("Ст")
            }
            Button(
                onClick = { viewModel.setFireColors() },
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text("Ог")
            }
            Button(
                onClick = { viewModel.setRainbowColors() },
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text("Рд")
            }
            Button(
                onClick = { viewModel.setCosmicColors() }
            ) {
                Text("Кс")
            }
        }
    }
}

@Composable
fun InfoPanel(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .background(Color.Black.copy(0.7f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Фрактал: ${viewModel.currentFractalName}",
            color = Color.White,
            fontSize = 14.sp
        )
        Text(
            text = "Цвета: ${viewModel.currentColorSchemeName}",
            color = Color.White,
            fontSize = 14.sp
        )
        Text(
            text = "Итераций: ${app.fractal.IterationsCalculator.getMaxIterations(viewModel.currentPlain)}",
            color = Color.White,
            fontSize = 14.sp
        )
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