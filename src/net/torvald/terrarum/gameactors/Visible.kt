package net.torvald.terrarum.gameactors

import org.newdawn.slick.GameContainer
import org.newdawn.slick.Graphics

/**
 * Created by minjaesong on 16-01-25.
 */
interface Visible {
    val hitbox: Hitbox

    fun drawBody(gc: GameContainer, g: Graphics)

    fun updateBodySprite(gc: GameContainer, delta: Int)

    fun drawGlow(gc: GameContainer, g: Graphics)

    fun updateGlowSprite(gc: GameContainer, delta: Int)
}
