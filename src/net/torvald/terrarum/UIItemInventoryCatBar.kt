package net.torvald.terrarum

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import net.torvald.terrarum.modulebasegame.ui.UIInventoryFull
import net.torvald.terrarum.ui.UIItem
import net.torvald.terrarum.ui.UIItemImageButton
import net.torvald.terrarum.ui.UIUtils

/**
 * Created by minjaesong on 2017-10-20.
 */
class UIItemInventoryCatBar(
        parentUI: UIInventoryFull,
        override var posX: Int,
        override var posY: Int,
        override val width: Int
) : UIItem(parentUI) {

    // deal with the moving position
    override var oldPosX = posX
    override var oldPosY = posY

    private val parentInventory = parentUI

    private val catIcons = parentUI.catIcons
    private val catArrangement = parentUI.catArrangement


    private val inventoryUI = parentUI
    override val height = catIcons.tileH + 5
    

    private val mainButtons: Array<UIItemImageButton>
    private val buttonGapSize = (width.toFloat() - (catArrangement.size * catIcons.tileW)) / (catArrangement.size)
    var selectedIndex = 0 // default to ALL
        private set
    val selectedIcon: Int
        get() = catArrangement[selectedIndex]

    private val sideButtons: Array<UIItemImageButton>

    // set up all the buttons
    init {
        // place sub UIs: Image Buttons
        mainButtons = Array(catArrangement.size) { index ->
            val iconPosX = ((buttonGapSize / 2) + index * (catIcons.tileW + buttonGapSize)).roundInt()
            val iconPosY = 0

            UIItemImageButton(
                    inventoryUI,
                    catIcons.get(catArrangement[index], 0),
                    activeBackCol = Color(0),
                    activeBackBlendMode = BlendMode.NORMAL,
                    posX = posX + iconPosX,
                    posY = posY + iconPosY,
                    highlightable = true
            )
        }


        // side buttons
        // NOTE: < > arrows must "highlightable = false"; "true" otherwise
        //  determine gaps: hacky way exploiting that we already know the catbar is always at the c of the ui
        val relativeStartX = posX - (parentUI.internalWidth - width) / 2
        val sideButtonsGap = (((parentUI.internalWidth - width) / 2) - 2f * catIcons.tileW) / 3f
        val iconIndex = arrayOf(12, 16, 17, 13)


        println("[UIItemInventoryCatBar] relativeStartX: $relativeStartX")
        println("[UIItemInventoryCatBar] posX: $posX")

        sideButtons = Array(iconIndex.size) { index ->
            val iconPosX = if (index < 2)
                (relativeStartX + sideButtonsGap + (sideButtonsGap + catIcons.tileW) * index).roundInt()
            else
                (relativeStartX + width + 2 * sideButtonsGap + (sideButtonsGap + catIcons.tileW) * index).roundInt()
            val iconPosY = 0

            UIItemImageButton(
                    inventoryUI,
                    catIcons.get(iconIndex[index], 0),
                    activeBackCol = Color(0),
                    activeBackBlendMode = BlendMode.NORMAL,
                    posX = iconPosX,
                    posY = posY + iconPosY,
                    buttonCol = if (index == 0 || index == 3) Color.WHITE else Color(0xffffff7f.toInt()),
                    activeCol = if (index == 0 || index == 3) Color(0xfff066_ff.toInt()) else Color(0xffffff7f.toInt()),
                    highlightable = (index == 0 || index == 3)
            )
        }
    }


    private val underlineIndTex: Texture
    private val underlineColour = Color(0xeaeaea_40.toInt())
    private val underlineHighlightColour = mainButtons[0].highlightCol

    private var highlighterXPos = mainButtons[selectedIndex].posX.toFloat()
    private var highlighterXStart = highlighterXPos
    private var highlighterXEnd = highlighterXPos

    private val highlighterYPos = catIcons.tileH + 4f
    private var highlighterMoving = false
    private val highlighterMoveDuration: Second = 0.1f
    private var highlighterMoveTimer: Second = 0f

    private var transitionFired = false

    /**
     * 0: map, 1: inventory caticons, 2: menu
     */
    var selectedPanel = 1
        private set

    // set up underlined indicator
    init {
        // procedurally generate texture
        val pixmap = Pixmap(catIcons.tileW + buttonGapSize.floorInt(), 1, Pixmap.Format.RGBA8888)
        for (x in 0 until pixmap.width.plus(1).ushr(1)) { // eqv. of ceiling the half-int
            val col = /*if      (x == 0)*/ /*0xffffff_80.toInt()*/
                      /*else if (x == 1)*/ /*0xffffff_c0.toInt()*/
                      /*else            */ 0xffffff_ff.toInt()

            pixmap.drawPixel(x, 0, col)
            pixmap.drawPixel(pixmap.width - (x + 1), 0, col)
        }
        underlineIndTex = Texture(pixmap)
        underlineIndTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
        pixmap.dispose()


        mainButtons[selectedIndex].highlighted = true
    }


    /** (oldIndex: Int?, newIndex: Int) -> Unit */
    var selectionChangeListener: ((Int?, Int) -> Unit)? = null

    override fun update(delta: Float) {
        super.update(delta)


        if (highlighterMoving) {
            highlighterMoveTimer += delta

            highlighterXPos = UIUtils.moveQuick(
                    highlighterXStart,
                    highlighterXEnd,
                    highlighterMoveTimer,
                    highlighterMoveDuration
            )

            if (highlighterMoveTimer > highlighterMoveDuration) {
                highlighterMoveTimer = 0f
                highlighterXStart = highlighterXEnd
                highlighterXPos = highlighterXEnd
                highlighterMoving = false
            }
        }


        mainButtons.forEachIndexed { index, btn ->
            btn.update(delta)

            if (btn.mousePushed && selectedPanel != 1) {
                transitionFired = true
                selectedPanel = 1
            }

            if (btn.mousePushed && index != selectedIndex) {
                // normal stuffs
                val oldIndex = selectedIndex

                highlighterXStart = mainButtons[selectedIndex].posX.toFloat() // using old selectedIndex
                selectedIndex = index
                highlighterMoving = true
                highlighterXEnd = mainButtons[selectedIndex].posX.toFloat() // using new selectedIndex

                selectionChangeListener?.invoke(oldIndex, index)
            }

            if (selectedPanel == 1) {
                btn.highlighted = (index == selectedIndex) // forcibly highlight if this.highlighted != null

                sideButtons[0].highlighted = false
                sideButtons[3].highlighted = false
            }
        }

        sideButtons[0].update(delta)
        sideButtons[3].update(delta)


        // more transition stuffs
        if (sideButtons[0].mousePushed) {
            if (selectedPanel != 0) transitionFired = true
            mainButtons.forEach { it.highlighted = false }
            selectedPanel = 0
            parentInventory.requestTransition(0)

            sideButtons[0].highlighted = true
            sideButtons[3].highlighted = false
        }
        else if (sideButtons[3].mousePushed) {
            if (selectedPanel != 2) transitionFired = true
            mainButtons.forEach { it.highlighted = false }
            selectedPanel = 2
            parentInventory.requestTransition(2)
            transitionFired = true

            sideButtons[0].highlighted = false
            sideButtons[3].highlighted = true
        }


        if (transitionFired) {
            transitionFired = false
            parentInventory.requestTransition(2 - selectedPanel)
        }
    }

    override fun render(batch: SpriteBatch, camera: Camera) {
        super.render(batch, camera)

        // button
        // colour determined by UI items themselves
        mainButtons.forEach { it.render(batch, camera) }
        sideButtons.forEach { it.render(batch, camera) }


        blendNormal(batch)


        // underline
        batch.color = underlineColour
        batch.drawStraightLine(posX.toFloat(), posY + height - 1f, posX + width.toFloat(), 1f, false)

        // indicator
        if (selectedPanel == 1) {
            batch.color = underlineHighlightColour
            batch.draw(underlineIndTex, (highlighterXPos - buttonGapSize / 2).toFloat().round(), posY + highlighterYPos)
        }


    }




    override fun dispose() {
        underlineIndTex.dispose()
        catIcons.dispose()
        mainButtons.forEach { it.dispose() }
        sideButtons.forEach { it.dispose() }
    }
}