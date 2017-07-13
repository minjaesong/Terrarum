package net.torvald.terrarum.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import net.torvald.terrarum.Terrarum
import net.torvald.terrarum.gameactors.Second
import net.torvald.terrarum.gameactors.roundInt


/**
 * Created by minjaesong on 15-12-31.
 */
interface UICanvas {

    var width: Int
    var height: Int

    /**
     * Usage: (in StateInGame:) uiHandlerField.ui.handler = uiHandlerField
     */
    var handler: UIHandler?

    /**
     * In milliseconds
     *
     * Timer itself is implemented in the handler.
     */
    var openCloseTime: Second


    val relativeMouseX: Int
        get() = (Terrarum.mouseScreenX - (handler?.posX ?: 0))
    val relativeMouseY: Int
        get() = (Terrarum.mouseScreenY - (handler?.posY ?: 0))

    /** If mouse is hovering over it */
    val mouseUp: Boolean
        get() = relativeMouseX in 0..width - 1 && relativeMouseY in 0..height - 1
    /** If mouse is hovering over it and mouse is down */
    val mousePushed: Boolean
        get() = mouseUp && Gdx.input.isButtonPressed(Terrarum.getConfigInt("mouseprimary"))


    fun update(delta: Float)

    fun render(batch: SpriteBatch)

    fun processInput(delta: Float)

    /**
     * Do not modify handler!!.openCloseCounter here.
     */
    fun doOpening(delta: Float)

    /**
     * Do not modify handler!!.openCloseCounter here.
     */
    fun doClosing(delta: Float)

    /**
     * Do not modify handler!!.openCloseCounter here.
     */
    fun endOpening(delta: Float)

    /**
     * Do not modify handler!!.openCloseCounter here.
     */
    fun endClosing(delta: Float)

    fun dispose()

    companion object {
        const val OPENCLOSE_GENERIC = 0.2f

        fun doOpeningFade(handler: UIHandler?, openCloseTime: Second) {
            handler!!.opacity = handler.openCloseCounter / openCloseTime
        }
        fun doClosingFade(handler: UIHandler?, openCloseTime: Second) {
            handler!!.opacity = (openCloseTime - handler.openCloseCounter) / openCloseTime
        }
        fun endOpeningFade(handler: UIHandler?) {
            handler!!.opacity = 1f
        }
        fun endClosingFade(handler: UIHandler?) {
            handler!!.opacity = 0f
        }


        fun doOpeningPopOut(handler: UIHandler?, openCloseTime: Second, position: Position) {
            when (position) {
                Position.LEFT -> handler!!.posX = Movement.fastPullOut(
                        handler.openCloseCounter / openCloseTime,
                        -handler.UI.width.toFloat(),
                        0f
                ).roundInt()
                Position.TOP -> handler!!.posY = Movement.fastPullOut(
                        handler.openCloseCounter / openCloseTime,
                        -handler.UI.height.toFloat(),
                        0f
                ).roundInt()
                Position.RIGHT -> handler!!.posX = Movement.fastPullOut(
                        handler.openCloseCounter / openCloseTime,
                        Terrarum.WIDTH.toFloat(),
                        Terrarum.WIDTH - handler.UI.width.toFloat()
                ).roundInt()
                Position.BOTTOM -> handler!!.posY = Movement.fastPullOut(
                        handler.openCloseCounter / openCloseTime,
                        Terrarum.HEIGHT.toFloat(),
                        Terrarum.HEIGHT - handler.UI.height.toFloat()
                ).roundInt()
            }
        }
        fun doClosingPopOut(handler: UIHandler?, openCloseTime: Second, position: Position) {
            when (position) {
                Position.LEFT -> handler!!.posX = Movement.fastPullOut(
                        handler.openCloseCounter / openCloseTime,
                        0f,
                        -handler.UI.width.toFloat()
                ).roundInt()
                Position.TOP -> handler!!.posY = Movement.fastPullOut(
                        handler.openCloseCounter / openCloseTime,
                        0f,
                        -handler.UI.height.toFloat()
                ).roundInt()
                Position.RIGHT -> handler!!.posX = Movement.fastPullOut(
                        handler.openCloseCounter / openCloseTime,
                        Terrarum.WIDTH - handler.UI.width.toFloat(),
                        Terrarum.WIDTH.toFloat()
                ).roundInt()
                Position.BOTTOM -> handler!!.posY = Movement.fastPullOut(
                        handler.openCloseCounter / openCloseTime,
                        Terrarum.HEIGHT - handler.UI.height.toFloat(),
                        Terrarum.HEIGHT.toFloat()
                ).roundInt()
            }
        }
        fun endOpeningPopOut(handler: UIHandler?, position: Position) {
            when (position) {
                Position.LEFT -> handler!!.posX = 0
                Position.TOP -> handler!!.posY = 0
                Position.RIGHT -> handler!!.posX = Terrarum.WIDTH - handler.UI.width
                Position.BOTTOM -> handler!!.posY = Terrarum.HEIGHT - handler.UI.height
            }
        }
        fun endClosingPopOut(handler: UIHandler?, position: Position) {
            when (position) {
                Position.LEFT -> handler!!.posX = -handler.UI.width
                Position.TOP -> handler!!.posY = -handler.UI.height
                Position.RIGHT -> handler!!.posX = Terrarum.WIDTH
                Position.BOTTOM -> handler!!.posY = Terrarum.HEIGHT
            }
        }

        // TODO add blackboard take in/out (sinusoidal)

        enum class Position {
            LEFT, RIGHT, TOP, BOTTOM
        }
    }
}
