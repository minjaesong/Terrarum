package com.Torvald.Terrarum.ConsoleCommand

import com.Torvald.Terrarum.Actors.ActorValue
import com.Torvald.Terrarum.Game
import com.Torvald.Terrarum.Terrarum

/**
 * Created by minjaesong on 16-01-19.
 */
class GetAV : ConsoleCommand {
    override fun execute(args: Array<String>) {
        val echo = Echo()

        try {
            if (args.size == 1) {
                // print all actorvalue of player
                val av = Terrarum.game.player.getActorValue()
                val keyset = av.keySet

                keyset.forEach { elem -> echo.execute("$elem = ${av[elem as String]}") }

            }
            else if (args.size != 3 && args.size != 2) {
                printUsage()
            }
            else if (args.size == 2) {
                echo.execute("player." + args[1] + " = "
                             + Terrarum.game.player.getActorValue()[args[1]]
                             + " ("
                             + Terrarum.game.player.getActorValue()[args[1]]!!.javaClass.simpleName
                             + ")")
            }
            else if (args.size == 3) {

            }
        }
        catch (e: NullPointerException) {
            if (args.size == 2) {
                echo.execute(args[1] + ": actor value does not exist.")
            }
            else if (args.size == 3) {
                echo.execute(args[2] + ": actor value does not exist.")
            }
            else {
                throw NullPointerException()
            }
        }

    }

    override fun printUsage() {
        val echo = Echo()
        echo.execute("Get desired actor value of specific target.")
        echo.execute("Usage: getav (id) <av>")
        echo.execute("blank ID for player")
    }
}
