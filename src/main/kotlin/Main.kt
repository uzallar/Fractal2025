package app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.mouse.FractalContextMenu
import app.mouse.fractalMouseHandlers
import app.viewmodels.MainViewModel

fun main() = application {
    val viewModel = remember { MainViewModel() }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Фракталы"
    ) {
        MaterialTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Фрактал: ${viewModel.currentFractalName}") },
                        backgroundColor = MaterialTheme.colors.primary,
                        contentColor = Color.White
                    )
                },
                bottomBar = {
                    BottomAppBar(
                        backgroundColor = MaterialTheme.colors.primarySurface
                    ) {
                        Text(
                            text = "Цветовая схема: ${viewModel.currentColorSchemeName}",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color.Black)
                ) {
                    FractalCanvas(viewModel)
                    ControlPanel(viewModel)
                }
            }
        }
    }
}

@Composable
fun FractalCanvas(viewModel: MainViewModel) {
    val textMeasurer = rememberTextMeasurer()

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .fractalMouseHandlers(
                    onRightPanDelta = { delta ->
                        viewModel.handlePan(delta)
                    },
                    onRightClick = { position ->
                        viewModel.showContextMenuAt(position)
                    },
                    onLeftSelectionStart = { offset ->
                        viewModel.onStartSelecting(offset)
                    },
                    onLeftSelectionUpdate = { currentPosition ->
                        viewModel.onSelecting(currentPosition)
                    },
                    onLeftSelectionEnd = {
                        viewModel.onStopSelecting()
                    }
                )
        ) {
            viewModel.paint(this)

            if (viewModel.isSelecting) {
                val (selectionOffset, selectionSize) = viewModel.selectionRect

                if (selectionSize.width > 0 && selectionSize.height > 0) {
                    drawRect(
                        color = Color.White.copy(alpha = 0.2f),
                        topLeft = selectionOffset,
                        size = selectionSize
                    )
                    drawRect(
                        color = Color.White,
                        topLeft = selectionOffset,
                        size = selectionSize,
                        style = Stroke(width = 2f)
                    )


                    val text = "%.0f × %.0f".format(selectionSize.width, selectionSize.height)
                    drawSelectionText(text, selectionOffset, selectionSize, textMeasurer)
                }
            }
        }

        // Индикатор зума
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Card(
                backgroundColor = Color.Black.copy(alpha = 0.7f),
                elevation = 4.dp
            ) {
                Text(
                    text = "Увеличение: ${viewModel.zoomText}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        FractalContextMenu(
            isOpen = viewModel.showContextMenu,
            onDismiss = { viewModel.hideContextMenu() },
            onCopyCoordinates = { viewModel.copyCoordinatesToClipboard() },
            coordinatesInfo = viewModel.contextMenuCoordinates
        )
    }
}

private fun DrawScope.drawSelectionText(
    text: String,
    selectionOffset: Offset,
    selectionSize: Size,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val textStyle = TextStyle(
        color = Color.White,
        fontSize = 14.sp,
        textAlign = TextAlign.Center
    )

    val textLayoutResult = textMeasurer.measure(text, textStyle)

    val textPosition = Offset(
        selectionOffset.x + selectionSize.width / 2 - textLayoutResult.size.width / 2,
        selectionOffset.y + selectionSize.height / 2 - textLayoutResult.size.height / 2
    )

    drawRect(
        color = Color.Black.copy(alpha = 0.5f),
        topLeft = textPosition - Offset(2f, 2f),
        size = Size(
            textLayoutResult.size.width + 4f,
            textLayoutResult.size.height + 4f
        )
    )

    drawText(
        textMeasurer = textMeasurer,
        text = text,
        style = textStyle,
        topLeft = textPosition
    )
}


@Composable
fun ControlPanel(viewModel: MainViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .width(200.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            // Выбор фрактала
            Text("Фракталы:", color = Color.White)
            Button(
                onClick = { viewModel.setMandelbrot() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Мандельброт")
            }
            Button(
                onClick = { viewModel.setJulia() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Жюлиа")
            }
            Button(
                onClick = { viewModel.setBurningShip() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Горящий корабль")
            }
            Button(
                onClick = { viewModel.setTricorn() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Трикорн")
            }

            Spacer(modifier = Modifier.height(16.dp))


            // Выбор цветовой схемы
            Text("Цветовые схемы:", color = Color.White)
            Button(
                onClick = { viewModel.setStandardColors() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Стандартная")
            }
            Button(
                onClick = { viewModel.setFireColors() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Огненная")
            }
            Button(
                onClick = { viewModel.setRainbowColors() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Радужная")
            }
            Button(
                onClick = { viewModel.setCosmicColors() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Космическая")
            }

            Spacer(modifier = Modifier.height(16.dp))


            Button(
                onClick = { viewModel.resetZoom() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red.copy(alpha = 0.7f))
            ) {
                Text("Сбросить зум", color = Color.White)
            }
        }
    }
}
