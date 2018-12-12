package net.torvald.terrarum

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import net.torvald.colourutil.CIELabUtil.darkerLab
import net.torvald.terrarum.itemproperties.GameItem
import net.torvald.terrarum.modulebasegame.Ingame
import net.torvald.terrarum.modulebasegame.ui.UIInventoryFull
import net.torvald.terrarum.modulebasegame.ui.UIItemInventoryCellBase
import net.torvald.terrarum.ui.*

/***
 * Note that the UI will not render if either item or itemImage is null.
 *
 * Created by minjaesong on 2017-03-16.
 */
class UIItemInventoryElem(
        parentUI: UIInventoryFull,
        override var posX: Int,
        override var posY: Int,
        override val width: Int,
        override var item: GameItem?,
        override var amount: Int,
        override var itemImage: TextureRegion?,
        val mouseOverTextCol: Color = Color(0xfff066_ff.toInt()),
        val mouseoverBackCol: Color = Color(0),
        val mouseoverBackBlendMode: String = BlendMode.NORMAL,
        val inactiveTextCol: Color = UIItemTextButton.defaultInactiveCol,
        val backCol: Color = Color(0),
        val backBlendMode: String = BlendMode.NORMAL,
        override var quickslot: Int? = null,
        override var equippedSlot: Int? = null,
        val drawBackOnNull: Boolean = true
) : UIItemInventoryCellBase(parentUI, posX, posY, item, amount, itemImage, quickslot, equippedSlot) {

    companion object {
        val height = 48
        val UNIQUE_ITEM_HAS_NO_AMOUNT = -1

        internal val durabilityCol = Color(0x22ff11_ff)
        internal val durabilityBack: Color; get() = durabilityCol.darkerLab(0.4f)
        internal val durabilityBarThickness = 3f
    }

    private val inventoryUI = parentUI

    override val height = UIItemInventoryElem.height

    private val imgOffset: Float
        get() = (this.height - itemImage!!.regionHeight).div(2).toFloat() // to snap to the pixel grid
    private val textOffsetX = 50f
    private val textOffsetY = 8f



    private val durabilityBarOffY = 35f



    override fun update(delta: Float) {
        if (item != null) {

        }
    }

    private val fwsp = 0x3000.toChar()

    override fun render(batch: SpriteBatch, camera: Camera) {

        // mouseover background
        if (item != null || drawBackOnNull) {
            // do not highlight even if drawBackOnNull is true
            if (mouseUp && item != null) {
                BlendMode.resolve(mouseoverBackBlendMode)
                batch.color = mouseoverBackCol
            }
            // if drawBackOnNull, just draw background
            else {
                BlendMode.resolve(backBlendMode)
                batch.color = backCol
            }
            batch.fillRect(posX.toFloat(), posY.toFloat(), width.toFloat(), height.toFloat())
        }


        if (item != null && itemImage != null) {
            blendNormal()
            
            // item image
            batch.color = Color.WHITE
            batch.draw(itemImage, posX + imgOffset, posY + imgOffset)

            // if mouse is over, text lights up
            // this one-liner sets color
            batch.color = item!!.nameColour mul if (mouseUp) mouseOverTextCol else inactiveTextCol
            // draw name of the item
            Terrarum.fontGame.draw(batch,
                    // print name and amount in parens
                    item!!.name + (if (amount > 0 && item!!.stackable) "$fwsp($amount)" else if (amount != 1) "$fwsp!!$amount!!" else "") +
                    // TEMPORARY print eqipped slot info as well
                    (if (equippedSlot != null) "  ${0xE081.toChar()}\$$equippedSlot" else ""),

                    posX + textOffsetX,
                    posY + textOffsetY
            )


            // durability metre
            val barFullLen = (width - 8f) - textOffsetX
            val barOffset = posX + textOffsetX
            if (item!!.maxDurability > 0.0) {
                batch.color = durabilityBack
                batch.drawStraightLine(barOffset, posY + durabilityBarOffY, barOffset + barFullLen, durabilityBarThickness, false)
                batch.color = durabilityCol
                batch.drawStraightLine(barOffset, posY + durabilityBarOffY, barOffset + barFullLen * (item!!.durability / item!!.maxDurability), durabilityBarThickness, false)
            }


            // quickslot marker (TEMPORARY UNTIL WE GET BETTER DESIGN)
            batch.color = Color.WHITE

            if (quickslot != null) {
                val label = quickslot!!.plus(0xE010).toChar()
                val labelW = Terrarum.fontGame.getWidth("$label")
                Terrarum.fontGame.draw(batch, "$label", barOffset + barFullLen - labelW, posY + textOffsetY)
            }

        }

        // see IFs above?
        batch.color = Color.WHITE

    }

    override fun keyDown(keycode: Int): Boolean {
        if (item != null && Terrarum.ingame != null && keycode in Input.Keys.NUM_1..Input.Keys.NUM_0) {
            val player = (Terrarum.ingame!! as Ingame).actorNowPlaying

            if (player == null) return false

            val inventory = player.inventory
            val slot = if (keycode == Input.Keys.NUM_0) 9 else keycode - Input.Keys.NUM_1
            val currentSlotItem = inventory?.getQuickBar(slot)


            inventory.setQuickBar(
                    slot,
                    if (currentSlotItem?.item != item)
                        item?.dynamicID // register
                    else
                        null // drop registration
            )

            // search for duplicates in the quickbar, except mine
            // if there is, unregister the other
            (0..9).minus(slot).forEach {
                if (inventory.getQuickBar(it)?.item == item) {
                    inventory.setQuickBar(it, null)
                }
            }
        }

        return true
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (item != null && Terrarum.ingame != null) {

            // equip da shit
            val itemEquipSlot = item!!.equipPosition
            if (itemEquipSlot == GameItem.EquipPosition.NULL) {
                TODO("Equip position is NULL, does this mean it's single-consume items like a potion?")
            }

            val player = (Terrarum.ingame!! as Ingame).actorNowPlaying

            if (player == null) return false

            if (item != player.inventory.itemEquipped.get(itemEquipSlot)) { // if this item is unequipped, equip it
                player.equipItem(item!!)
            }
            else { // if not, unequip it
                player.unequipItem(item!!)
            }
        }

        inventoryUI.rebuildList()

        return true
    }


    override fun dispose() {
        itemImage?.texture?.dispose()
    }

    override fun keyUp(keycode: Int): Boolean {
        return super.keyUp(keycode)
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return super.mouseMoved(screenX, screenY)
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return super.touchDragged(screenX, screenY, pointer)
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return super.touchUp(screenX, screenY, pointer, button)
    }

    override fun scrolled(amount: Int): Boolean {
        return super.scrolled(amount)
    }
}
