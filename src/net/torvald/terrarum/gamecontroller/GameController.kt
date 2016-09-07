package net.torvald.terrarum.gamecontroller

import net.torvald.terrarum.gameactors.Controllable
import net.torvald.terrarum.gameactors.Player
import net.torvald.terrarum.mapdrawer.MapCamera
import net.torvald.terrarum.mapdrawer.MapDrawer
import net.torvald.terrarum.Terrarum
import net.torvald.terrarum.gameactors.ProjectileSimple
import net.torvald.terrarum.tileproperties.TileNameCode
import net.torvald.terrarum.tileproperties.TilePropCodex
import net.torvald.terrarum.ui.UIHandler
import org.dyn4j.geometry.Vector2
import org.newdawn.slick.Input

/**
 * Created by minjaesong on 15-12-31.
 */
object GameController {

    val mouseX: Float
        get() = (MapCamera.cameraX + Terrarum.appgc.input.mouseX / Terrarum.ingame.screenZoom)
    val mouseY: Float
        get() = (MapCamera.cameraY + Terrarum.appgc.input.mouseY / Terrarum.ingame.screenZoom)
    val mouseTileX: Int
        get() = (mouseX / MapDrawer.TILE_SIZE).toInt()
    val mouseTileY: Int
        get() = (mouseY / MapDrawer.TILE_SIZE).toInt()

    fun processInput(input: Input) {

        KeyToggler.update(input)


        if (!Terrarum.ingame.consoleHandler.isTakingControl) {
            if (Terrarum.ingame.player.vehicleRiding != null) {
                Terrarum.ingame.player.vehicleRiding!!.processInput(input)
            }

            Terrarum.ingame.player.processInput(input)

            for (ui in Terrarum.ingame.uiContainer) {
                ui.processInput(input)
            }
        }
        else {
            Terrarum.ingame.consoleHandler.processInput(input)
        }


        if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
            // test tile remove
            try {
                Terrarum.ingame.world.setTileTerrain(mouseTileX, mouseTileY, TileNameCode.AIR)
                // terrarum.game.map.setTileWall(mouseTileX, mouseTileY, TileNameCode.AIR);
            }
            catch (e: ArrayIndexOutOfBoundsException) {
            }


        }
        else if (input.isMouseButtonDown(Input.MOUSE_RIGHT_BUTTON)) {
            // test tile place
            try {
                Terrarum.ingame.world.setTileTerrain(
                        mouseTileX, mouseTileY,
                        Terrarum.ingame.player.actorValue.getAsInt("__selectedtile")!!
                )
            }
            catch (e: ArrayIndexOutOfBoundsException) {
            }

        }
    }

    fun keyPressed(key: Int, c: Char) {
        if (keyPressedByCode(key, EnumKeyFunc.UI_CONSOLE)) {
            Terrarum.ingame.consoleHandler.toggleOpening()
        }
        else if (keyPressedByCode(key, EnumKeyFunc.UI_BASIC_INFO)) {
            Terrarum.ingame.debugWindow.toggleOpening()
        }



        if (!Terrarum.ingame.consoleHandler.isTakingControl) {
            if (Terrarum.ingame.player.vehicleRiding != null) {
                Terrarum.ingame.player.vehicleRiding!!.keyPressed(key, c)
            }

            Terrarum.ingame.player.keyPressed(key, c)
        }
        else {
            Terrarum.ingame.consoleHandler.keyPressed(key, c)
        }

        //System.out.println(String.valueOf(key) + ", " + String.valueOf(c));
    }

    fun keyReleased(key: Int, c: Char) {

    }

    fun mouseMoved(oldx: Int, oldy: Int, newx: Int, newy: Int) {

    }

    fun mouseDragged(oldx: Int, oldy: Int, newx: Int, newy: Int) {

    }

    fun mousePressed(button: Int, x: Int, y: Int) {
        if (button == 0) {
            Terrarum.ingame.addActor(ProjectileSimple(
                    0,
                    Terrarum.ingame.player.centrePosition,
                    Vector2(mouseX.toDouble(), mouseY.toDouble())
            ))
        }
    }

    fun mouseReleased(button: Int, x: Int, y: Int) {

    }

    fun mouseWheelMoved(change: Int) {

    }

    fun controllerButtonPressed(controller: Int, button: Int) {

    }

    fun controllerButtonReleased(controller: Int, button: Int) {

    }

    private fun keyPressedByCode(key: Int, fn: EnumKeyFunc): Boolean {
        return KeyMap.getKeyCode(fn) == key
    }
}
