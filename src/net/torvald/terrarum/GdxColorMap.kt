package net.torvald.terrarum

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import javax.naming.OperationNotSupportedException

/**
 * Created by minjaesong on 2017-06-17.
 */

class GdxColorMap {

    constructor(imageFile: FileHandle) {
        AppLoader.printdbg(this, "Loading colormap from ${imageFile.name()}")

        val pixmap = Pixmap(imageFile)
        width = pixmap.width
        height = pixmap.height
        is2D = pixmap.height > 1

        data = kotlin.IntArray(pixmap.width * pixmap.height) {
            pixmap.getPixel(it % pixmap.width, it / pixmap.width)
        }

        pixmap.dispose()
    }

    constructor(pixmap: Pixmap, disposePixmap: Boolean = true) {
        width = pixmap.width
        height = pixmap.height
        is2D = pixmap.height > 1

        data = kotlin.IntArray(pixmap.width * pixmap.height) {
            pixmap.getPixel(it % pixmap.width, it / pixmap.width)
        }

        if (disposePixmap) pixmap.dispose()
    }

    constructor(color: Color) {
        data = intArrayOf(color.toIntBits())
        width = 1
        height = 1
        is2D = false
    }

    constructor(gradStart: Color, gradEnd: Color) {
        data = intArrayOf(gradStart.toIntBits(), gradEnd.toIntBits())
        width = 1
        height = 2
        is2D = true
    }

    private val data: IntArray
    val width: Int
    val height: Int
    val is2D: Boolean



    fun get(x: Int, y: Int): Color = Color(data[y * width + x])
    operator fun get(x: Int): Color = if (is2D) throw OperationNotSupportedException("This is 2D color map") else Color(data[x])

    fun getRaw(x: Int, y: Int): RGBA8888 = data[y * width + x]
    fun getRaw(x: Int): RGBA8888 = if (is2D) throw OperationNotSupportedException("This is 2D color map") else data[x]

    override fun toString(): String {
        val sb = StringBuilder()

        sb.append("ColorMap ${width}x$height:\n")

        var yi = 0
        var xi = 0
        for (y in ((0 until height).take(2) + (0 until height).toList().takeLast(2)).distinct()) {

            if (y - yi > 1) {
                sb.append(when (width) {
                    in 1..4 -> ".......... ".repeat(width) + '\n'
                    else -> ".......... .......... ... .......... .......... \n"
                }
                )
            }

            for (x in ((0 until width).take(2) + (0 until width).toList().takeLast(2)).distinct()) {
                if (x - xi > 1) {
                    sb.append("... ")
                }

                sb.append("0x")
                sb.append(getRaw(x, y).toLong().and(0xFFFFFFFF).toString(16).toUpperCase().padStart(8, '0'))
                sb.append(' ')

                xi = x
            }

            sb.append('\n')

            yi = y
        }

        return sb.toString()
    }
}