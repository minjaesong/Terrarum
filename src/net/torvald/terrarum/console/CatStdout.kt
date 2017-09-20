package net.torvald.terrarum.console

import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Files

/**
 * Created by minjaesong on 2016-02-10.
 */
internal object CatStdout : ConsoleCommand {
    override fun execute(args: Array<String>) {

        if (args.size == 1) {
            printUsage()
            return
        }

        try {
            Files.lines(FileSystems.getDefault().getPath(args[1])).forEach({ Echo(it) })
        }
        catch (e: IOException) {
            Echo("CatStdout: could not read file -- IOException")
        }

    }

    override fun printUsage() {
        Echo("usage: cat 'path/to/text/file")
    }
}
