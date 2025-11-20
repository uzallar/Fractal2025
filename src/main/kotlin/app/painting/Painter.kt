package app.painting

import androidx.compose.ui.graphics.drawscope.DrawScope

interface Painter {
    suspend fun paint(scope: DrawScope)
}
