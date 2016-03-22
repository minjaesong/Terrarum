package com.Torvald.Terrarum.GameItem

import com.Torvald.Terrarum.TileProperties.TilePropCodex
import org.newdawn.slick.GameContainer

/**
 * Created by minjaesong on 16-03-15.
 */
class TileAsItem(tileNum: Int) : InventoryItem {

    override var itemID: Long = -1
    override var mass: Float = 0f
    override var scale: Float = 1f

    init {
        itemID = tileNum.toLong()
        mass = TilePropCodex.getProp(tileNum).density / 1000f
    }

    override fun effectWhileInPocket(gc: GameContainer, delta_t: Int) {
        throw UnsupportedOperationException()
    }

    override fun effectWhenPickedUp(gc: GameContainer, delta_t: Int) {
        throw UnsupportedOperationException()
    }

    override fun primaryUse(gc: GameContainer, delta_t: Int) {
        throw UnsupportedOperationException()
    }

    override fun secondaryUse(gc: GameContainer, delta_t: Int) {
        throw UnsupportedOperationException()
    }

    override fun effectWhenThrownAway(gc: GameContainer, delta_t: Int) {
        throw UnsupportedOperationException()
    }
}