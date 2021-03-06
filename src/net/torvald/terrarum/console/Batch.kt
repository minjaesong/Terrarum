package net.torvald.terrarum.console

import java.nio.file.FileSystems
import java.nio.file.Files

/**
 * Created by minjaesong on 2016-03-07.
 */
internal object Batch : ConsoleCommand {
    @Throws(Exception::class)
    override fun execute(args: Array<String>) {
        if (args.size == 2) {
            Files.lines(FileSystems.getDefault().getPath(args[1])).forEach(
                    { CommandInterpreter.execute(it) })
        }
        else {
            printUsage()
        }
    }

    override fun printUsage() {
        Echo("Usage: batch path/to/batch.txt")
    }
}
