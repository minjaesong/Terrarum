package net.torvald.terrarum.modulebasegame.ui

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import net.torvald.terrarum.ModMgr
import net.torvald.terrarum.Second
import net.torvald.terrarum.ui.UICanvas
import net.torvald.terrarum.ui.UIItemImageButton
import net.torvald.terrarumsansbitmap.gdx.TextureRegionPack

class UIBuildingMakerToolbox : UICanvas() {

    val toolsTexture = TextureRegionPack(ModMgr.getGdxFile("basegame", "gui/building_maker_toolbox.tga"), 16, 16)
    val tools = Array(toolsTexture.verticalCount) { UIItemImageButton(
            this, toolsTexture.get(0, it),
            initialX = 0,
            initialY = 20 * it,
            highlightable = true
    ) }

    override var width = 16
    override var height = 20 * tools.size - 4
    override var openCloseTime = 0f

    var selectedTool = 0; private set

    init {
        setAsAlwaysVisible()
        tools[selectedTool].highlighted = true
    }

    override fun updateUI(delta: Float) {
        tools.forEachIndexed { counter, it ->
            it.update(delta)

            if (it.highlighted) selectedTool = counter
        }
    }

    override fun renderUI(batch: SpriteBatch, camera: Camera) {
        tools.forEach { it.render(batch, camera) }
    }

    override fun doOpening(delta: Float) { }

    override fun doClosing(delta: Float) { }

    override fun endOpening(delta: Float) { }

    override fun endClosing(delta: Float) { }

    override fun dispose() {
        toolsTexture.dispose()
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
