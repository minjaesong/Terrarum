package net.torvald.terrarum.modulebasegame.items

import net.torvald.terrarum.Point2d
import net.torvald.terrarum.Terrarum
import net.torvald.terrarum.blockproperties.Block
import net.torvald.terrarum.gameactors.AVKey
import net.torvald.terrarum.gameactors.ActorWBMovable
import net.torvald.terrarum.itemproperties.Calculate
import net.torvald.terrarum.itemproperties.GameItem
import net.torvald.terrarum.itemproperties.ItemID
import net.torvald.terrarum.itemproperties.Material
import net.torvald.terrarum.modulebasegame.Ingame

/**
 * Created by minjaesong on 2017-07-17.
 */
class PickaxeGeneric(override val originalID: ItemID) : GameItem() {

    override var dynamicID: ItemID = originalID
    override val originalName = "PACKAGED_PICK"
    override var baseMass = 10.0
    override var baseToolSize: Double? = 10.0
    override var stackable = true
    override var inventoryCategory = Category.TOOL
    override var isUnique = false
    override var isDynamic = true
    override var material = Material(0,0,0,0,0,0,0,0,1,0.0)

    init {
        super.equipPosition = GameItem.EquipPosition.HAND_GRIP
        super.maxDurability = 147
        super.durability = maxDurability.toFloat()
        super.name = "Builtin Pickaxe"
    }

    override fun startPrimaryUse(delta: Float): Boolean {
        val player = (Terrarum.ingame!! as Ingame).actorNowPlaying
        if (player == null) return false

        val mouseTileX = Terrarum.mouseTileX
        val mouseTileY = Terrarum.mouseTileY

        val mousePoint = Point2d(mouseTileX.toDouble(), mouseTileY.toDouble())
        val actorvalue = player.actorValue

        using = true

        // linear search filter (check for intersection with tilewise mouse point and tilewise hitbox)
        // return false if hitting actors
        Terrarum.ingame!!.actorContainerActive.forEach {
            if (it is ActorWBMovable && it.hIntTilewiseHitbox.intersects(mousePoint))
                return false
        }

        // return false if here's no tile
        if (Block.AIR == (Terrarum.ingame!!.world).getTileFromTerrain(mouseTileX, mouseTileY))
            return false

        // filter passed, do the job
        val swingDmgToFrameDmg = delta.toDouble() / actorvalue.getAsDouble(AVKey.ACTION_INTERVAL)!!

        (Terrarum.ingame!!.world).inflictTerrainDamage(
                mouseTileX, mouseTileY,
                Calculate.pickaxePower(player, material) * swingDmgToFrameDmg
        )

        return true
    }

    override fun endPrimaryUse(delta: Float): Boolean {
        val player = (Terrarum.ingame!! as Ingame).actorNowPlaying
        if (player == null) return false

        using = false
        // reset action timer to zero
        player.actorValue.set(AVKey.__ACTION_TIMER, 0.0)
        return true
    }
}