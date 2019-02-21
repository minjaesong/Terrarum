package net.torvald.terrarum.modulebasegame.console

import com.google.gson.GsonBuilder
import net.torvald.terrarum.AppLoader
import net.torvald.terrarum.Terrarum
import net.torvald.terrarum.console.ConsoleCommand
import net.torvald.terrarum.console.Echo
import net.torvald.terrarum.modulebasegame.Ingame
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException

/**
 * Created by minjaesong on 2016-02-10.
 */
internal object GsonTest : ConsoleCommand {
    override fun execute(args: Array<String>) {
        if (args.size == 2) {

            val jsonBuilder = if (AppLoader.IS_DEVELOPMENT_BUILD) {
                GsonBuilder()
                        .setPrettyPrinting()

                        .serializeNulls()
                        .create()
            }
            else {
                GsonBuilder()
                        .serializeNulls()
                        .create()
            }


            val jsonString = jsonBuilder.toJson((Terrarum.ingame!! as Ingame).actorNowPlaying)

            //val avelem = Gson().toJson((Terrarum.ingame!! as Ingame).actorNowPlaying)
            //val jsonString = avelem.toString()

            val bufferedWriter: BufferedWriter
            val writer: FileWriter
            try {
                writer = FileWriter(AppLoader.defaultDir + "/Exports/" + args[1] + ".json")
                bufferedWriter = BufferedWriter(writer)

                bufferedWriter.write(jsonString)
                bufferedWriter.close()

                Echo("GsonTest: exported to " + args[1] + ".json")
            }
            catch (e: IOException) {
                Echo("GsonTest: IOException raised.")
                e.printStackTrace()
            }

        }
        else {
            printUsage()
        }
    }

    override fun printUsage() {

        Echo("Usage: gsontest filename-without-extension")
    }
}
