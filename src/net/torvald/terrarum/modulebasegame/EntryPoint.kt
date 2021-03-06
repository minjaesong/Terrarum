package net.torvald.terrarum.modulebasegame

import net.torvald.terrarum.AppLoader.IS_DEVELOPMENT_BUILD
import net.torvald.terrarum.AppLoader.printdbg
import net.torvald.terrarum.CommonResourcePool
import net.torvald.terrarum.ModMgr
import net.torvald.terrarum.ModuleEntryPoint
import net.torvald.terrarum.blockproperties.BlockCodex
import net.torvald.terrarum.blockproperties.BlockProp
import net.torvald.terrarum.gameitem.GameItem
import net.torvald.terrarum.itemproperties.ItemCodex
import net.torvald.terrarum.itemproperties.MaterialCodex
import net.torvald.terrarum.modulebasegame.gameitems.BlockBase
import net.torvald.terrarum.modulebasegame.imagefont.WatchFont
import net.torvald.terrarumsansbitmap.gdx.TextureRegionPack

/**
 * The entry point for the module "Basegame"
 *
 * Created by minjaesong on 2018-06-21.
 */
class EntryPoint : ModuleEntryPoint() {

    private val moduleName = "basegame"

    override fun invoke() {

        // the order of invocation is important! Material should be the first as blocks and items are depend on it.
        ModMgr.GameMaterialLoader.invoke(moduleName)
        ModMgr.GameBlockLoader.invoke(moduleName)
        ModMgr.GameItemLoader.invoke(moduleName)
        ModMgr.GameLanguageLoader.invoke(moduleName)



        // load common resources to the AssetsManager
        CommonResourcePool.addToLoadingList("$moduleName.items16") {
            TextureRegionPack(ModMgr.getGdxFile(moduleName, "items/items.tga"), 16, 16)
        }
        CommonResourcePool.addToLoadingList("$moduleName.items24") {
            TextureRegionPack(ModMgr.getGdxFile(moduleName, "items/items24.tga"), 24, 24)
        }
        CommonResourcePool.addToLoadingList("$moduleName.items48") {
            TextureRegionPack(ModMgr.getGdxFile(moduleName, "items/items48.tga"), 48, 48)
        }


        /////////////////////////////////
        // load customised item loader //
        /////////////////////////////////

        printdbg(this, "recording item ID ")

        // blocks.csvs are loaded by ModMgr beforehand
        // block items (blocks and walls are the same thing basically)
        for (tile in BlockCodex.getAll()) {
            ItemCodex[tile.id] = makeNewItemObj(tile, false)

            if (IS_DEVELOPMENT_BUILD) print(tile.id+" ")

            if (BlockCodex[tile.id].isWallable) {
                ItemCodex["wall@" + tile.id] = makeNewItemObj(tile, true)
                if (IS_DEVELOPMENT_BUILD) print("wall@" + tile.id + " ")
            }
        }



        println("[Basegame.EntryPoint] Welcome back!")
    }

    private fun makeNewItemObj(tile: BlockProp, isWall: Boolean) = object : GameItem(
            if (isWall) "wall@"+tile.id else tile.id
    ) {
        override val isUnique: Boolean = false
        override var baseMass: Double = tile.density / 1000.0
        override var baseToolSize: Double? = null
        override val originalName = tile.nameKey
        override var stackable = true
        override var inventoryCategory = if (isWall) Category.WALL else Category.BLOCK
        override var isDynamic = false
        override val material = MaterialCodex.getOrDefault(tile.material)

        init {
            equipPosition = EquipPosition.HAND_GRIP
        }

        override fun startPrimaryUse(delta: Float): Boolean {
            return BlockBase.blockStartPrimaryUse(this, dynamicID, delta)
        }

        override fun effectWhenEquipped(delta: Float) {
            BlockBase.blockEffectWhenEquipped(delta)
        }
    }


    override fun dispose() {
        WatchFont.dispose()
    }
}