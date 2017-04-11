package net.torvald.imagefont

import org.newdawn.slick.Color
import org.newdawn.slick.Font
import org.newdawn.slick.SpriteSheet

/**
 * Created by SKYHi14 on 2017-03-24.
 */
class NewRunes : Font {
    private val runeSize = 12

    // hard-coded encode map
    private fun codeToEnc(code: Int): Int? = if (code in 0x21..0x3f)
            code - 0x20
        else if (code in 0x3001..0x300f)
            code - 0x3000 + 0x20
        else if (code in 0x3131..0x3163)
            code - 0x3130 + 0x30
        else
            null


    private val runes = SpriteSheet("./assets/graphics/fonts/newrunes.tga", runeSize, runeSize)

    var scale = 1
    var linegap = 8


    override fun getHeight(str: String) = 12

    override fun drawString(x: Float, y: Float, text: String) = drawString(x, y, text, Color.white)

    override fun drawString(x: Float, y: Float, text: String, col: Color) {
        text.forEachIndexed { index, c ->
            val encodePoint = codeToEnc(c.toInt())

            if (encodePoint != null) {
                runes.getSubImage(encodePoint % 16, encodePoint / 16).draw(
                        x + runeSize * index * scale, y, scale.toFloat(), col
                )
            }
        }
    }

    override fun drawString(x: Float, y: Float, text: String, col: Color, startIndex: Int, endIndex: Int) {
        UnsupportedOperationException("not implemented")
    }

    override fun getWidth(str: String) = runeSize * str.length

    override fun getLineHeight() = (runeSize + linegap) * scale
}

/*
How runes are made:

The new runes are based on Hangul writing system. The runes had two main goals:
 - Implement the principle of its design (specifically, how this letter is altered from its base shape)
    - {KIYEOK, KHIEUKH}, {TIKEUT, THIEUTH, NIEUN}, {PIEUP, PHIEUPH, MIEUM}, {CIEUC, CHIEUCH, SIOS} sets
        are similar in shape
    - Aspirated sounds keep similar shape to their base
    - Vowels are not random; they have rules
    - In non-assembled writing, IEUNG only appears as "-ng" phoneme, so the shape is based on
        old Hangul YESIEUNG, which actually had "-ng" sound
 - "Doensori" are realised by prepending SIOS, much like older Korean orthography
 - Good enough obfuscation


Notes:
 - In some letters (e.g. NIEUN-HIEUH), ligatures may applied
 - EU appear as non-assembled shape; U-shape instead of dash


 */