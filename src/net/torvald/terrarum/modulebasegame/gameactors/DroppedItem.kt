package net.torvald.terrarum.modulebasegame.gameactors

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import net.torvald.terrarum.itemproperties.GameItem
import net.torvald.terrarum.itemproperties.ItemCodex
import net.torvald.terrarum.blockproperties.BlockCodex
import net.torvald.terrarum.gameactors.ActorWBMovable
import net.torvald.terrarum.gameworld.GameWorld

/**
 * Created by minjaesong on 2016-03-15.
 */
open class DroppedItem(world: GameWorld, private val item: GameItem) : ActorWBMovable(world, RenderOrder.MIDTOP) {

    init {
        if (item.dynamicID >= ItemCodex.ACTORID_MIN)
            throw RuntimeException("Attempted to create DroppedItem actor of a real actor; the real actor must be dropped instead.")

        isVisible = true

        avBaseMass = if (item.dynamicID < BlockCodex.TILE_UNIQUE_MAX)
            BlockCodex[item.dynamicID].density / 1000.0
        else
            ItemCodex[item.dynamicID].mass

        scale = ItemCodex[item.dynamicID].scale
    }

    override fun update(delta: Float) {
        super.update(delta)
    }

    override fun drawGlow(batch: SpriteBatch) {
        super.drawGlow(batch)
    }

    override fun drawBody(batch: SpriteBatch) {
        super.drawBody(batch)
    }
}