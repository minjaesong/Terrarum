package com.Torvald.Terrarum.ConsoleCommand

import com.Torvald.Terrarum.Game
import com.Torvald.Terrarum.MapDrawer.MapDrawer
import com.Torvald.Terrarum.Terrarum

/**
 * Created by minjaesong on 16-01-24.
 */
class TeleportPlayer : ConsoleCommand {

    override fun execute(args: Array<String>) {
        if (args.size != 3) {
            printUsage()
        }
        else {

            val x: Int
            val y: Int
            try {
                x = args[1].toInt() * MapDrawer.TILE_SIZE + MapDrawer.TILE_SIZE / 2
                y = args[2].toInt() * MapDrawer.TILE_SIZE + MapDrawer.TILE_SIZE / 2
            }
            catch (e: NumberFormatException) {
                Echo().execute("Wrong number input.")
                return
            }

            Terrarum.game.player.setPosition(x.toFloat(), y.toFloat())
        }
    }

    override fun printUsage() {
        Echo().execute("Usage: teleport [x-tile] [y-tile]")
    }
}
