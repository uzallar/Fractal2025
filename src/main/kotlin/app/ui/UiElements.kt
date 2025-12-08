package app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.onDrag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun PaintPanel(
    modifier: Modifier = Modifier,
    onImageUpdate: (ImageBitmap)->Unit = {},
    onPaint: (DrawScope)->Unit = {},
) {
    val graphicsLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    Canvas(modifier.drawWithContent {
        // call record to capture the content in the graphics layer
        graphicsLayer.record {
            // draw the contents of the composable into the graphics layer
            this@drawWithContent.drawContent()
        }
        // draw the graphics layer on the visible canvas
        drawLayer(graphicsLayer)
        scope.launch { onImageUpdate(graphicsLayer.toImageBitmap()) }
    }) {
        onPaint(this)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectionPanel(
    offset: Offset,
    size: Size,
    modifier: Modifier = Modifier,
    onDragStart: (Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDrag: (Offset) -> Unit = {},
){
    Canvas(modifier = modifier.onDrag(
        onDragStart = onDragStart,
        onDragEnd = onDragEnd,
        onDrag = onDrag,
    )){
        this.drawRect(Color.Blue, offset, size, alpha = 0.2f)
    }
}

private val SoftPink = Color(0xFFF8BBD0)
private val LightPink = Color(0xFFF48FB1)
private val MediumPink = Color(0xFFEC407A)
private val DarkPink = Color(0xFFC2185B)
private val BackgroundPink = Color(0xFFFFF0F5)
private val CardPink = Color(0xFFFFEBEE)
private val TextDark = Color(0xFF311B92)
private val ButtonColor = Color(0xFFD81B60)
private val DisabledPink = Color(0xFFF8C1D9)

@Composable
fun FractalTopAppBar(
    currentFractalName: String,
    onShowHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                "Фрактал: $currentFractalName",
                fontWeight = FontWeight.Bold
            )
        },
        backgroundColor = MediumPink,
        contentColor = Color.White,
        elevation = 8.dp,
        actions = {
            IconButton(
                onClick = {
                    onShowHistory()
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
        },
        modifier = modifier
    )
}

@Composable
fun FractalBottomBar(
    colorSchemeName: String,
    coordinatesText: String,
    modifier: Modifier = Modifier
) {
    BottomAppBar(
        backgroundColor = LightPink.copy(alpha = 0.9f),
        modifier = modifier.height(36.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Левая часть — цветовая схема
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(modifier = Modifier.size(16.dp).background(Color.White, CircleShape).padding(2.dp)) {
                    Box(modifier = Modifier.fillMaxSize().background(MediumPink, CircleShape))
                }
                Text(
                    text = "Цвет: $colorSchemeName",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Правая часть — координаты
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
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
                    text = coordinatesText,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// UiElements.kt

@Composable
fun FractalInfoPanel(
    canUndo: Boolean,
    canRedo: Boolean,
    zoomText: String,
    maxIterations: Int,
    historyInfo: String,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onResetZoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        backgroundColor = CardPink.copy(alpha = 0.9f),
        elevation = 8.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .wrapContentSize()
            .padding(start = 16.dp, top = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Undo
            Button(
                onClick = onUndo,
                enabled = canUndo,
                modifier = Modifier.size(40.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (canUndo) ButtonColor else DisabledPink,
                    disabledBackgroundColor = DisabledPink,
                    contentColor = Color.White,
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                ),
                shape = MaterialTheme.shapes.small,
                elevation = ButtonDefaults.elevation(defaultElevation = 4.dp, pressedElevation = 8.dp, disabledElevation = 0.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад", modifier = Modifier.size(24.dp))
            }

            // Redo
            Button(
                onClick = onRedo,
                enabled = canRedo,
                modifier = Modifier.size(40.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (canRedo) ButtonColor else DisabledPink,
                    disabledBackgroundColor = DisabledPink,
                    contentColor = Color.White,
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                ),
                shape = MaterialTheme.shapes.small,
                elevation = ButtonDefaults.elevation(defaultElevation = 4.dp, pressedElevation = 8.dp, disabledElevation = 0.dp)
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Вперёд", modifier = Modifier.size(24.dp))
            }

            Box(modifier = Modifier.width(1.dp).height(30.dp).background(SoftPink.copy(alpha = 0.5f)))

            // Reset zoom
            Button(
                onClick = onResetZoom,
                modifier = Modifier.size(40.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = ButtonColor, contentColor = Color.White),
                shape = MaterialTheme.shapes.small,
                elevation = ButtonDefaults.elevation(defaultElevation = 4.dp, pressedElevation = 8.dp)
            ) {
                Text("\uD83D\uDDD1\uFE0F", fontSize = 18.sp)
            }

            Column(
                modifier = Modifier.padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(12.dp).background(MediumPink, CircleShape)) {
                        Box(modifier = Modifier.fillMaxSize().padding(3.dp).background(Color.White, CircleShape))
                    }
                    Text("Увеличение: $zoomText", color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Text("Итерации: $maxIterations", color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text("История: $historyInfo", color = TextDark.copy(alpha = 0.7f), fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun FractalControlPanel(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        backgroundColor = CardPink.copy(alpha = 0.9f),
        elevation = 8.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Фракталы
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Фракталы:", color = TextDark, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                FractalButton("Мандельброт") { viewModel.setMandelbrot() }
                FractalButton("Жюлиа") { viewModel.setJulia() }
                FractalButton("Трикорн") { viewModel.setTricorn() }

                Button(
                    onClick = { /* TODO: Загрузить фрактал */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = ButtonColor, contentColor = Color.White),
                    shape = MaterialTheme.shapes.small,
                    elevation = ButtonDefaults.elevation(4.dp, 8.dp)
                ) {
                    Text("Загрузить фрактал", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }

            Divider(color = SoftPink, thickness = 1.dp)

            // Цветовые схемы
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Цветовые схемы:", color = TextDark, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                ColorSchemeButton("Стандартная") { viewModel.setStandardColors() }
                ColorSchemeButton("Огненная") { viewModel.setFireColors() }
                ColorSchemeButton("Радужная") { viewModel.setRainbowColors() }
                ColorSchemeButton("Ледяная") { viewModel.setCosmicColors() }
            }

            Divider(color = SoftPink, thickness = 1.dp)

            // Сохранение
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Сохранить как:", color = TextDark, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { }, modifier = Modifier.weight(1f).height(40.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = ButtonColor, contentColor = Color.White)) {
                        Text("Fractal", fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                    Button(onClick = { }, modifier = Modifier.weight(1f).height(40.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = ButtonColor, contentColor = Color.White)) {
                        Text("JPG", fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            Divider(color = SoftPink, thickness = 1.dp)

            // Остальные кнопки
            Button(
                onClick = { viewModel.randomJump() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = ButtonColor, contentColor = Color.White)
            ) {
                Text("Случайный прыжок", fontSize = 14.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
            }

            Divider(color = SoftPink, thickness = 1.dp)

            Button(
                onClick = { /* TODO: экскурсия */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = ButtonColor, contentColor = Color.White)
            ) {
                Text("Экскурсия по фракталу", fontSize = 14.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
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

@Composable
fun HistoryDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    detailedHistory: String,
    historyInfo: String,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = "История", tint = MediumPink)
                Text(
                    "История действий ($historyInfo)",
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
                    text = detailedHistory,
                    fontSize = 13.sp,
                    color = TextDark.copy(alpha = 0.8f),
                    modifier = Modifier.padding(12.dp)
                )
            }
        },
        buttons = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onClearHistory()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = ButtonColor, contentColor = Color.White),
                    elevation = ButtonDefaults.elevation(4.dp, 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Очистить", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Очистить историю", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = ButtonColor, contentColor = Color.White),
                    elevation = ButtonDefaults.elevation(4.dp, 8.dp)
                ) {
                    Text("Закрыть", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        },
        modifier = modifier.width(400.dp),
        shape = MaterialTheme.shapes.medium,
        backgroundColor = BackgroundPink
    )
}