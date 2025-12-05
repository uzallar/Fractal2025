import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.input.key.isCtrlPressed
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

private val SoftPink = Color(0xFFF8BBD0)
private val LightPink = Color(0xFFF48FB1)
private val MediumPink = Color(0xFFEC407A)
private val DarkPink = Color(0xFFC2185B)
private val BackgroundPink = Color(0xFFFFF0F5)
private val CardPink = Color(0xFFFFEBEE)
private val TextDark = Color(0xFF311B92)
private val ButtonColor = Color(0xFFD81B60)
private val DisabledPink = Color(0xFFF8C1D9)
private val BrightPurple = Color(0xFF7B1FA2)

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
        // Обработка горячих клавиш
        LaunchedEffect(Unit) {
            // Можно также использовать onKeyEvent в Modifier,
            // но для глобальных сочетаний клавиш лучше использовать Window
        }

        // Добавляем обработку клавиш к окну через onPreviewKeyEvent
        val keyState = remember { mutableStateOf(true) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onPreviewKeyEvent { keyEvent ->
                    when {
                        // Ctrl+Z - отмена
                        keyEvent.key == Key.Z && keyEvent.isCtrlPressed && keyEvent.type == KeyEventType.KeyDown -> {
                            viewModel.undo()
                            true
                        }
                        // Ctrl+Y или Ctrl+Shift+Z - повтор
                        (keyEvent.key == Key.Y && keyEvent.isCtrlPressed && keyEvent.type == KeyEventType.KeyDown) ||
                                (keyEvent.key == Key.Z && keyEvent.isCtrlPressed && keyEvent.isShiftPressed && keyEvent.type == KeyEventType.KeyDown) -> {
                            viewModel.redo()
                            true
                        }
                        else -> false
                    }
                }
        ) {
            MaterialTheme(
                colors = MaterialTheme.colors.copy(
                    primary = MediumPink,
                    primaryVariant = DarkPink,
                    secondary = LightPink,
                    background = BackgroundPink,
                    surface = CardPink,
                    onPrimary = Color.White,
                    onSecondary = Color.White,
                    onBackground = TextDark,
                    onSurface = TextDark
                )
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    "Фрактал: ${viewModel.currentFractalName}",
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            backgroundColor = MediumPink,
                            contentColor = Color.White,
                            elevation = 8.dp,
                            actions = {
                                IconButton(
                                    onClick = {
                                        viewModel.refreshDetailedHistory()
                                        showHistoryDialog = true
                                    },
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "История",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        )
                    },
                    bottomBar = {
                        BottomAppBar(
                            backgroundColor = LightPink.copy(alpha = 0.9f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .background(Color.White, androidx.compose.foundation.shape.CircleShape)
                                                .padding(2.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(MediumPink, androidx.compose.foundation.shape.CircleShape)
                                            )
                                        }
                                        Text(
                                            text = "Цвет: ${viewModel.currentColorSchemeName}",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
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

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Place,
                                            contentDescription = "Координаты",
                                            tint = Color.White.copy(alpha = 0.9f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = coordsText,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .background(BackgroundPink)
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

                        if (showHistoryDialog) {
                            AlertDialog(
                                onDismissRequest = { showHistoryDialog = false },
                                title = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = "История",
                                            tint = MediumPink
                                        )
                                        Text(
                                            "История действий (${viewModel.historyInfo})",
                                            color = TextDark,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                },
                                text = {
                                    Card(
                                        backgroundColor = CardPink,
                                        elevation = 4.dp,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = viewModel.detailedHistory,
                                            fontSize = 13.sp,
                                            color = TextDark.copy(alpha = 0.8f),
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                },
                                buttons = {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.resetToInitial()
                                                showHistoryDialog = false
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(40.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                backgroundColor = ButtonColor,
                                                contentColor = Color.White
                                            ),
                                            elevation = ButtonDefaults.elevation(
                                                defaultElevation = 4.dp,
                                                pressedElevation = 8.dp
                                            )
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.Refresh,
                                                    contentDescription = "Очистить",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    "Очистить историю",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = { showHistoryDialog = false },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(40.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                backgroundColor = ButtonColor,
                                                contentColor = Color.White
                                            ),
                                            elevation = ButtonDefaults.elevation(
                                                defaultElevation = 4.dp,
                                                pressedElevation = 8.dp
                                            )
                                        ) {
                                            Text(
                                                "Закрыть",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.width(400.dp),
                                shape = MaterialTheme.shapes.medium,
                                backgroundColor = BackgroundPink
                            )
                        }
                    }
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
                        color = SoftPink.copy(alpha = 0.3f),
                        topLeft = selectionOffset,
                        size = selectionSize
                    )
                    drawRect(
                        color = MediumPink,
                        topLeft = selectionOffset,
                        size = selectionSize,
                        style = Stroke(width = 2f)
                    )

                    val text = "%.0f × %.0f".format(selectionSize.width, selectionSize.height)
                    drawSelectionText(text, selectionOffset, selectionSize, textMeasurer)
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    backgroundColor = CardPink.copy(alpha = 0.9f),
                    elevation = 8.dp,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(MediumPink, androidx.compose.foundation.shape.CircleShape)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp)
                                        .background(Color.White, androidx.compose.foundation.shape.CircleShape)
                                )
                            }
                            Text(
                                text = "Увеличение: ${viewModel.zoomText}",
                                color = TextDark,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "Итерации: ${viewModel.maxIterations}",
                            color = TextDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "История: ${viewModel.historyInfo}",
                            color = TextDark.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }

                Card(
                    backgroundColor = CardPink.copy(alpha = 0.9f),
                    elevation = 8.dp,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.wrapContentSize()
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { viewModel.undo() },
                            enabled = viewModel.canUndo,
                            modifier = Modifier.size(40.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (viewModel.canUndo) ButtonColor else DisabledPink,
                                contentColor = Color.White,
                                disabledBackgroundColor = DisabledPink,
                                disabledContentColor = Color.White.copy(alpha = 0.5f)
                            ),
                            shape = MaterialTheme.shapes.small,
                            elevation = ButtonDefaults.elevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp,
                                disabledElevation = 0.dp
                            )
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Назад",
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Button(
                            onClick = { viewModel.redo() },
                            enabled = viewModel.canRedo,
                            modifier = Modifier.size(40.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (viewModel.canRedo) ButtonColor else DisabledPink,
                                contentColor = Color.White,
                                disabledBackgroundColor = DisabledPink,
                                disabledContentColor = Color.White.copy(alpha = 0.5f)
                            ),
                            shape = MaterialTheme.shapes.small,
                            elevation = ButtonDefaults.elevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp,
                                disabledElevation = 0.dp
                            )
                        ) {
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "Вперёд",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
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
        color = MediumPink,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Medium
    )

    val textLayoutResult = textMeasurer.measure(text, textStyle)

    val textPosition = Offset(
        selectionOffset.x + selectionSize.width / 2 - textLayoutResult.size.width / 2,
        selectionOffset.y + selectionSize.height / 2 - textLayoutResult.size.height / 2
    )

    drawRect(
        color = CardPink.copy(alpha = 0.9f),
        topLeft = textPosition - Offset(4f, 4f),
        size = Size(
            textLayoutResult.size.width + 8f,
            textLayoutResult.size.height + 8f
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
        modifier = Modifier.fillMaxSize()
    ) {
        Card(
            backgroundColor = CardPink.copy(alpha = 0.9f),
            elevation = 8.dp,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .width(220.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "Фракталы:",
                        color = TextDark,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                    FractalButton(
                        text = "Мандельброт",
                        onClick = { viewModel.setMandelbrot() }
                    )
                    FractalButton(
                        text = "Жюлиа",
                        onClick = { viewModel.setJulia() }
                    )
                    FractalButton(
                        text = "Горящий корабль",
                        onClick = { viewModel.setBurningShip() }
                    )
                    FractalButton(
                        text = "Трикорн",
                        onClick = { viewModel.setTricorn() }
                    )
                }

                Divider(color = SoftPink, thickness = 1.dp)

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "Цветовые схемы:",
                        color = TextDark,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                    ColorSchemeButton(
                        text = "Стандартная",
                        onClick = { viewModel.setStandardColors() }
                    )
                    ColorSchemeButton(
                        text = "Огненная",
                        onClick = { viewModel.setFireColors() }
                    )
                    ColorSchemeButton(
                        text = "Радужная",
                        onClick = { viewModel.setRainbowColors() }
                    )
                    ColorSchemeButton(
                        text = "Космическая",
                        onClick = { viewModel.setCosmicColors() }
                    )
                }

                Divider(color = SoftPink, thickness = 1.dp)

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "Сохранить как:",
                        color = TextDark,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = ButtonColor,
                                contentColor = Color.White
                            ),
                            shape = MaterialTheme.shapes.small,
                            elevation = ButtonDefaults.elevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            Text("Fractal",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Button(
                            onClick = { },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = ButtonColor,
                                contentColor = Color.White
                            ),
                            shape = MaterialTheme.shapes.small,
                            elevation = ButtonDefaults.elevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            Text("JPG",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Divider(color = SoftPink, thickness = 1.dp)

                Button(
                    onClick = {
                        // TODO: Реализовать логику экскурсии по фракталу
                        // viewModel.startFractalTour()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = ButtonColor,
                        contentColor = Color.White
                    ),
                    shape = MaterialTheme.shapes.small,
                    elevation = ButtonDefaults.elevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Text(
                        "Экскурсия по фракталу",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Divider(color = SoftPink, thickness = 1.dp)

                Button(
                    onClick = { viewModel.resetZoom() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = ButtonColor,
                        contentColor = Color.White
                    ),
                    shape = MaterialTheme.shapes.small,
                    elevation = ButtonDefaults.elevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Сбросить",
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "Сбросить зум",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FractalButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = ButtonColor,
            contentColor = Color.White
        ),
        shape = MaterialTheme.shapes.small,
        elevation = ButtonDefaults.elevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Text(
            text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
fun ColorSchemeButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = ButtonColor,
            contentColor = Color.White
        ),
        shape = MaterialTheme.shapes.small,
        elevation = ButtonDefaults.elevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier.size(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.3f), androidx.compose.foundation.shape.CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color.White, androidx.compose.foundation.shape.CircleShape)
                        .align(Alignment.Center)
                )
            }
            Text(
                text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}