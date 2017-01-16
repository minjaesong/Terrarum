package net.torvald.aa

import net.torvald.terrarum.virtualcomputer.terminal.Terminal
import org.newdawn.slick.Color
import org.newdawn.slick.Font
import org.newdawn.slick.Image
import org.newdawn.slick.SpriteSheet
import java.util.*

/**
 * Based on multisheet slick spritesheef font (net.torvald.imagefont.GameFontBase) of my game project.
 * Again, based on my Ba-AA project (github.com/minjaesong/ba-aa)
 *
 * Created by minjaesong on 16-08-12.
 * Adopted by minjaesong on 16-09-07.
 */
class ColouredFastFont(val vt: Terminal, fontRef: String, val fontW: Int, val fontH: Int) : Font {

    val colouredSheet = ArrayList<SpriteSheet>() // index zero: dark grey
    private var sheetW = 0
    private var sheetH = 0


    private lateinit var sheetImageBuffer: Image

    init {
        val getSizeImg = Image(fontRef)
        sheetW = getSizeImg.width
        sheetH = getSizeImg.height

        getSizeImg.destroy()

        sheetImageBuffer = Image(sheetW, sheetH)

        for (i in 0..vt.coloursCount - 1) {
            val sheet = SpriteSheet("$fontRef.$i.tga", fontW, fontH)
            colouredSheet.add(sheet)

            //sheetImageBuffer.graphics.clear()
        }

        sheetImageBuffer.destroy()
    }

    private fun getIndexX(ch: Char) = ch.toInt() % (sheetW / fontW)
    private fun getIndexY(ch: Char) = ch.toInt() / (sheetW / fontW)

    override fun getHeight(p0: String): Int = fontH

    override fun getWidth(p0: String): Int {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLineHeight(): Int = fontH

    override fun drawString(p0: Float, p1: Float, p2: String) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun drawString(p0: Float, p1: Float, p2: String, p3: Color) {
        //search colour
        var colourIndex = -1
        for (i in 0..vt.coloursCount - 1) {
            if (vt.getColor(i) == p3) {
                colourIndex = i
                break
            }
        }

        if (colourIndex >= 0) {


            colouredSheet[colourIndex].startUse()


            for (i in 0..p2.length - 1) {
                val ch = p2[i]

                colouredSheet[colourIndex].renderInUse(
                        p0.floorInt() + (i * fontW),
                        p1.floorInt(),
                        getIndexX(ch),
                        getIndexY(ch)
                )
            }


            colouredSheet[colourIndex].endUse()
        }
        else {
            //System.err.println("[ColouredFastFont] unmatched colour! $p3")
        }

    }

    override fun drawString(p0: Float, p1: Float, p2: String, p3: Color, p4: Int, p5: Int) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun Float.floorInt() = this.toInt()
}
