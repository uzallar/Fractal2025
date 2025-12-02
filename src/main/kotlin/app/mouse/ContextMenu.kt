package app.mouse

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.rememberCursorPositionProvider

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
            Box(
                modifier = Modifier
                    .shadow(4.dp)
                    .background(Color.White)
            ) {
                Column {
                    if (coordinatesInfo.isNotEmpty()) {
                        Text(
                            text = coordinatesInfo,
                            modifier = Modifier
                                .background(Color(0xFFF5F5F5))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .fillMaxWidth(),
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clickable {
                                onCopyCoordinates()
                                onDismiss()
                            }
                            .background(MaterialTheme.colors.surface)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "📋 Копировать координаты",
                            color = MaterialTheme.colors.onSurface
                        )
                    }
                }
            }
        }
    }
}