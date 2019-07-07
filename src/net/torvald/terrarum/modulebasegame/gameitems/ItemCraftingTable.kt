package net.torvald.terrarum.modulebasegame.gameitems

import com.badlogic.gdx.graphics.g2d.TextureRegion
import net.torvald.terrarum.AppLoader
import net.torvald.terrarum.Terrarum
import net.torvald.terrarum.gameitem.GameItem
import net.torvald.terrarum.gameitem.ItemID
import net.torvald.terrarum.itemproperties.Material
import net.torvald.terrarum.modulebasegame.gameactors.FixtureCraftingTable
import net.torvald.terrarum.modulebasegame.gameactors.FixtureTikiTorch

/**
 * Created by minjaesong on 2019-07-08.
 */
class ItemCraftingTable(originalID: ItemID) : GameItem(originalID) {

    override var dynamicID: ItemID = originalID
    override val originalName = "ITEM_CRAFTING_TABLE"
    override var baseMass = FixtureTikiTorch.MASS
    override var stackable = true
    override var inventoryCategory = Category.FIXTURE
    override val isUnique = false
    override val isDynamic = false
    override val material = Material()
    override val itemImage: TextureRegion?
        get() = AppLoader.resourcePool.getAsTextureRegion("itemplaceholder_16")
    override var baseToolSize: Double? = baseMass

    init {
        equipPosition = EquipPosition.HAND_GRIP
    }

    override fun startPrimaryUse(delta: Float): Boolean {
        val item = FixtureCraftingTable()

        return item.spawn(Terrarum.mouseTileX, Terrarum.mouseTileY - item.blockBox.height + 1)
        // return true when placed, false when cannot be placed
    }

}