package app.utils

import androidx.compose.ui.geometry.Offset
import app.painting.convertation.Plain
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class FractalSaving (var fractalName: String,
    var color: String, var plain: Plain,var zoomLevel : Double
) : Serializable {
    constructor() : this(String(), "", Plain(0.0, 0.0, 0.0, 0.0),0.0)
    /**
     * Сохранение объекта в файл с расширением .fractal
     */
    fun saveFractalObject( file: File?) {
        file?.let {
            ObjectOutputStream(FileOutputStream(it)).use { out ->
                out.writeObject(this)
            }
        }
    }

    /**
     * Загрузка объекта из файла .fractal
     */
    fun loadFractalObject(path:String,filename:String) {
        var res = ""
        if(path.endsWith('/')) res = "$path$filename"
        else res = "$path/$filename"
        ObjectInputStream(FileInputStream(res)).use { input ->
            val l = input.readObject() as FractalSaving
            fractalName = l.fractalName
            color = l.color
            plain = l.plain
            zoomLevel = l.zoomLevel
        }

    }

}