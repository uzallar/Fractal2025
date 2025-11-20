package app.painting.convertation

object Converter {

    fun xCrt2Scr(x: Double, p: Plain) =
        ((x - p.xMin) * p.xDen).toFloat().coerceIn(-p.width..p.width*2f)

    fun yCrt2Scr(y: Double, p: Plain) =
        ((p.yMax - y) * p.yDen).toFloat().coerceIn(-p.height..p.height*2f)

    fun xScr2Crt(x: Float, p: Plain) =
        x / p.xDen + p.xMin

    fun yScr2Crt(y: Float, p: Plain) =
        p.yMax - y / p.yDen
}