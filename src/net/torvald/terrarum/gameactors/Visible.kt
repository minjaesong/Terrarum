package net.torvald.terrarum.gameactors

import org.newdawn.slick.GameContainer
import org.newdawn.slick.Graphics

/**
 * Created by minjaesong on 16-03-14.
 */
interface Visible {
    val hitbox: Hitbox

    fun drawBody(gc: GameContainer, g: Graphics)

    fun updateBodySprite(gc: GameContainer, delta: Int)
}
