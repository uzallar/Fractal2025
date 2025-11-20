package app.painting.convertation

data class Plain(
    var xMin: Double,
    var xMax: Double,
    var yMin: Double,
    var yMax: Double,
    var width: Float = 0f,
    var height: Float = 0f,
){
    val xDen get() = width / (xMax - xMin)
    val yDen get() = height / (yMax - yMin)
}
