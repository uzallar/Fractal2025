package app.mouse

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.rememberCursorPositionProvider

private val CardPink = Color(0xFFFFEBEE)
private val SoftPink = Color(0xFFF8BBD0)
private val ButtonColor = Color(0xFFD81B60)
private val TextDark = Color(0xFF311B92)

@Composable
fun FractalContextMenu(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onCopyCoordinates: () -> Unit,
    coordinatesInfo: String = ""
) {
    if (isOpen) {
        Popup(
            popupPositionProvider = rememberCursorPositionProvider(),
            onDismissRequest = onDismiss
        ) {
            Card(
                modifier = Modifier
                    .shadow(8.dp)
                    .width(260.dp),
                backgroundColor = CardPink,
                elevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    // Блок с координатами
                    Card(
                        backgroundColor = SoftPink,
                        elevation = 2.dp,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "Координаты:",
                                fontSize = 13.sp,
                                color = TextDark,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = coordinatesInfo,
                                fontSize = 12.sp,
                                color = TextDark,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Кнопка копирования
                    Button(
                        onClick = {
                            onCopyCoordinates()
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
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
                            text = "Копировать координаты",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}