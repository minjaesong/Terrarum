package net.torvald.terrarum.gameactors

import org.newdawn.slick.GameContainer
import org.newdawn.slick.Graphics

/**
 * Actor with visible body
 *
 * Created by minjaesong on 2017-01-21.
 */
abstract class ActorVisible(renderOrder: RenderOrder) : Actor(renderOrder) {
    open val hitbox = Hitbox(0.0, 0.0, 0.0, 0.0)
    override abstract fun update(gc: GameContainer, delta: Int)
    abstract fun drawBody(g: Graphics)
    abstract fun drawGlow(g: Graphics)
}