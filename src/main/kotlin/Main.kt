package app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerMoveFilter
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
import app.painting.convertation.Converter
import app.viewmodels.MainViewModel

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    val viewModel = remember { MainViewModel() }

    var showHistoryDialog by remember { mutableStateOf(false) }
    var globalMousePosition by remember { mutableStateOf<Offset?>(null)}

    Window(
        onCloseRequest = {
            viewModel.onAppClosing()
            exitApplication()
        },
        title = "Фракталы"
    ) {
        MaterialTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Фрактал: ${viewModel.currentFractalName}") },
                        backgroundColor = MaterialTheme.colors.primary,
                        contentColor = Color.White,
                        actions = {
                            TextButton(
                                onClick = {
                                    viewModel.refreshDetailedHistory()
                                    showHistoryDialog = true
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Информация",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )

                                }
                            }
                        }
                    )
                },
                bottomBar = {
                    BottomAppBar(
                        backgroundColor = MaterialTheme.colors.primarySurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = "Цветовая схема: ${viewModel.currentColorSchemeName}",
                                    color = Color.White
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                val coordsText = remember(globalMousePosition, viewModel.currentPlain) {
                                    val pos = globalMousePosition
                                    val plain = viewModel.currentPlain
                                    if (pos != null && plain != null) {
                                        try {
                                            val x = Converter.xScr2Crt(pos.x, plain)
                                            val y = Converter.yScr2Crt(pos.y, plain)
                                            "x: ${"%.6f".format(x)}, y: ${"%.6f".format(y)}"
                                        } catch (e: Exception) {
                                            "x: -, y: -"
                                        }
                                    } else {
                                        "x: -, y: -"
                                    }
                                }

                                Text(
                                    text = coordsText,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color.Black)
                        .pointerMoveFilter(
                            onMove = { offset ->
                                globalMousePosition = offset
                                false
                            },
                            onExit = {
                                globalMousePosition = null
                                false
                            }
                        )
                ) {
                    FractalCanvas(viewModel)
                    ControlPanel(viewModel)

                    UndoRedoButtons(viewModel)


                    // Диалог истории
                    if (showHistoryDialog) {
                        AlertDialog(
                            onDismissRequest = { showHistoryDialog = false },
                            title = {
                                Text("История действий (${viewModel.historyInfo})")
                            },
                            text = {
                                Column {
                                    Text(
                                        text = viewModel.detailedHistory,
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            },
                            buttons = {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Кнопка "Новый проект" в диалоге
                                    Button(
                                        onClick = {
                                            viewModel.resetToInitial()
                                            showHistoryDialog = false
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            backgroundColor = Color.Blue.copy(alpha = 0.7f)
                                        )
                                    ) {
                                        Text("Очистить историю")
                                    }

                                    Button(
                                        onClick = { showHistoryDialog = false },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Закрыть")
                                    }
                                }
                            },
                            modifier = Modifier.width(400.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun UndoRedoButtons(viewModel: MainViewModel) {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 80.dp)
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Button(
                onClick = { viewModel.undo() },
                enabled = viewModel.canUndo,
                modifier = Modifier
                    .width(100.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (viewModel.canUndo) MaterialTheme.colors.primary else Color.Gray,
                    disabledBackgroundColor = Color.DarkGray
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("<", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Назад", fontSize = 14.sp)
                }
            }

            Button(
                onClick = { viewModel.redo() },
                enabled = viewModel.canRedo,
                modifier = Modifier
                    .width(100.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (viewModel.canRedo) MaterialTheme.colors.primary else Color.Gray,
                    disabledBackgroundColor = Color.DarkGray
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Вперёд", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(">", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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

        // Индикатор вверху слева
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Card(
                backgroundColor = Color.Black.copy(alpha = 0.7f),
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(
                        text = "Увеличение: ${viewModel.zoomText}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Итерации: ${viewModel.maxIterations}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "История: ${viewModel.historyInfo}",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
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

            // Управление итерациями
            Text("Итерации:", color = Color.White)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = { viewModel.decreaseIterations() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("-")
                }
                Button(
                    onClick = { viewModel.increaseIterations() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+")
                }
            }

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

            // Кнопка сброса зума
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