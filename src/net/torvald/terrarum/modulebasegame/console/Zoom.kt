package net.torvald.terrarum.modulebasegame.console

import net.torvald.terrarum.Terrarum
import net.torvald.terrarum.console.ConsoleCommand
import net.torvald.terrarum.console.Echo

/**
 * Created by minjaesong on 2016-01-25.
 */
internal object Zoom : ConsoleCommand {
    override fun execute(args: Array<String>) {
        if (args.size == 2) {

            var zoom: Float
            try {
                zoom = args[1].toFloat()
            }
            catch (e: NumberFormatException) {
                Echo("Wrong number input.")
                return
            }

            if (zoom < Terrarum.ingame!!.ZOOM_MINIMUM) {
                zoom = Terrarum.ingame!!.ZOOM_MINIMUM
            }
            else if (zoom > Terrarum.ingame!!.ZOOM_MAXIMUM) {
                zoom = Terrarum.ingame!!.ZOOM_MAXIMUM
            }

            Terrarum.ingame!!.screenZoom = zoom

            System.gc()

            Echo("Set screen zoom to " + zoom.toString())
        }
        else {
            printUsage()
        }
    }

    override fun printUsage() {
        Echo("Usage: zoom [zoom]")
    }
}
