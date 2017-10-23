package net.torvald.terrarum.ui

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import net.torvald.terrarum.BlendMode
import net.torvald.terrarum.Terrarum
import net.torvald.terrarum.UIItemInventoryElem
import net.torvald.terrarum.UIItemInventoryElemSimple
import net.torvald.terrarum.console.Inventory
import net.torvald.terrarum.gameactors.ActorInventory
import net.torvald.terrarum.gameactors.InventoryPair
import net.torvald.terrarum.itemproperties.GameItem
import net.torvald.terrarum.itemproperties.ItemCodex
import java.util.ArrayList

/**
 * Display either extended or compact list
 *
 * Note: everything is pretty much fixed size.
 *
 * Dimension of the whole area: 496x384
 * Number of grids: 9x7
 * Number of lists: 2x7
 *
 * Created by minjaesong on 2017-10-21.
 */
class UIItemInventoryDynamicList(
        parentUI: UIInventoryFull,
        val inventory: ActorInventory,
        override var posX: Int,
        override var posY: Int
) : UIItem(parentUI) {

    override val width  = 496
    override val height = 384

    private val catArrangement = parentUI.catArrangement



    val catIconsMeaning = listOf( // sortedBy: catArrangement
            GameItem.Category.WEAPON,
            GameItem.Category.TOOL,
            GameItem.Category.ARMOUR,
            GameItem.Category.GENERIC,
            GameItem.Category.POTION,
            GameItem.Category.MAGIC,
            GameItem.Category.BLOCK,
            GameItem.Category.WALL,
            GameItem.Category.MISC,
            "__all__"
    )

    private val inventoryUI = parentUI

    private val selection: Int
        get() = inventoryUI.catSelection
    private val selectedIcon: Int
        get() = inventoryUI.catSelectedIcon

    private val compactViewCat = setOf(3, 4, 6, 7, 9) // ingredients, potions, blocks, walls, all (spritesheet order)

    var itemPage = 0
    var itemPageCount = 1 // TODO total size of current category / items.size

    var inventorySortList = ArrayList<InventoryPair>()
    private var rebuildList = true

    val defaultTextColour = Color(0xeaeaea_ff.toInt())

    private val listGap = 8
    private val itemList = Array<UIItemInventoryCellBase>(
            7 * 2, {
        UIItemInventoryElem(
                parentUI = inventoryUI,
                posX = this.posX + (244 + listGap) * (it % 2),
                posY = this.posY + (UIItemInventoryElem.height + listGap) * (it / 2),
                width = 244,
                item = null,
                amount = UIItemInventoryElem.UNIQUE_ITEM_HAS_NO_AMOUNT,
                itemImage = null,
                mouseoverBackCol = Color(0x282828_ff),
                mouseoverBackBlendMode = BlendMode.SCREEN,
                backCol = Color(0xd4d4d4_ff.toInt()),
                backBlendMode = BlendMode.MULTIPLY,
                drawBackOnNull = true,
                inactiveTextCol = defaultTextColour
        ) })
    private val itemGrid = Array<UIItemInventoryCellBase>(
            7 * 9, {
        UIItemInventoryElemSimple(
                parentUI = inventoryUI,
                posX = this.posX + (UIItemInventoryElemSimple.height + listGap) * (it % 9),
                posY = this.posY + (UIItemInventoryElemSimple.height + listGap) * (it / 9),
                item = null,
                amount = UIItemInventoryElem.UNIQUE_ITEM_HAS_NO_AMOUNT,
                itemImage = null,
                mouseoverBackCol = Color(0x282828_ff),
                mouseoverBackBlendMode = BlendMode.SCREEN,
                backCol = Color(0xd4d4d4_ff.toInt()),
                backBlendMode = BlendMode.MULTIPLY,
                drawBackOnNull = true,
                inactiveTextCol = defaultTextColour
        )
    }
    )

    private val items: Array<UIItemInventoryCellBase>
            get() = if (catArrangement[selection] in compactViewCat) itemGrid else itemList



    override fun render(batch: SpriteBatch, camera: Camera) {

        items.forEach { it.render(batch, camera) }


        super.render(batch, camera)
    }


    override fun update(delta: Float) {
        super.update(delta)

        items.forEach { it.update(delta) }
    }



    internal fun rebuild() {
        val filter = catIconsMeaning[selectedIcon]

        inventorySortList = ArrayList<InventoryPair>()

        // filter items
        inventory.forEach {
            if (it.item.inventoryCategory == filter || filter == "__all__")
                inventorySortList.add(it)
        }

        rebuildList = false

        // sort if needed
        // test sort by name
        inventorySortList.sortBy { it.item.name }

        // map sortList to item list
        for (k in 0 until items.size) {
            // we have an item
            try {
                val sortListItem = inventorySortList[k + itemPage * items.size]
                items[k].item = sortListItem.item
                items[k].amount = sortListItem.amount
                items[k].itemImage = ItemCodex.getItemImage(sortListItem.item)

                // set quickslot number
                for (qs in 1..UIQuickBar.SLOT_COUNT) {
                    if (sortListItem.item == inventory.getQuickBar(qs - 1)?.item) {
                        items[k].quickslot = qs % 10 // 10 -> 0, 1..9 -> 1..9
                        break
                    }
                    else
                        items[k].quickslot = null
                }

                // set equippedslot number
                for (eq in 0 until inventory.itemEquipped.size) {
                    if (eq < inventory.itemEquipped.size) {
                        if (inventory.itemEquipped[eq] == items[k].item) {
                            items[k].equippedSlot = eq
                            break
                        }
                        else
                            items[k].equippedSlot = null
                    }
                }
            }
            // we do not have an item, empty the slot
            catch (e: IndexOutOfBoundsException) {
                items[k].item = null
                items[k].amount = 0
                items[k].itemImage = null
                items[k].quickslot = null
            }
        }
    }

    override fun dispose() {
        itemList.forEach { it.dispose() }
        itemGrid.forEach { it.dispose() }
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        super.touchDown(screenX, screenY, pointer, button)

        items.forEach { if (it.mouseUp) it.touchDown(screenX, screenY, pointer, button) }
        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        items.forEach { if (it.mouseUp) it.touchUp(screenX, screenY, pointer, button) }

        return true
    }

    override fun keyDown(keycode: Int): Boolean {
        super.keyDown(keycode)

        items.forEach { if (it.mouseUp) it.keyDown(keycode) }
        rebuild()

        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        super.keyUp(keycode)

        items.forEach { if (it.mouseUp) it.keyUp(keycode) }
        rebuild()

        return true
    }
}
