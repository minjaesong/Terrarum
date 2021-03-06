package net.torvald.terrarum.modulebasegame.gameactors

import net.torvald.gdx.graphics.Cvec
import net.torvald.terrarum.gameactors.ActorWithBody
import net.torvald.terrarum.gameactors.Hitbox
import net.torvald.terrarum.gameactors.Luminous
import net.torvald.terrarum.gameactors.PhysProperties

/**
 * Created by minjaesong on 2016-04-26.
 */
class WeaponSwung(val itemID: Int) : ActorWithBody(RenderOrder.MIDTOP, PhysProperties.IMMOBILE), Luminous {
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
    override var color: Cvec
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