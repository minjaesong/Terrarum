package net.torvald.terrarum.console

import net.torvald.imagefont.GameFontBase
import net.torvald.terrarum.langpack.Lang
import net.torvald.terrarum.Terrarum
import org.apache.commons.csv.CSVRecord
import org.newdawn.slick.SlickException

import java.io.IOException

/**
 * Created by minjaesong on 16-01-25.
 */
internal object SetLocale : ConsoleCommand {
    override fun execute(args: Array<String>) {
        if (args.size == 2) {
            val prevLocale = Terrarum.gameLocale
            Terrarum.gameLocale = args[1]
            try {
                Echo("Set locale to '" + Terrarum.gameLocale + "'.")
            }
            catch (e: IOException) {
                Echo("could not read lang file.")
                Terrarum.gameLocale = prevLocale
            }

        }
        else if (args.size == 1) {

            Echo("Locales:")

            Lang.languageList.forEach { Echo("--> $it") }
        }
        else {
            printUsage()
        }
    }

    override fun printUsage() {
        Echo("Usage: setlocale [locale]")
    }
}
