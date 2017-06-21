package net.torvald.terrarum.console

import net.torvald.terrarum.TerrarumGDX

/**
 * Created by minjaesong on 16-03-20.
 */
internal object GetTime : ConsoleCommand {
    override fun execute(args: Array<String>) {

        val worldTime = TerrarumGDX.ingame!!.world.time
        Echo(worldTime.getFormattedTime())
    }

    override fun printUsage() {
        Echo("Print current world time in convenient form")
    }
}