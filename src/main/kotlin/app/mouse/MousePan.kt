package app.mouse

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.isSecondaryPressed
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

object MousePan {
    fun Modifier.rightClickPan(
        onPanDelta: (Offset) -> Unit = {},
        onRightClick: (Offset) -> Unit = {}
    ): Modifier = this.then(
        Modifier.pointerInput(Unit) {
            coroutineScope {
                launch {
                    awaitPointerEventScope {
                        var isPanning = false
                        var startPosition = Offset.Zero
                        var lastPosition = Offset.Zero

                        while (true) {
                            val event = awaitPointerEvent()

                            when (event.type) {
                                PointerEventType.Press -> {
                                    if (event.buttons.isSecondaryPressed) {
                                        isPanning = true
                                        startPosition = event.changes.first().position
                                        lastPosition = startPosition
                                        event.changes.first().consume()
                                    }
                                }

                                PointerEventType.Move -> {
                                    if (isPanning) {
                                        val currentPosition = event.changes.first().position
                                        val delta = currentPosition - lastPosition

                                        if (delta.x != 0f || delta.y != 0f) {
                                            onPanDelta(delta)
                                        }

                                        lastPosition = currentPosition
                                        event.changes.first().consume()
                                    }
                                }

                                PointerEventType.Release -> {
                                    if (isPanning) {
                                        val endPosition = event.changes.first().position

                                        // Простая проверка: если движение < 5px - это клик
                                        val dx = endPosition.x - startPosition.x
                                        val dy = endPosition.y - startPosition.y
                                        // Простая проверка без absoluteValue
                                        val moved = (dx > 5f || dx < -5f) || (dy > 5f || dy < -5f)

                                        if (!moved) {
                                            onRightClick(endPosition)
                                        }

                                        isPanning = false
                                        event.changes.first().consume()
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
}