
package app.mouse

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.*
import kotlinx.coroutines.coroutineScope
import kotlin.time.TimeSource

fun Modifier.fractalMouseHandlers(
    onRightPanDelta: (Offset) -> Unit = {},
    onRightClick: (Offset) -> Unit = {},
    onLeftSelectionStart: (Offset) -> Unit = {},
    onLeftSelectionUpdate: (Offset) -> Unit = {},
    onLeftSelectionEnd: () -> Unit = {}
): Modifier = this.then(
    Modifier.pointerInput(Unit) {
        coroutineScope {
            awaitPointerEventScope {
                var rightPressPos = Offset.Zero
                var rightPressTime = TimeSource.Monotonic.markNow()
                var hasMovedDuringRightPress = false
                var lastRightPos = Offset.Zero

                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.first()

                    when (event.type) {
                        PointerEventType.Press -> {
                            if (event.buttons.isSecondaryPressed && !event.buttons.isPrimaryPressed) {
                                // ПКМ нажата — начинаем отслеживать возможный клик
                                rightPressPos = change.position
                                rightPressTime = TimeSource.Monotonic.markNow()
                                hasMovedDuringRightPress = false
                                lastRightPos = change.position
                                change.consume()
                            } else if (event.buttons.isPrimaryPressed && !event.buttons.isSecondaryPressed) {
                                // ЛКМ — только выделение
                                onLeftSelectionStart(change.position)
                                change.consume()
                            }
                        }

                        PointerEventType.Move -> {
                            if (event.buttons.isPrimaryPressed) {
                                onLeftSelectionUpdate(change.position)
                                change.consume()
                            }

                            if (event.buttons.isSecondaryPressed) {
                                val delta = change.position - lastRightPos
                                if (delta.getDistanceSquared() > 25f) { // > 5px
                                    hasMovedDuringRightPress = true
                                    onRightPanDelta(Offset(delta.x, -delta.y))  // инверсия Y
                                    lastRightPos = change.position
                                }
                                change.consume()
                            }
                        }

                        PointerEventType.Release -> {
                            // ЛКМ отпущена
                            if (!event.buttons.isPrimaryPressed) {
                                onLeftSelectionUpdate(change.position)
                                onLeftSelectionEnd()
                                change.consume()
                            }

                            // ПКМ отпущена — решаем: был ли это настоящий быстрый клик?
                            if (!event.buttons.isSecondaryPressed) {
                                val duration = rightPressTime.elapsedNow()
                                val distance = (change.position - rightPressPos).getDistanceSquared()

                                val isQuickClick = !hasMovedDuringRightPress &&
                                        distance < 100f &&        // < 10px общее смещение
                                        duration.inWholeMilliseconds < 300   // < 300 мс

                                if (isQuickClick) {
                                    onRightClick(change.position)
                                }
                                change.consume()
                            }
                        }
                    }
                }
            }
        }
    }
)