package net.torvald.terrarum.modulebasegame.console

import net.torvald.terrarum.langpack.Lang
import net.torvald.terrarum.Terrarum
import net.torvald.terrarum.console.ConsoleCommand
import net.torvald.terrarum.modulebasegame.Ingame

/**
 * Created by minjaesong on 2016-01-23.
 */
internal object SetBulletin : ConsoleCommand {
    override fun execute(args: Array<String>) {
        val testMsg = arrayOf(
                Lang["ERROR_SAVE_CORRUPTED"],
                Lang["MENU_LABEL_CONTINUE_QUESTION"]
        )
        send(testMsg)
    }

    override fun printUsage() {

    }

    /**
     * Actually send notifinator
     * @param message real message
     */
    fun send(message: Array<String>) {
        (Terrarum.ingame!! as Ingame).sendNotification(message)
        println("sent notifinator")
    }
}