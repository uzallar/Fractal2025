// app/ui/ExcursionPanel.kt
package app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.excursion.Excursion
import app.excursion.ExcursionPoint
import app.viewmodels.MainViewModel

val ExcursionBlue = Color(0xFF2196F3)
private val ExcursionLightBlue = Color(0xFFE3F2FD)
private val ExcursionDarkBlue = Color(0xFF0D47A1)

@Composable
fun ExcursionPanel(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var showPointDialog by remember { mutableStateOf(false) }
    var editingPoint by remember { mutableStateOf<ExcursionPoint?>(null) }
    var pointTitle by remember { mutableStateOf("") }
    var pointDescription by remember { mutableStateOf("") }

    Card(
        backgroundColor = ExcursionLightBlue.copy(alpha = 0.95f),
        elevation = 8.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.width(320.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Заголовок
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Экскурсия",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ExcursionDarkBlue
                )

                IconButton(
                    onClick = { viewModel.toggleExcursionMode() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        if (viewModel.isExcursionMode) Icons.Default.Close else Icons.Default.Close,
                        contentDescription = "Режим экскурсии",
                        tint = ExcursionBlue
                    )
                }
            }

            if (!viewModel.isExcursionMode) {
                Text(
                    text = "Включите режим экскурсии, чтобы добавлять контрольные точки",
                    fontSize = 13.sp,
                    color = ExcursionDarkBlue.copy(alpha = 0.7f)
                )
            } else {
                // Информация об экскурсии
                val excursion = viewModel.currentExcursion
                if (excursion != null) {
                    ExcursionInfo(excursion = excursion)
                }

                // Кнопки управления
                ExcursionControls(viewModel = viewModel)

                // Список точек
                ExcursionPointsList(
                    points = viewModel.getExcursionPoints(),
                    currentIndex = viewModel.currentExcursionPointIndex,
                    onPointClick = { point ->
                        editingPoint = point
                        pointTitle = point.title
                        pointDescription = point.description
                        showPointDialog = true
                    },
                    onRemovePoint = { pointId ->
                        viewModel.removeExcursionPoint(pointId)
                    }
                )

                // Кнопка добавления точки
                Button(
                    onClick = {
                        viewModel.addCurrentViewToExcursion()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = ExcursionBlue,
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("Добавить текущий вид")
                    }
                }

                // Сохранение экскурсии
                if (viewModel.getExcursionPoints().isNotEmpty()) {
                    Button(
                        onClick = {
                            viewModel.saveExcursion()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = ExcursionDarkBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Сохранить экскурсию")
                    }
                }
            }
        }
    }

    // Диалог редактирования точки
    if (showPointDialog) {
        AlertDialog(
            onDismissRequest = { showPointDialog = false },
            title = { Text("Редактировать точку", color = ExcursionDarkBlue) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = pointTitle,
                        onValueChange = { pointTitle = it },
                        label = { Text("Название точки") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = pointDescription,
                        onValueChange = { pointDescription = it },
                        label = { Text("Описание") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            buttons = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showPointDialog = false }) {
                        Text("Отмена")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            // TODO: Обновить точку
                            showPointDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = ExcursionBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Сохранить")
                    }
                }
            }
        )
    }
}

@Composable
private fun ExcursionInfo(excursion: Excursion) {
    Card(
        backgroundColor = Color.White,
        elevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = excursion.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ExcursionDarkBlue
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${excursion.pointCount} точек",
                    fontSize = 13.sp,
                    color = ExcursionDarkBlue.copy(alpha = 0.7f)
                )

                Text(
                    text = "Длительность: ${excursion.formattedDuration}",
                    fontSize = 13.sp,
                    color = ExcursionDarkBlue.copy(alpha = 0.7f)
                )
            }

            if (excursion.description.isNotEmpty()) {
                Text(
                    text = excursion.description,
                    fontSize = 12.sp,
                    color = ExcursionDarkBlue.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ExcursionControls(viewModel: MainViewModel) {
    val isPlaying = viewModel.isPlayingExcursion
    val hasPoints = viewModel.getExcursionPoints().isNotEmpty()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Кнопка назад
        IconButton(
            onClick = { viewModel.previousExcursionPoint() },
            enabled = hasPoints && viewModel.currentExcursionPointIndex > 0,
            modifier = Modifier.weight(1f).height(48.dp)
        ) {
            Icon(Icons.Default.Done, contentDescription = "Предыдущая точка")
        }

        // Кнопка play/pause
        IconButton(
            onClick = {
                if (isPlaying) {
                    viewModel.pauseExcursion()
                } else {
                    viewModel.startExcursion()
                }
            },
            enabled = hasPoints,
            modifier = Modifier.weight(2f).height(48.dp)
        ) {
            Icon(
                if (isPlaying) Icons.Default.Phone else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Пауза" else "Воспроизвести",
                modifier = Modifier.size(32.dp)
            )
        }

        // Кнопка вперед
        IconButton(
            onClick = { viewModel.nextExcursionPoint() },
            enabled = hasPoints && viewModel.currentExcursionPointIndex < viewModel.getExcursionPoints().size - 1,
            modifier = Modifier.weight(1f).height(48.dp)
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = "Следующая точка")
        }

        // Кнопка остановки
        IconButton(
            onClick = { viewModel.stopExcursion() },
            enabled = hasPoints,
            modifier = Modifier.weight(1f).height(48.dp)
        ) {
            Icon(Icons.Default.Lock, contentDescription = "Стоп")
        }
    }
}

@Composable
private fun ExcursionPointsList(
    points: List<ExcursionPoint>,
    currentIndex: Int,
    onPointClick: (ExcursionPoint) -> Unit,
    onRemovePoint: (String) -> Unit
) {
    if (points.isEmpty()) {
        Text(
            text = "Добавьте точки, чтобы создать экскурсию",
            fontSize = 13.sp,
            color = ExcursionDarkBlue.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    } else {
        LazyColumn(
            modifier = Modifier.height(200.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(points, key = { it.id }) { point ->
                val isCurrent = points.indexOf(point) == currentIndex

                Card(
                    backgroundColor = if (isCurrent) ExcursionBlue.copy(alpha = 0.1f) else Color.White,
                    elevation = if (isCurrent) 4.dp else 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPointClick(point) }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isCurrent) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(ExcursionBlue)
                                    )
                                }

                                Text(
                                    text = point.title.ifEmpty { "Точка ${points.indexOf(point) + 1}" },
                                    fontSize = 14.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = ExcursionDarkBlue
                                )
                            }

                            if (point.description.isNotEmpty()) {
                                Text(
                                    text = point.description,
                                    fontSize = 12.sp,
                                    color = ExcursionDarkBlue.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Text(
                                text = "${point.fractalName} • ${point.colorSchemeName}",
                                fontSize = 11.sp,
                                color = ExcursionDarkBlue.copy(alpha = 0.5f)
                            )
                        }

                        IconButton(
                            onClick = { onRemovePoint(point.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Удалить",
                                tint = MaterialTheme.colors.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}