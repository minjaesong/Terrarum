package net.torvald.terrarum.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import net.torvald.terrarum.BlendMode
import net.torvald.terrarum.fillRect
import net.torvald.terrarum.gameactors.Second


/**
 * Created by minjaesong on 2017-08-01.
 */
class UIItemList<Item: UIItem>(
        parentUI: UICanvas,
        val itemList: ArrayList<Item>,
        override var posX: Int,
        override var posY: Int,
        override val width: Int,
        override val height: Int,

        var selectable: Boolean = false,
        val defaultSelection: Int? = null, // negative: INVALID, positive: valid, null: no select

        // copied directly from UIItemTextButton
        val activeCol: Color = Color(0xfff066_ff.toInt()),
        val activeBackCol: Color = Color(0),
        val activeBackBlendMode: String = BlendMode.NORMAL,
        val highlightCol: Color = Color(0x00f8ff_ff),
        val highlightBackCol: Color = Color(0xb0b0b0_ff.toInt()),
        val highlightBackBlendMode: String = BlendMode.MULTIPLY,
        val inactiveCol: Color = Color(0xc0c0c0_ff.toInt()),
        val backgroundCol: Color = Color(0x242424_80),
        val backgroundBlendMode: String = BlendMode.NORMAL,
        val kinematic: Boolean = false
) : UIItem(parentUI) {

    init {
        itemList.forEachIndexed { index, item ->
            item.posX = this.posX
            item.posY = if (index == 0) this.posY else itemList[index - 1].posY + itemList[index - 1].height
        }
    }


    var selectedIndex: Int? = defaultSelection
    val selectedButton: UIItem?
        get() = if (selectedIndex != null) itemList[selectedIndex!!] else null
    private var highlightY: Double? = if (selectedIndex != null) itemList[selectedIndex!!].posY.toDouble() else null
    private val highlighterMoveDuration: Second = 0.1f
    private var highlighterMoveTimer: Second = 0f
    private var highlighterMoving = false
    private var highlighterYStart = highlightY
    private var highlighterYEnd = highlightY

    /** (oldIndex: Int?, newIndex: Int) -> Unit */
    var selectionChangeListener: ((Int?, Int) -> Unit)? = null


    override fun update(delta: Float) {
        if (highlighterMoving) {
            highlighterMoveTimer += delta

            if (selectedIndex != null) {
                highlightY = UIUtils.moveQuick(
                        highlighterYStart!!,
                        highlighterYEnd!!,
                        highlighterMoveTimer.toDouble(),
                        highlighterMoveDuration.toDouble()
                )
            }

            if (highlighterMoveTimer > highlighterMoveDuration) {
                highlighterMoveTimer = 0f
                highlighterYStart = highlighterYEnd
                highlightY = highlighterYEnd
                highlighterMoving = false
            }
        }

        itemList.forEachIndexed { index, item ->
            item.update(delta)


            if (item.mousePushed && index != selectedIndex) {
                val oldIndex = selectedIndex

                if (kinematic) {
                    highlighterYStart = itemList[selectedIndex!!].posY.toDouble()
                    selectedIndex = index
                    highlighterMoving = true
                    highlighterYEnd = itemList[selectedIndex!!].posY.toDouble()
                }
                else {
                    selectedIndex = index
                    highlightY = itemList[selectedIndex!!].posY.toDouble()
                }

                selectionChangeListener?.invoke(oldIndex, index)
            }
            //item.highlighted = (index == selectedIndex) // forcibly highlight if this.highlighted != null

        }
    }

    override fun render(batch: SpriteBatch) {
        batch.color = backgroundCol
        BlendMode.resolve(backgroundBlendMode)
        batch.fillRect(posX.toFloat(), posY.toFloat(), width.toFloat(), height.toFloat())

        batch.color = highlightBackCol
        BlendMode.resolve(highlightBackBlendMode)
        if (highlightY != null) {
            batch.fillRect(posX.toFloat(), highlightY!!.toFloat(), width.toFloat(), UIItemTextButton.height.toFloat())
        }

        itemList.forEach { it.render(batch) }

        batch.color = backgroundCol
    }

    override fun dispose() {
        itemList.forEach { it.dispose() }
    }
}