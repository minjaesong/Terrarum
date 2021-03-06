package net.torvald.terrarum.tests

import net.torvald.spriteassembler.ADProperties
import net.torvald.spriteassembler.AssembleSheetPixmap
import java.io.StringReader

/**
 * Created by minjaesong on 2019-01-06.
 */
class SpriteAssemblerTest {

    operator fun invoke() {
        val properties = ADProperties(StringReader(ADLParsingTest().TEST_STR))
        AssembleSheetPixmap.invoke(properties)
    }

}

fun main(args: Array<String>) {
    SpriteAssemblerTest().invoke()
}