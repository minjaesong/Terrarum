package net.torvald.terrarum.gameactors

import net.torvald.random.HQRNG
import net.torvald.terrarum.Terrarum
import net.torvald.terrarum.itemproperties.ItemPropCodex
import org.newdawn.slick.GameContainer

/**
 * Created by minjaesong on 16-03-14.
 */
abstract class Actor : Comparable<Actor>, Runnable {

    abstract fun update(gc: GameContainer, delta: Int)

    /**
     * Valid RefID is equal to or greater than 32768.
     * @return Reference ID. (32768-0xFFFF_FFFF)
     */
    abstract var referenceID: Int

    abstract var actorValue: ActorValue

    override fun equals(other: Any?) = referenceID == (other as Actor).referenceID
    override fun hashCode() = referenceID
    override fun toString() = "Actor, " + if (actorValue.getAsString(AVKey.NAME).isNullOrEmpty())
        "ID: ${hashCode()}"
    else
        "ID: ${hashCode()} (${actorValue.getAsString(AVKey.NAME)})"
    override fun compareTo(other: Actor): Int = (this.referenceID - other.referenceID).sign()

    fun Int.sign(): Int = if (this > 0) 1 else if (this < 0) -1 else this

    /**
     * Usage:
     *
     * override var referenceID: Int = generateUniqueReferenceID()
     */
    fun generateUniqueReferenceID(): Int {
        var ret: Int
        do {
            ret = HQRNG().nextInt().and(0x7FFFFFFF) // set new ID
        } while (Terrarum.game.hasActor(ret) || ret < ItemPropCodex.ITEM_UNIQUE_MAX) // check for collision
        return ret
    }
}