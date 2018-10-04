package net.torvald.terrarum.modulebasegame.gameactors

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import net.torvald.terrarum.Terrarum
import net.torvald.terrarum.gameworld.GameWorld
import net.torvald.terrarumsansbitmap.gdx.TextureRegionPack

/**
 * Created by minjaesong on 2017-01-07.
 */
class TapestryObject(world: GameWorld, pixmap: Pixmap, val artName: String, val artAuthor: String) : FixtureBase(world, physics = false) {

    // physics = false only speeds up for ~2 frames with 50 tapestries

    init {
        val texture = Texture(pixmap)
        pixmap.dispose()
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
        val texturePack = TextureRegionPack(texture, texture.width, texture.height)

        makeNewSprite(texturePack)
        setHitboxDimension(texture.width, texture.height, 0, 0)
        setPosition(Terrarum.mouseX, Terrarum.mouseY)
        // you CAN'T destroy the image
    }

    override fun update(delta: Float) {
        super.update(delta)
    }

    override fun drawBody(batch: SpriteBatch) {
        super.drawBody(batch)
    }

    override var tooltipText: String? = "$artName\n$artAuthor"
}