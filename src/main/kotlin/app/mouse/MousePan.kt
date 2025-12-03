package app.mouse

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

enum class MouseState {
    IDLE,
    RIGHT_DRAG,
    LEFT_SELECTION
}

fun Modifier.fractalMouseHandlers(
    onRightPanDelta: (Offset) -> Unit = {},
    onRightClick: (Offset) -> Unit = {},
    onLeftSelectionStart: (Offset) -> Unit = {},
    onLeftSelectionUpdate: (Offset) -> Unit = {},
    onLeftSelectionEnd: () -> Unit = {}
): Modifier = this.then(
    Modifier.pointerInput(Unit) {
        coroutineScope {
            launch {
                awaitPointerEventScope {
                    var mouseState = MouseState.IDLE
                    var startPosition = Offset.Zero

                    while (true) {
                        val event = awaitPointerEvent()

                        when (event.type) {
                            PointerEventType.Press -> {
                                // Левая кнопка для выделения
                                if (event.buttons.isPrimaryPressed && !event.buttons.isSecondaryPressed) {
                                    println("DEBUG: Left button PRESSED for selection")
                                    mouseState = MouseState.LEFT_SELECTION
                                    startPosition = event.changes.first().position
                                    onLeftSelectionStart(startPosition)
                                    onLeftSelectionUpdate(startPosition)
                                    event.changes.first().consume()
                                }
                                // Правая кнопка для панорамирования
                                else if (event.buttons.isSecondaryPressed && !event.buttons.isPrimaryPressed) {
                                    println("DEBUG: Right button PRESSED for panning")
                                    mouseState = MouseState.RIGHT_DRAG
                                    startPosition = event.changes.first().position
                                    event.changes.first().consume()
                                }
                            }

                            PointerEventType.Move -> {
                                val currentPosition = event.changes.first().position

                                when (mouseState) {
                                    MouseState.RIGHT_DRAG -> {
                                        val delta = currentPosition - startPosition

                                        if (delta.x != 0f || delta.y != 0f) {
                                            println("DEBUG: Panning delta = $delta")
                                            onRightPanDelta(delta)
                                            startPosition = currentPosition
                                        }

                                        event.changes.first().consume()
                                    }
                                    MouseState.LEFT_SELECTION -> {
                                        // Передаем текущую позицию мыши (не дельту)
                                        println("DEBUG: Selection update at $currentPosition")
                                        onLeftSelectionUpdate(currentPosition)
                                        event.changes.first().consume()
                                    }
                                    else -> {}
                                }
                            }

                            PointerEventType.Release -> {
                                when (mouseState) {
                                    MouseState.RIGHT_DRAG -> {
                                        val endPosition = event.changes.first().position
                                        val dx = endPosition.x - startPosition.x
                                        val dy = endPosition.y - startPosition.y
                                        val distanceSquared = dx * dx + dy * dy

                                        println("DEBUG: Right button RELEASED, distance^2 = $distanceSquared")

                                        if (distanceSquared < 100f) { // 10px в квадрате
                                            println("DEBUG: Right CLICK detected at $endPosition")
                                            onRightClick(endPosition)
                                        }

                                        mouseState = MouseState.IDLE
                                        event.changes.first().consume()
                                    }
                                    MouseState.LEFT_SELECTION -> {
                                        val endPosition = event.changes.first().position
                                        println("DEBUG: Left button RELEASED at $endPosition, ending selection")
                                        onLeftSelectionUpdate(endPosition)
                                        onLeftSelectionEnd()
                                        mouseState = MouseState.IDLE
                                        event.changes.first().consume()
                                    }
                                    else -> {}
                                }
                            }

                            else -> {}
                        }
                    }
                }
            }
        }
    }
)