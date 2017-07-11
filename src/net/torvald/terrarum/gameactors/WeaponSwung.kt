package net.torvald.terrarum.gameactors

import com.badlogic.gdx.graphics.Color

/**
 * Created by minjaesong on 16-04-26.
 */
class WeaponSwung(val itemID: Int) : ActorWithPhysics(Actor.RenderOrder.MIDTOP), Luminous {
    // just let the solver use AABB; it's cheap but works just enough

    /**
     * Recommended implementation:
     *
    override var color: Int
    get() = actorValue.getAsInt(AVKey.LUMINOSITY) ?: 0
    set(value) {
    actorValue[AVKey.LUMINOSITY] = value
    }
     */
    override var color: Color
        get() = throw UnsupportedOperationException()
        set(value) {
        }
    /**
     * Arguments:
     *
     * Hitbox(x-offset, y-offset, width, height)
     * (Use ArrayList for normal circumstances)
     */
    override val lightBoxList: List<Hitbox>
        get() = throw UnsupportedOperationException()

    init {

    }
}