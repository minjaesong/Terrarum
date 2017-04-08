package net.torvald.terrarum.ui

import net.torvald.terrarum.mapdrawer.TilesDrawer
import net.torvald.terrarum.Terrarum
import com.jme3.math.FastMath
import net.torvald.terrarum.gamecontroller.KeyToggler
import org.lwjgl.opengl.GL11
import org.newdawn.slick.*
import org.newdawn.slick.state.StateBasedGame

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
                val toggleKey: Int? = null, val toggleButton: Int? = null
) {

    // X/Y Position to the game window.
    var posX: Int = 0
    var posY: Int = 0

    private var alwaysVisible = false

    private val UIGraphicInstance: Graphics
    private val UIDrawnCanvas: Image

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

    var opacity = 1f
    var scale = 1f

    var openCloseCounter: Int = 0

    init {
        UI.handler = this

        println("[UIHandler] Creating framebuffer for UI '${UI.javaClass.simpleName}'")

        UIDrawnCanvas = Image(UI.width, UI.height)
        UIGraphicInstance = UIDrawnCanvas.graphics
    }


    fun update(gc: GameContainer, delta: Int) {
        // open/close UI by key pressed
        if (toggleKey != null) {
            if (KeyToggler.isOn(toggleKey)) {
                setAsOpen()
            }
            else {
                setAsClose()
            }
        }



        if (isVisible || alwaysVisible) {
            UI.update(gc, delta)
        }

        if (isOpening) {
            isVisible = true
            openCloseCounter += delta

            // println("UI ${UI.javaClass.simpleName} (open)")
            // println("-> timecounter $openCloseCounter / ${UI.openCloseTime} timetakes")

            if (openCloseCounter < UI.openCloseTime) {
                UI.doOpening(gc, delta)
                // println("UIHandler.opening ${UI.javaClass.simpleName}")
            }
            else {
                UI.endOpening(gc, delta)
                isOpening = false
                isClosing = false
                isOpened = true
                openCloseCounter = 0
            }
        }
        else if (isClosing) {
            openCloseCounter += delta

            // println("UI ${UI.javaClass.simpleName} (close)")
            // println("-> timecounter $openCloseCounter / ${UI.openCloseTime} timetakes")

            if (openCloseCounter < UI.openCloseTime) {
                UI.doClosing(gc, delta)
                // println("UIHandler.closing ${UI.javaClass.simpleName}")
            }
            else {
                UI.endClosing(gc, delta)
                isClosing = false
                isOpening = false
                isOpened = false
                isVisible = false
                openCloseCounter = 0
            }
        }
    }

    fun render(gc: GameContainer, sbg: StateBasedGame, ingameGraphics: Graphics) {
        if (isVisible || alwaysVisible) {
            UIGraphicInstance.clear()
            UIGraphicInstance.font = Terrarum.fontGame

            UI.render(gc, UIGraphicInstance)

            ingameGraphics.drawImage(
                    UIDrawnCanvas, posX.toFloat(), posY.toFloat(), Color(1f, 1f, 1f, opacity)
            )
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
        if (alwaysVisible) {
            throw RuntimeException("[UIHandler] Tried to 'open' constant UI")
        }
        if (!isOpened && !isOpening) {
            isOpened = false
            isOpening = true
            isClosing = false
            isVisible = true
        }
    }

    /**
     * Send CLOSE signal to the attached UI.
     */
    fun setAsClose() {
        if (alwaysVisible) {
            throw RuntimeException("[UIHandler] Tried to 'close' constant UI")
        }
        if ((isOpening || isOpened) && !isClosing && isVisible) {
            isOpened = false
            isClosing = true
            isOpening = false
        }
    }

    val isClosed: Boolean
        get() = !isOpened && !isClosing && !isOpening

    fun toggleOpening() {
        if (alwaysVisible) {
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

    fun processInput(gc: GameContainer, delta: Int, input: Input) {
        if (isVisible) {
            UI.processInput(gc, delta, input)
        }
    }

    fun keyPressed(key: Int, c: Char) {
        if (isVisible && UI is KeyboardControlled) {
            UI.keyPressed(key, c)
        }
    }

    fun keyReleased(key: Int, c: Char) {
        if (isVisible && UI is KeyboardControlled) {
            UI.keyReleased(key, c)
        }
    }

    fun mouseMoved(oldx: Int, oldy: Int, newx: Int, newy: Int) {
        if (isVisible && UI is MouseControlled) {
            UI.mouseMoved(oldx, oldy, newx, newy)
        }
    }

    fun mouseDragged(oldx: Int, oldy: Int, newx: Int, newy: Int) {
        if (isVisible && UI is MouseControlled) {
            UI.mouseDragged(oldx, oldy, newx, newy)
        }
    }

    fun mousePressed(button: Int, x: Int, y: Int) {
        if (isVisible && UI is MouseControlled) {
            UI.mousePressed(button, x, y)
        }
    }

    fun mouseReleased(button: Int, x: Int, y: Int) {
        if (isVisible && UI is MouseControlled) {
            UI.mouseReleased(button, x, y)
        }
    }

    fun mouseWheelMoved(change: Int) {
        if (isVisible && UI is MouseControlled) {
            UI.mouseWheelMoved(change)
        }
    }

    fun controllerButtonPressed(controller: Int, button: Int) {
        if (isVisible && UI is GamepadControlled) {
            UI.controllerButtonPressed(controller, button)
        }
    }

    fun controllerButtonReleased(controller: Int, button: Int) {
        if (isVisible && UI is GamepadControlled) {
            UI.controllerButtonReleased(controller, button)
        }
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
