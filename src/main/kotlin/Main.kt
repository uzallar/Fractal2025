import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
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
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.mouse.FractalContextMenu
import app.mouse.fractalMouseHandlers
import app.painting.convertation.Converter
import app.ui.FractalBottomBar
import app.ui.FractalControlPanel
import app.ui.FractalInfoPanel
import app.ui.FractalTopAppBar
import app.ui.HistoryDialog
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
        }

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
                        // Ctrl+Y
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
                        FractalTopAppBar(
                            currentFractalName = viewModel.currentFractalName,
                            onShowHistory = {
                                viewModel.refreshDetailedHistory()
                                showHistoryDialog = true
                            }
                        )
                    },
                    bottomBar = {
                        val coordsText = remember(globalMousePosition, viewModel.currentPlain) {
                            val pos = globalMousePosition
                            val plain = viewModel.currentPlain
                            if (pos != null) {
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

                        FractalBottomBar(
                            colorSchemeName = viewModel.currentColorSchemeName,
                            coordinatesText = coordsText
                        )
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
                        FractalControlPanel(
                            viewModel = viewModel,
                            modifier = Modifier
                                .width(220.dp)
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                                .align(Alignment.TopEnd)
                        )

                        HistoryDialog(
                            isOpen = showHistoryDialog,
                            onDismiss = { showHistoryDialog = false },
                            detailedHistory = viewModel.detailedHistory,
                            historyInfo = viewModel.historyInfo,
                            onClearHistory = { viewModel.resetToInitial() }
                        )
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

        FractalInfoPanel(
            canUndo = viewModel.canUndo,
            canRedo = viewModel.canRedo,
            zoomText = viewModel.zoomText,
            maxIterations = viewModel.maxIterations,
            historyInfo = viewModel.historyInfo,
            onUndo = { viewModel.undo() },
            onRedo = { viewModel.redo() },
            onResetZoom = { viewModel.resetZoom() },
            modifier = Modifier.align(Alignment.TopStart)
        )

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