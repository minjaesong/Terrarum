package net.torvald.terrarum.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import net.torvald.terrarum.*
import net.torvald.terrarum.Terrarum.joypadLabelNinA
import net.torvald.terrarum.Terrarum.joypadLabelNinY
import net.torvald.terrarum.gameactors.*
import net.torvald.terrarum.gameactors.ActorInventory.Companion.CAPACITY_MODE_NO_ENCUMBER
import net.torvald.terrarum.itemproperties.GameItem
import net.torvald.terrarum.itemproperties.ItemCodex
import net.torvald.terrarum.langpack.Lang
import net.torvald.terrarumsansbitmap.gdx.TextureRegionPack
import java.util.*

/**
 * Created by SKYHi14 on 2017-03-13.
 */
class UIInventory(
        var actor: Pocketed?,
        override var width: Int,
        override var height: Int,
        var categoryWidth: Int
) : UICanvas, MouseControlled, KeyControlled {

    val inventory: ActorInventory?
        get() = actor?.inventory
    //val actorValue: ActorValue
    //    get() = (actor as Actor).actorValue

    override var handler: UIHandler? = null
    override var openCloseTime: Second = 0.12f

    val catButtonsToCatIdent = HashMap<String, String>()

    val backgroundColour = Color(0x242424_80)
    val defaultTextColour = Color(0xeaeaea_ff.toInt())

    init {
        catButtonsToCatIdent.put("GAME_INVENTORY_WEAPONS", GameItem.Category.WEAPON)
        catButtonsToCatIdent.put("CONTEXT_ITEM_TOOL_PLURAL", GameItem.Category.TOOL)
        catButtonsToCatIdent.put("CONTEXT_ITEM_ARMOR", GameItem.Category.ARMOUR)
        catButtonsToCatIdent.put("GAME_INVENTORY_INGREDIENTS", GameItem.Category.GENERIC)
        catButtonsToCatIdent.put("GAME_INVENTORY_POTIONS", GameItem.Category.POTION)
        catButtonsToCatIdent.put("CONTEXT_ITEM_MAGIC", GameItem.Category.MAGIC)
        catButtonsToCatIdent.put("GAME_INVENTORY_BLOCKS", GameItem.Category.BLOCK)
        catButtonsToCatIdent.put("GAME_INVENTORY_WALLS", GameItem.Category.WALL)
        catButtonsToCatIdent.put("GAME_GENRE_MISC", GameItem.Category.MISC)

        // special filter
        catButtonsToCatIdent.put("MENU_LABEL_ALL", "__all__")

    }

    val itemStripGutterV = 6
    val itemStripGutterH = 8
    val itemInterColGutter = 8

    val controlHelpHeight = Terrarum.fontGame.lineHeight.toInt()

    val catButtons = UIItemTextButtonList(
            this,
            arrayOf(
                    "MENU_LABEL_ALL",
                    "GAME_INVENTORY_BLOCKS",
                    "GAME_INVENTORY_WALLS",
                    "CONTEXT_ITEM_TOOL_PLURAL",
                    "GAME_INVENTORY_WEAPONS",
                    "CONTEXT_ITEM_ARMOR",
                    "GAME_INVENTORY_INGREDIENTS",
                    "GAME_INVENTORY_POTIONS",
                    "CONTEXT_ITEM_MAGIC",
                    "GAME_GENRE_MISC"
                    //"GAME_INVENTORY_FAVORITES",
            ),
            width = categoryWidth,
            height = height - controlHelpHeight,
            verticalGutter = itemStripGutterH,
            readFromLang = true,
            textAreaWidth = 100,
            defaultSelection = 0,
            iconSpriteSheet = TextureRegionPack("./assets/graphics/gui/inventory/category.tga", 20, 20),
            iconSpriteSheetIndices = intArrayOf(9,6,7,1,0,2,3,4,5,8),
            iconCol = defaultTextColour,
            highlightBackCol = Color(0xb8b8b8_ff.toInt()),
            highlightBackBlendMode = BlendMode.MULTIPLY,
            backgroundCol = Color(0), // will use custom background colour!
            backgroundBlendMode = BlendMode.NORMAL,
            kinematic = true,
            inactiveCol = defaultTextColour
    )

    val itemsStripWidth = ((width - catButtons.width) - (2 * itemStripGutterH + itemInterColGutter)) / 2
    val items = Array(
            ((height - controlHelpHeight) / (UIItemInventoryElem.height + itemStripGutterV)) * 2, {
        UIItemInventoryElem(
                parentUI = this,
                posX = catButtons.width + if (it % 2 == 0) itemStripGutterH else (itemStripGutterH + itemsStripWidth + itemInterColGutter),
                posY = itemStripGutterH + it / 2 * (UIItemInventoryElem.height + itemStripGutterV),
                width = itemsStripWidth,
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
    val itemsScrollOffset = 0

    var inventorySortList = ArrayList<InventoryPair>()
    private var rebuildList = true

    private val SP = "${0x3000.toChar()}${0x3000.toChar()}${0x3000.toChar()}"
    val listControlHelp: String
        get() = if (Terrarum.environment == RunningEnvironment.PC)
            "${0xe006.toChar()} ${Lang["GAME_INVENTORY_USE"]}$SP" +
            "${0xe011.toChar()}..${0xe010.toChar()} ${Lang["GAME_INVENTORY_REGISTER"]}$SP" +
            "${0xe034.toChar()} ${Lang["GAME_INVENTORY_DROP"]}"
    else
            "$joypadLabelNinY ${Lang["GAME_INVENTORY_USE"]}$SP" +
            "${0xe011.toChar()}${0xe010.toChar()} ${Lang["GAME_INVENTORY_REGISTER"]}$SP" +
            "$joypadLabelNinA ${Lang["GAME_INVENTORY_DROP"]}"
    val listControlClose: String
        get() = if (Terrarum.environment == RunningEnvironment.PC)
            "${0xe037.toChar()} ${Lang["GAME_ACTION_CLOSE"]}"
    else
            "${0xe069.toChar()} ${Lang["GAME_ACTION_CLOSE"]}"

    private var oldCatSelect = -1

    private var encumbrancePerc = 0f
    private var isEncumbered = false

    override fun update(delta: Float) {
        if (handler == null) {
            throw Error("Handler for this UI is null, you douchebag.")
        }

        catButtons.update(delta)

        if (actor != null && inventory != null) {
            // monitor and check if category selection has been changed
            // OR UI is being opened from closed state
            if (oldCatSelect != catButtons.selectedIndex ||
                    !rebuildList && handler!!.openFired) {
                rebuildList = true
            }


            if (rebuildList) {
                shutUpAndRebuild()
            }
        }


        oldCatSelect = catButtons.selectedIndex
    }

    private val weightBarWidth = 60f

    override fun render(batch: SpriteBatch) {
        // background
        blendNormal()
        batch.color = backgroundColour
        batch.fillRect(0f, 0f, width.toFloat(), height.toFloat())


        // cat bar background
        blendMul()
        batch.color = Color(0xcccccc_ff.toInt())
        batch.fillRect(0f, 0f, catButtons.width.toFloat(), height.toFloat())

        catButtons.render(batch)


        items.forEach {
            it.render(batch)
        }

        // texts
        blendNormal()
        batch.color = defaultTextColour
        // W - close
        Terrarum.fontGame.draw(batch, listControlClose, 4f, height - controlHelpHeight.toFloat())
        // MouseL - Use ; 1.9 - Register ; T - Drop
        Terrarum.fontGame.draw(batch, listControlHelp, catButtons.width + 4f, height - controlHelpHeight.toFloat())
        // encumbrance
        if (inventory != null) {
            val encumbranceText = Lang["GAME_INVENTORY_ENCUMBRANCE"]

            Terrarum.fontGame.draw(batch,
                    encumbranceText,
                    width - 9 - Terrarum.fontGame.getWidth(encumbranceText) - weightBarWidth,
                    height - controlHelpHeight.toFloat()
            )

            // encumbrance bar background
            blendMul()
            batch.color = Color(0xa0a0a0_ff.toInt())
            batch.fillRect(
                    width - 3 - weightBarWidth,
                    height - controlHelpHeight + 3f,
                    weightBarWidth,
                    controlHelpHeight - 6f
            )
            // encumbrance bar
            blendNormal()
            batch.color = if (isEncumbered) Color(0xff0000_cc.toInt()) else Color(0x00ff00_cc.toInt())
            batch.fillRect(
                    width - 3 - weightBarWidth,
                    height - controlHelpHeight + 3f,
                    if (actor?.inventory?.capacityMode == CAPACITY_MODE_NO_ENCUMBER)
                        1f
                    else // make sure 1px is always be seen
                        minOf(weightBarWidth, maxOf(1f, weightBarWidth * encumbrancePerc)),
                    controlHelpHeight - 5f
            )
        }
    }

    /** Persuade the UI to rebuild its item list */
    fun rebuildList() {
        rebuildList = true
    }


    fun shutUpAndRebuild() {
        val filter = catButtonsToCatIdent[catButtons.selectedButton.labelText]

        // encumbrance
        encumbrancePerc = inventory!!.capacity.toFloat() / inventory!!.maxCapacity
        isEncumbered = inventory!!.isEncumbered



        inventorySortList = ArrayList<InventoryPair>()

        // filter items
        inventory?.forEach {
            if (it.item.inventoryCategory == filter || filter == "__all__")
                inventorySortList.add(it)
        }

        rebuildList = false

        // sort if needed
        // test sort by name
        inventorySortList.sortBy { it.item.name }

        // map sortList to item list
        for (k in 0..items.size - 1) {
            // we have an item
            try {
                val sortListItem = inventorySortList[k + itemsScrollOffset]
                items[k].item = sortListItem.item
                items[k].amount = sortListItem.amount
                items[k].itemImage = ItemCodex.getItemImage(sortListItem.item)

                // set quickslot number
                for (qs in 1..UIQuickBar.SLOT_COUNT) {
                    if (sortListItem.item == actor?.inventory?.getQuickBar(qs - 1)?.item) {
                        items[k].quickslot = qs % 10 // 10 -> 0, 1..9 -> 1..9
                        break
                    }
                    else
                        items[k].quickslot = null
                }

                // set equippedslot number
                for (eq in 0..actor!!.inventory.itemEquipped.size - 1) {
                    if (eq < actor!!.inventory.itemEquipped.size) {
                        if (actor!!.inventory.itemEquipped[eq] == items[k].item) {
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



    ////////////
    // Inputs //
    ////////////

    override fun processInput(delta: Float) {
    }

    override fun doOpening(delta: Float) {
        UICanvas.doOpeningPopOut(handler, openCloseTime, UICanvas.Companion.Position.LEFT)
    }

    override fun doClosing(delta: Float) {
        UICanvas.doClosingPopOut(handler, openCloseTime, UICanvas.Companion.Position.LEFT)

    }

    override fun endOpening(delta: Float) {
        UICanvas.endOpeningPopOut(handler, UICanvas.Companion.Position.LEFT)
    }

    override fun endClosing(delta: Float) {
        UICanvas.endClosingPopOut(handler, UICanvas.Companion.Position.LEFT)
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        items.forEach { if (it.mouseUp) it.keyDown(keycode) }
        shutUpAndRebuild()

        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        items.forEach { if (it.mouseUp) it.keyUp(keycode) }
        shutUpAndRebuild()

        return true
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        items.forEach { if (it.mouseUp) it.touchDown(screenX, screenY, pointer, button) }

        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        items.forEach { if (it.mouseUp) it.touchUp(screenX, screenY, pointer, button) }

        return true
    }

    override fun scrolled(amount: Int): Boolean {
        return false
    }

    override fun dispose() {
        catButtons.dispose()
        items.forEach { it.dispose() }
    }
}
