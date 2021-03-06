package net.torvald.terrarum.modulebasegame.ui

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import net.torvald.random.HQRNG
import net.torvald.terrarum.AppLoader
import net.torvald.terrarum.AppLoader.printdbg
import net.torvald.terrarum.Second
import net.torvald.terrarum.Terrarum
import net.torvald.terrarum.modulebasegame.TerrarumIngame
import net.torvald.terrarum.modulebasegame.WorldgenLoadScreen
import net.torvald.terrarum.ui.UICanvas

/**
 * Created by minjaesong on 2018-12-08.
 */
class UIProxyNewRandomGame : UICanvas() {

    override var width: Int = 0
    override var height: Int = 0
    override var openCloseTime: Second = 0f

    override fun updateUI(delta: Float) {
    }

    override fun renderUI(batch: SpriteBatch, camera: Camera) {
    }

    override fun doOpening(delta: Float) {
    }

    override fun doClosing(delta: Float) {
        TODO("not implemented")
    }

    override fun endOpening(delta: Float) {
        printdbg(this, "endOpening")


        val ingame = TerrarumIngame(AppLoader.batch)
        val worldParam = TerrarumIngame.NewWorldParameters(2400, 1280, 0x51621DL)
        //val worldParam = TerrarumIngame.NewWorldParameters(2400, 1280, HQRNG().nextLong())

        //val worldParam = TerrarumIngame.NewWorldParameters(6000, 1800, 0x51621DL)
        //val worldParam = TerrarumIngame.NewWorldParameters(9000, 2250, 0x51621DL)
        //val worldParam = TerrarumIngame.NewWorldParameters(13500, 3000, 0x51621DL)
        //val worldParam = TerrarumIngame.NewWorldParameters(22500, 4500, 0x51621DL)
        ingame.gameLoadInfoPayload = worldParam
        ingame.gameLoadMode = TerrarumIngame.GameLoadMode.CREATE_NEW

        Terrarum.setCurrentIngameInstance(ingame)
        //LoadScreen.screenToLoad = ingame
        //AppLoader.setScreen(LoadScreen)
        val loadScreen = WorldgenLoadScreen(ingame, worldParam.width, worldParam.height)
        AppLoader.setLoadScreen(loadScreen)
    }

    override fun endClosing(delta: Float) {
        TODO("not implemented")
    }

    override fun dispose() {
        TODO("not implemented")
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return super.mouseMoved(screenX, screenY)
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return super.touchDragged(screenX, screenY, pointer)
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return super.touchDown(screenX, screenY, pointer, button)
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return super.touchUp(screenX, screenY, pointer, button)
    }

    override fun scrolled(amount: Int): Boolean {
        return super.scrolled(amount)
    }

    override fun keyDown(keycode: Int): Boolean {
        return super.keyDown(keycode)
    }

    override fun keyUp(keycode: Int): Boolean {
        return super.keyUp(keycode)
    }

    override fun keyTyped(character: Char): Boolean {
        return super.keyTyped(character)
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
    }
}