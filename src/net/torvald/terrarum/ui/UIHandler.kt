package net.torvald.terrarum.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Matrix4
import net.torvald.terrarum.TerrarumGDX
import net.torvald.terrarum.gamecontroller.KeyToggler

/**
 * UIHandler is a handler for UICanvas. It opens/closes the attached UI, moves the "window" (or "canvas")
 * to the coordinate of displayed cartesian coords, and update and render the UI.
 * It also process game inputs and send control events to the UI so that the UI can handle them.
 *
 * Newly created UI is invisible by default.
 *
 * Created by minjaesong on 15-12-31.
 */
class UIHandler(val UI: UICanvas,
                val toggleKey: Int? = null, val toggleButton: Int? = null,
                // UI positions itself? (you must g.flush() yourself after the g.translate(Int, Int))
                var customPositioning: Boolean = false, // mainly used by vital meter
                val doNotWarnConstant: Boolean = false
) {

    // X/Y Position relative to the game window.
    var posX: Int = 0
    var posY: Int = 0

    private var alwaysVisible = false

    var isOpening = false
    var isClosing = false
    var isOpened = false // fully opened
    var isVisible: Boolean = false
        get() = if (alwaysVisible) true
                else field
        set(value) {
            if (alwaysVisible)
                throw RuntimeException("[UIHandler] Tried to 'set visibility of' constant UI")
            if (value == true) {
                isOpened = true
                field = value
            }
            else {
                isOpened = false
                field = value
            }
        }

    /**
     * being TRUE for only one frame when the UI is told to open
     */
    var openFired = false
    var closeFired = false

    var opacity = 1f
    var scale = 1f

    var openCloseCounter = 0f

    init {
        UI.handler = this
    }


    fun update(delta: Float) {
        // open/close UI by key pressed
        if (toggleKey != null) {
            if (KeyToggler.isOn(toggleKey)) {
                setAsOpen()
            }
            else {
                setAsClose()
            }
        }
        if (toggleButton != null) {
            /* */
        }


        if (openFired && openCloseCounter > 9) openFired = false
        if (closeFired && openCloseCounter > 9) closeFired = false


        if (isVisible || alwaysVisible) {
            UI.update(delta)
        }

        if (isOpening) {
            isVisible = true
            openCloseCounter += delta

            // println("UI ${UI.javaClass.simpleName} (open)")
            // println("-> timecounter $openCloseCounter / ${UI.openCloseTime} timetakes")

            if (openCloseCounter < UI.openCloseTime) {
                UI.doOpening(delta)
                // println("UIHandler.opening ${UI.javaClass.simpleName}")
            }
            else {
                UI.endOpening(delta)
                isOpening = false
                isClosing = false
                isOpened = true
                openCloseCounter = 0f
            }
        }
        else if (isClosing) {
            openCloseCounter += delta

            // println("UI ${UI.javaClass.simpleName} (close)")
            // println("-> timecounter $openCloseCounter / ${UI.openCloseTime} timetakes")

            if (openCloseCounter < UI.openCloseTime) {
                UI.doClosing(delta)
                // println("UIHandler.closing ${UI.javaClass.simpleName}")
            }
            else {
                UI.endClosing(delta)
                isClosing = false
                isOpening = false
                isOpened = false
                isVisible = false
                openCloseCounter = 0f
            }
        }
    }

    fun render(batch: SpriteBatch) {
        if (isVisible || alwaysVisible) {
            // camera SHOULD BE CENTERED to HALFX and HALFY (see StateInGame) //


            //batch.projectionMatrix = Matrix4()
//
            //if (!customPositioning)
            //    TerrarumGDX.ingame?.camera?.position?.set(posX.toFloat(), posY.toFloat(), 0f) // does it work?



            if (!customPositioning)
                TerrarumGDX.ingame?.setCameraPosition(posX.toFloat(), posY.toFloat())
            batch.color = Color.WHITE

            UI.render(batch)
            //ingameGraphics.flush()
        }
    }

    fun setPosition(x: Int, y: Int) {
        posX = x
        posY = y
    }

    fun setAsAlwaysVisible() {
        isVisible = true
        alwaysVisible = true
        isOpened = true
        isOpening = false
        isClosing = false
    }

    /**
     * Send OPEN signal to the attached UI.
     */
    fun setAsOpen() {
        if (alwaysVisible && !doNotWarnConstant) {
            throw RuntimeException("[UIHandler] Tried to 'open' constant UI")
        }
        if (!isOpened && !isOpening) {
            isOpened = false
            isOpening = true
            isClosing = false
            isVisible = true

            openFired = true
        }
    }

    /**
     * Send CLOSE signal to the attached UI.
     */
    fun setAsClose() {
        if (alwaysVisible && !doNotWarnConstant) {
            throw RuntimeException("[UIHandler] Tried to 'close' constant UI")
        }
        if ((isOpening || isOpened) && !isClosing && isVisible) {
            isOpened = false
            isClosing = true
            isOpening = false

            closeFired = true
        }
    }

    val isClosed: Boolean
        get() = !isOpened && !isClosing && !isOpening

    fun toggleOpening() {
        if (alwaysVisible && !doNotWarnConstant) {
            throw RuntimeException("[UIHandler] Tried to 'toggle opening of' constant UI")
        }
        if (isVisible) {
            if (!isClosing) {
                setAsClose()
            }
        }
        else {
            if (!isOpening) {
                setAsOpen()
            }
        }
    }

    fun processInput(delta: Float) {
        if (isVisible) {
            UI.processInput(delta)
        }
    }

    fun keyDown(keycode: Int): Boolean {
        if (isVisible && UI is KeyControlled) {
            return UI.keyDown(keycode)
        }

        return false
    }

    fun keyUp(keycode: Int): Boolean {
        if (isVisible && UI is KeyControlled) {
            return UI.keyUp(keycode)
        }

        return false
    }

    fun keyTyped(char: Char): Boolean {
        if (isVisible && UI is KeyControlled) {
            return UI.keyTyped(char)
        }

        return false
    }

    fun mouseMoved(screenX: Int, screenY: Int) {
        if (isVisible && UI is MouseControlled) {
            UI.mouseMoved(screenX, screenY)
        }
    }

    fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        if (isVisible && UI is MouseControlled) {
            UI.touchDragged(screenX, screenY, pointer)
        }

        return false
    }

    fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (isVisible && UI is MouseControlled) {
            UI.touchDown(screenX, screenY, pointer, button)
        }

        return false
    }

    fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (isVisible && UI is MouseControlled) {
            UI.touchUp(screenX, screenY, pointer, button)
        }

        return false
    }

    fun scrolled(amount: Int): Boolean {
        if (isVisible && UI is MouseControlled) {
            UI.scrolled(amount)
        }

        return false
    }

    // constant UI can't take control
    val isTakingControl: Boolean
        get() {
            if (alwaysVisible) {
                return false
            }
            return isVisible && !isOpening
        }
}
