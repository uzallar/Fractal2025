package app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.onDrag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
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