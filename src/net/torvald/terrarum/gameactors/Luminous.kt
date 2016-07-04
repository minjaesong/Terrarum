package net.torvald.terrarum.gameactors

import java.util.*

/**
 * Created by minjaesong on 16-03-14.
 */
interface Luminous {

    /**
     * Recommended implementation:
     *
     override var luminosity: Int
        get() = actorValue.getAsInt(AVKey.LUMINOSITY) ?: 0
        set(value) {
            actorValue[AVKey.LUMINOSITY] = value
        }
     */
    var luminosity: Int

    /**
     * Arguments:
     *
     * Hitbox(x-offset, y-offset, width, height)
     * (Use ArrayList for normal circumstances)
     */
    val lightBoxList: List<Hitbox>
}