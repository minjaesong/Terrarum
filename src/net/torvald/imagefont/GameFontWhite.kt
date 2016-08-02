package net.torvald.imagefont

import net.torvald.terrarum.Terrarum
import org.newdawn.slick.*

/**
 * Created by minjaesong on 16-01-20.
 */
class GameFontWhite @Throws(SlickException::class)
constructor() : GameFontBase() {

    init {

        GameFontBase.hangulSheet = SpriteSheet(
                "./assets/graphics/fonts/han_johab.png", GameFontBase.W_CJK, GameFontBase.H_HANGUL)
        GameFontBase.asciiSheet = SpriteSheet(
                "./assets/graphics/fonts/ascii_fullwidth.png", GameFontBase.W_LATIN_WIDE, GameFontBase.H)
        GameFontBase.asciiSheetEF = SpriteSheet(
                "./assets/graphics/fonts/ascii_special_ef.png", GameFontBase.W_LATIN_NARROW, GameFontBase.H)
        GameFontBase.runicSheet = SpriteSheet(
                "./assets/graphics/fonts/futhark.png", GameFontBase.W_LATIN_WIDE, GameFontBase.H)
        GameFontBase.extASheet = SpriteSheet(
                "./assets/graphics/fonts/LatinExtA_fullwidth.png", GameFontBase.W_LATIN_WIDE, GameFontBase.H)
        GameFontBase.extASheetEF = SpriteSheet(
                "./assets/graphics/fonts/LatinExtA_ef.png", GameFontBase.W_LATIN_NARROW, GameFontBase.H)
        GameFontBase.kanaSheet = SpriteSheet(
                "./assets/graphics/fonts/kana.png", GameFontBase.W_CJK, GameFontBase.H_KANA)
        GameFontBase.cjkPunct = SpriteSheet(
                "./assets/graphics/fonts/cjkpunct.png", GameFontBase.W_CJK, GameFontBase.H_KANA)
        /*uniHan = new SpriteSheet(
                "./assets/graphics/fonts/unifont_unihan"
                        + ((!terrarum.gameLocale.contains("zh"))
                        ? "_ja" : "")
                        +".png"
                , W_UNIHAN, H_UNIHAN
        );*/
        GameFontBase.cyrilic = SpriteSheet(
                "./assets/graphics/fonts/cyrilic_fullwidth.png", GameFontBase.W_LATIN_WIDE, GameFontBase.H)
        GameFontBase.cyrilicEF = SpriteSheet(
                "./assets/graphics/fonts/cyrilic_ef.png", GameFontBase.W_LATIN_NARROW, GameFontBase.H)
        GameFontBase.fullwidthForms = SpriteSheet(
                "./assets/graphics/fonts/fullwidth_forms.png", GameFontBase.W_UNIHAN, GameFontBase.H_UNIHAN)
        GameFontBase.uniPunct = SpriteSheet(
                "./assets/graphics/fonts/unipunct.png", GameFontBase.W_LATIN_WIDE, GameFontBase.H)
        GameFontBase.wenQuanYi_1 = SpriteSheet(
                "./assets/graphics/fonts/wenquanyi_11pt_part1.png", 16, 18, 2)
        GameFontBase.wenQuanYi_2 = SpriteSheet(
                "./assets/graphics/fonts/wenquanyi_11pt_part2.png", 16, 18, 2)
        GameFontBase.greekSheet = SpriteSheet(
                "./assets/graphics/fonts/greek_fullwidth.png", GameFontBase.W_LATIN_WIDE, GameFontBase.H)
        GameFontBase.greekSheetEF = SpriteSheet(
                "./assets/graphics/fonts/greek_ef.png", GameFontBase.W_LATIN_NARROW, GameFontBase.H)
        GameFontBase.romanianSheet = SpriteSheet(
                "./assets/graphics/fonts/romana_fullwidth.png", GameFontBase.W_LATIN_WIDE, GameFontBase.H)
        GameFontBase.romanianSheetEF = SpriteSheet(
                "./assets/graphics/fonts/romana_ef.png", GameFontBase.W_LATIN_NARROW, GameFontBase.H)
        GameFontBase.thaiSheet = SpriteSheet(
                "./assets/graphics/fonts/thai_fullwidth.png", GameFontBase.W_LATIN_WIDE, GameFontBase.H)

        val shk = arrayOf(
                GameFontBase.asciiSheet,
                GameFontBase.asciiSheetEF,
                GameFontBase.hangulSheet,
                GameFontBase.runicSheet,
                GameFontBase.extASheet,
                GameFontBase.extASheetEF,
                GameFontBase.kanaSheet,
                GameFontBase.cjkPunct,
                null, // Filler
                GameFontBase.cyrilic,
                GameFontBase.cyrilicEF,
                GameFontBase.fullwidthForms,
                GameFontBase.uniPunct,
                GameFontBase.wenQuanYi_1,
                GameFontBase.wenQuanYi_2, // uniHan
                GameFontBase.greekSheet,
                GameFontBase.greekSheetEF,
                GameFontBase.romanianSheet,
                GameFontBase.romanianSheetEF,
                GameFontBase.thaiSheet,
                null // Filler
        )
        GameFontBase.sheetKey = shk
    }
}
