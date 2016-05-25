package net.torvald.terrarum

import net.torvald.terrarum.gameactors.Player
import org.newdawn.slick.GameContainer

/**
 * Created by minjaesong on 16-05-25.
 */
class ThreadActorUpdate(val startIndex: Int, val endIndex: Int,
                        val gc: GameContainer, val delta: Int) : Runnable {
    override fun run() {
        for (i in startIndex..endIndex)
            Terrarum.game.actorContainer[i].update(gc, delta)
    }
}