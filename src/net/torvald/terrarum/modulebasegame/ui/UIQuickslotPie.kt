package net.torvald.terrarum.modulebasegame.ui

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.jme3.math.FastMath
import net.torvald.terrarum.AppLoader
import net.torvald.terrarum.Second
import net.torvald.terrarum.Terrarum
import net.torvald.terrarum.gameactors.AVKey
import net.torvald.terrarum.itemproperties.ItemCodex
import net.torvald.terrarum.modulebasegame.TerrarumIngame
import net.torvald.terrarum.modulebasegame.ui.UIQuickslotBar.Companion.COMMON_OPEN_CLOSE
import net.torvald.terrarum.modulebasegame.ui.UIQuickslotBar.Companion.SLOT_COUNT
import net.torvald.terrarum.ui.UICanvas
import org.dyn4j.geometry.Vector2

/**
 * The Sims styled pie representation of the Quickslot.
 *
 * Created by minjaesong on 2016-07-20.
 */
class UIQuickslotPie : UICanvas() {
    private val cellSize = ItemSlotImageFactory.slotImage.tileW

    private val slotCount = UIQuickslotBar.SLOT_COUNT

    private val slotDistanceFromCentre: Double
            get() = cellSize * 2.5 * handler.scale
    override var width: Int = cellSize * 7
    override var height: Int = width


    /**
     * In milliseconds
     */
    override var openCloseTime: Second = COMMON_OPEN_CLOSE

    private val smallenSize = 0.93f

    var selection: Int = -1

    override fun updateUI(delta: Float) {
        if (selection >= 0 && (Terrarum.ingame!! as TerrarumIngame).actorNowPlaying != null)
            (Terrarum.ingame!! as TerrarumIngame).actorNowPlaying!!.actorValue[AVKey.__PLAYER_QUICKSLOTSEL] =
                    selection % slotCount


        // update controls
        if (handler.isOpened || handler.isOpening) {
            val cursorPos = Vector2(Terrarum.mouseScreenX.toDouble(), Terrarum.mouseScreenY.toDouble())
            val centre = Vector2(AppLoader.halfScreenW.toDouble(), AppLoader.halfScreenH.toDouble())
            val deg = -(centre - cursorPos).direction.toFloat()

            selection = Math.round(deg * slotCount / FastMath.TWO_PI)
            if (selection < 0) selection += SLOT_COUNT

            // TODO add gamepad support
        }
    }

    private val drawColor = Color(1f, 1f, 1f, 1f)

    override fun renderUI(batch: SpriteBatch, camera: Camera) {
        // draw radial thingies
        for (i in 0..slotCount - 1) {
            val item = ItemCodex[(Terrarum.ingame!! as TerrarumIngame).actorNowPlaying?.inventory?.getQuickslot(i)?.item]

            // set position
            val angle = Math.PI * 2.0 * (i.toDouble() / slotCount) + Math.PI // 180 deg monitor-wise
            val slotCentrePoint = Vector2(0.0, slotDistanceFromCentre).setDirection(-angle) // NOTE: NOT a center of circle!

            // draw cells
            val image = if (i == selection)
                ItemSlotImageFactory.produceLarge(false, (i + 1) % SLOT_COUNT, item)
            else
                ItemSlotImageFactory.produce(true, (i + 1) % SLOT_COUNT, item)

            val slotX = slotCentrePoint.x.toInt()
            val slotY = slotCentrePoint.y.toInt()

            drawColor.a = UIQuickslotBar.DISPLAY_OPACITY
            batch.color = drawColor
            image.draw(batch, slotX, slotY)

        }
    }

    override fun doOpening(delta: Float) {
        doOpeningFade(this, openCloseTime)
        handler.scale = smallenSize + (1f.minus(smallenSize) * handler.opacity)
    }

    override fun doClosing(delta: Float) {
        doClosingFade(this, openCloseTime)
        handler.scale = smallenSize + (1f.minus(smallenSize) * handler.opacity)
    }

    override fun endOpening(delta: Float) {
        endOpeningFade(this)
        handler.scale = 1f
    }

    override fun endClosing(delta: Float) {
        endClosingFade(this)
        handler.scale = 1f
    }

    override fun dispose() {
    }
}