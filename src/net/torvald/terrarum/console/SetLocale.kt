package net.torvald.terrarum.console

import net.torvald.terrarum.langpack.Lang
import net.torvald.terrarum.AppLoader

import java.io.IOException

/**
 * Created by minjaesong on 2016-01-25.
 */
internal object SetLocale : ConsoleCommand {
    override fun execute(args: Array<String>) {
        if (args.size == 2) {
            val prevLocale = AppLoader.GAME_LOCALE
            AppLoader.GAME_LOCALE = args[1]
            try {
                Echo("Set locale to '" + AppLoader.GAME_LOCALE + "'.")
            }
            catch (e: IOException) {
                Echo("could not read lang file.")
                AppLoader.GAME_LOCALE = prevLocale
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
