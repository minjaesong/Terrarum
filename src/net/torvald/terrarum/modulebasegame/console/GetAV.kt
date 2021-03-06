package net.torvald.terrarum.modulebasegame.console

import net.torvald.terrarum.*
import net.torvald.terrarum.Terrarum
import net.torvald.terrarum.console.ConsoleCommand
import net.torvald.terrarum.console.Echo
import net.torvald.terrarum.console.EchoError
import net.torvald.terrarum.modulebasegame.TerrarumIngame

/**
 * Created by minjaesong on 2016-01-19.
 */
internal object GetAV : ConsoleCommand {

    override fun execute(args: Array<String>) {
        try {
            val ingame = Terrarum.ingame!! as TerrarumIngame
            val player = (Terrarum.ingame!! as TerrarumIngame).actorNowPlaying
            if (player == null) return
            
            
            if (args.size == 1 && player != null) {
                // print all actorvalue of player
                val av = player.actorValue
                val keyset = av.keySet

                Echo("$ccW== ActorValue list for ${ccY}player $ccW==")
                println("[GetAV] == ActorValue list for 'player' ==")
                keyset.forEach { elem ->
                    Echo("$ccM$elem $ccW= $ccG${av[elem as String]}")
                    println("[GetAV] $elem = ${av[elem]}")
                }
            }
            else if (args.size != 3 && args.size != 2) {
                printUsage()
            }
            else if (args.size == 2) {
                // check if args[1] is number or not
                if (!args[1].isNum()) { // args[1] is ActorValue name
                    Echo("${ccW}player.$ccM${args[1]} $ccW= " +
                         ccG +
                         player.actorValue[args[1]] +
                         " $ccO" +
                         player.actorValue[args[1]]!!.javaClass.simpleName
                    )
                    println("[GetAV] player.${args[1]} = " +
                            player.actorValue[args[1]] +
                            " " +
                            player.actorValue[args[1]]!!.javaClass.simpleName
                    )
                }
                else {
                    // args[1] is actor ID
                    val actor = ingame.getActorByID(args[1].toInt())
                    val av = actor.actorValue
                    val keyset = av.keySet

                    Echo("$ccW== ActorValue list for $ccY$actor $ccW==")
                    println("[GetAV] == ActorValue list for '$actor' ==")
                    if (keyset.isEmpty()) {
                        Echo("$ccK(nothing)")
                        println("[GetAV] (nothing)")
                    }
                    else {
                        keyset.forEach { elem ->
                            Echo("$ccM$elem $ccW= $ccG${av[elem as String]}")
                            println("[GetAV] $elem = ${av[elem]}")
                        }
                    }
                }
            }
            else if (args.size == 3) {
                val id = args[1].toInt()
                val av = args[2]
                Echo("$ccW$id.$ccM$av $ccW= $ccG" +
                                                  ingame.getActorByID(id).actorValue[av] +
                                                  " $ccO" +
                                                  ingame.getActorByID(id).actorValue[av]!!.javaClass.simpleName
                )
                println("$id.$av = " +
                        ingame.getActorByID(id).actorValue[av] +
                        " " +
                        ingame.getActorByID(id).actorValue[av]!!.javaClass.simpleName
                )
            }
        }
        catch (e: NullPointerException) {
            if (args.size == 2) {
                EchoError(args[1] + ": actor value does not exist.")
                System.err.println("[GetAV] ${args[1]}: actor value does not exist.")
            }
            else if (args.size == 3) {
                EchoError(args[2] + ": actor value does not exist.")
                System.err.println("[GetAV] ${args[2]}: actor value does not exist.")
            }
            else {
                throw NullPointerException()
            }
        }
        catch (e1: IllegalArgumentException) {
            EchoError("${args[1]}: no actor with this ID.")
            System.err.println("[GetAV] ${args[1]}: no actor with this ID.")
        }

    }

    fun String.isNum(): Boolean {
        try {
            this.toInt()
            return true
        }
        catch (e: NumberFormatException) {
            return false
        }
    }

    override fun printUsage() {
        Echo("${ccW}Get desired ActorValue of specific target.")
        Echo("${ccW}Usage: ${ccY}getav $ccG(id) <av>")
        Echo("${ccW}blank ID for player")
    }
}
