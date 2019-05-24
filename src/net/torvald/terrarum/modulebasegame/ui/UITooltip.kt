package net.torvald.terrarum.modulebasegame.ui

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import net.torvald.terrarum.Second
import net.torvald.terrarum.Terrarum
import net.torvald.terrarum.ui.UICanvas

/**
 * Created by minjaesong on 2017-11-25.
 */
class UITooltip : UICanvas() {

    override var openCloseTime: Second = 0f

    var message: String = ""
        set(value) {
            field = value
            msgWidth = font.getWidth(value)
        }

    private val font = Terrarum.fontGame
    private var msgWidth = 0

    val textMarginX = 4

    override var width: Int
        get() = msgWidth + (textMarginX + FloatDrawer.tile.tileW) * 2
        set(value) { throw Error("You are not supposed to set the width of the tooltip manually.") }
    override var height: Int
        get() = FloatDrawer.tile.tileH * 2 + font.lineHeight.toInt()
        set(value) { throw Error("You are not supposed to set the height of the tooltip manually.") }

    override fun renderUI(batch: SpriteBatch, camera: Camera) {
        val mouseX = 4f
        val mouseY = 6f

        val tooltipY = mouseY - height

        val txtW = msgWidth + 2f * textMarginX

        batch.color = Color.WHITE

        FloatDrawer(batch, mouseX - textMarginX, tooltipY, txtW, font.lineHeight)
        font.draw(batch, message,
                mouseX,
                mouseY - height
        )
    }

    override fun updateUI(delta: Float) {
        setPosition(Terrarum.mouseScreenX, Terrarum.mouseScreenY)
    }

    override fun doOpening(delta: Float) {
    }

    override fun doClosing(delta: Float) {
    }

    override fun endOpening(delta: Float) {
    }

    override fun endClosing(delta: Float) {
    }

    override fun dispose() {
    }

}